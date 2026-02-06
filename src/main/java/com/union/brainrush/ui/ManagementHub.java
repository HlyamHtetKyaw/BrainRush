package com.union.brainrush.ui;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.union.brainrush.model.PlayerEntity;
import com.union.brainrush.model.QuestionFormat;
import com.union.brainrush.repository.PlayerRepository;
import com.union.brainrush.service.QuestionService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
public class ManagementHub {

    @Autowired private PlayerRepository playerRepository;
    @Autowired private QuestionService questionService;

    // Class-level fields for the Editor Form
    private TextArea qTextArea;
    private TextField ansA, ansB, ansC;
    private ComboBox<String> rightAns;
    private QuestionFormat currentlyEditing = null;

    public void show(StackPane root) {
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        tabPane.getTabs().addAll(
                new Tab("Player Dashboard", createPlayerDashboard()),
                new Tab("Question Management", createQuestionDashboard())
        );

        VBox container = new VBox(tabPane);
        container.setPadding(new Insets(15));
        container.setStyle("-fx-background-color: white; -fx-background-radius: 15;");
        container.setMaxSize(1100, 850);

        StackPane overlay = new StackPane(container);
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.85);");

        overlay.setOnMouseClicked(e -> {
            if (e.getTarget() == overlay) root.getChildren().remove(overlay);
        });

        root.getChildren().add(overlay);
    }

    private VBox createPlayerDashboard() {
        // 1. Calculate Player Count
        long count = playerRepository.count();
        Label countLabel = new Label("Total Players: " + count);
        countLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // 2. Setup Table
        TableView<PlayerEntity> table = new TableView<>();
        TableColumn<PlayerEntity, String> uuidCol = new TableColumn<>("Player UUID");
        uuidCol.setCellValueFactory(new PropertyValueFactory<>("uuid"));

        TableColumn<PlayerEntity, Integer> markCol = new TableColumn<>("Mark");
        markCol.setCellValueFactory(new PropertyValueFactory<>("mark"));

        TableColumn<PlayerEntity, LocalDateTime> dateCol = new TableColumn<>("Created At");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("createdAt"));

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        dateCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.format(dtf));
            }
        });

        table.getColumns().addAll(uuidCol, markCol, dateCol);
        List<PlayerEntity> allPlayers = playerRepository.findAll();
        table.setItems(FXCollections.observableArrayList(allPlayers));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // 3. Download Button Logic
        Button downloadBtn = new Button("Download Player Info (CSV)");
        downloadBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold;");

        downloadBtn.setOnAction(e -> {
            exportPlayersToCSV(allPlayers);
        });

        HBox header = new HBox(20, countLabel, new Region() {{ HBox.setHgrow(this, Priority.ALWAYS); }}, downloadBtn);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox layout = new VBox(15, header, table);
        layout.setPadding(new Insets(15));
        return layout;
    }
    private void exportPlayersToCSV(List<PlayerEntity> players) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Player Information");
        fileChooser.setInitialFileName("player_records.csv");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));

        File file = fileChooser.showSaveDialog(new Stage());

        if (file != null) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                // Write CSV Header
                writer.println("ID,UUID,Mark,CreatedAt");

                // Write Player Data
                for (PlayerEntity p : players) {
                    writer.printf("%d,%s,%d,%s%n",
                            p.getId(),
                            p.getUuid(),
                            p.getMark(),
                            p.getCreatedAt().toString());
                }

                new Alert(Alert.AlertType.INFORMATION, "File saved successfully!").show();
            } catch (Exception ex) {
                new Alert(Alert.AlertType.ERROR, "Failed to save file: " + ex.getMessage()).show();
            }
        }
    }
    private VBox createQuestionDashboard() {
        // 1. Data Logic
        ObservableList<QuestionFormat> masterData = FXCollections.observableArrayList(questionService.getQuestions());
        FilteredList<QuestionFormat> filteredData = new FilteredList<>(masterData, p -> true);

        // 2. Header: Search and Batch Buttons
        TextField searchBox = new TextField();
        searchBox.setPromptText("🔍 Search by Question text or ID...");
        searchBox.setPrefWidth(400);
        searchBox.setPrefHeight(35);

        Button batchBtn = new Button("Batch Upload (JSON)");
        batchBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold;");

        Button deleteBtn = new Button("Delete Selected");
        deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold;");

        HBox topBar = new HBox(15, searchBox, new Region() {{ HBox.setHgrow(this, Priority.ALWAYS); }}, batchBtn, deleteBtn);
        topBar.setAlignment(Pos.CENTER_LEFT);

        // 3. Table: With ID and Question Columns
        TableView<QuestionFormat> table = new TableView<>(filteredData);
        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE); // MULTI-SELECT ENABLED

        TableColumn<QuestionFormat, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(60);

        TableColumn<QuestionFormat, String> qCol = new TableColumn<>("Question Content");
        qCol.setCellValueFactory(new PropertyValueFactory<>("question"));

        table.getColumns().addAll(idCol, qCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(350);

        // 4. Search Filter Logic
        searchBox.textProperty().addListener((obs, old, newVal) -> {
            filteredData.setPredicate(q -> {
                if (newVal == null || newVal.isEmpty()) return true;
                String filter = newVal.toLowerCase();
                return q.getQuestion().toLowerCase().contains(filter) ||
                        String.valueOf(q.getId()).contains(filter);
            });
        });

        // 5. Large Form Editor (TextArea)
        qTextArea = new TextArea();
        qTextArea.setPromptText("Type your question here...");
        qTextArea.setPrefRowCount(4);
        qTextArea.setWrapText(true);

        ansA = new TextField(); ansB = new TextField(); ansC = new TextField();
        rightAns = new ComboBox<>(FXCollections.observableArrayList("A", "B", "C"));
        rightAns.setPromptText("Key");

        Button saveBtn = new Button("Save / Update Entry");
        saveBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20;");
        Button clearBtn = new Button("Clear");

        GridPane form = new GridPane();
        form.setHgap(10); form.setVgap(10);
        form.setPadding(new Insets(10));
        form.add(new Label("Question:"), 0, 0); form.add(qTextArea, 1, 0, 3, 1);
        form.add(new Label("Answer A:"), 0, 1); form.add(ansA, 1, 1);
        form.add(new Label("Answer B:"), 0, 2); form.add(ansB, 1, 2);
        form.add(new Label("Answer C:"), 0, 3); form.add(ansC, 1, 3);
        form.add(new Label("Correct:"), 0, 4); form.add(rightAns, 1, 4);
        form.add(new HBox(10, saveBtn, clearBtn), 1, 5);

        // 6. Action Listeners
        table.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            if (newVal != null) {
                currentlyEditing = newVal;
                qTextArea.setText(newVal.getQuestion());
                ansA.setText(newVal.getAns().get("A"));
                ansB.setText(newVal.getAns().get("B"));
                ansC.setText(newVal.getAns().get("C"));
                rightAns.setValue(newVal.getRightAns());
            }
        });

        saveBtn.setOnAction(e -> {
            if (qTextArea.getText().isEmpty() || rightAns.getValue() == null) return;
            QuestionFormat q = (currentlyEditing == null) ? new QuestionFormat() : currentlyEditing;
            q.setQuestion(qTextArea.getText());
            q.setRightAns(rightAns.getValue());
            Map<String, String> answers = new HashMap<>();
            answers.put("A", ansA.getText()); answers.put("B", ansB.getText()); answers.put("C", ansC.getText());
            q.setAns(answers);
            questionService.saveOrUpdate(q);
            masterData.setAll(questionService.getQuestions());
            clearForm();
        });

        clearBtn.setOnAction(e -> clearForm());

        // Batch Upload Action
        batchBtn.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
            File file = chooser.showOpenDialog(new Stage());
            if (file != null) {
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    List<QuestionFormat> list = mapper.readValue(file, new TypeReference<List<QuestionFormat>>(){});
                    list.forEach(q -> q.setId(0)); // Ensure they are treated as new
                    questionService.batchSave(list);
                    masterData.setAll(questionService.getQuestions());
                    new Alert(Alert.AlertType.INFORMATION, "Successfully imported " + list.size() + " items.").show();
                } catch (Exception ex) {
                    new Alert(Alert.AlertType.ERROR, "Import Error: " + ex.getMessage()).show();
                }
            }
        });

        // Multi-Delete Action
        deleteBtn.setOnAction(e -> {
            List<QuestionFormat> selected = new ArrayList<>(table.getSelectionModel().getSelectedItems());
            if (selected.isEmpty()) return;

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Delete " + selected.size() + " questions?", ButtonType.YES, ButtonType.NO);
            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.YES) {
                    questionService.deleteAll(selected);
                    masterData.setAll(questionService.getQuestions());
                    clearForm();
                }
            });
        });

        VBox layout = new VBox(15, topBar, table, new Separator(), form);
        layout.setPadding(new Insets(15));
        return layout;
    }

    private void clearForm() {
        currentlyEditing = null; qTextArea.clear();
        ansA.clear(); ansB.clear(); ansC.clear(); rightAns.setValue(null);
    }
}