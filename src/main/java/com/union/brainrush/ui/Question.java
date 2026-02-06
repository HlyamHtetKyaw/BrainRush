package com.union.brainrush.ui;

import com.union.brainrush.model.QuestionFormat;
import com.union.brainrush.routing.SceneManager;
import com.union.brainrush.service.Player;
import com.union.brainrush.service.PlayerManager;
import com.union.brainrush.service.QuestionService;
import com.union.brainrush.service.SoundService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

@Component
@Lazy
public class Question {
	public static int questionPointer = 0;
	public static int totalQuestion = 10;

	private Scene scene;
	public static StackPane root;
	private final List<String> bgColor = new ArrayList<>(Arrays.asList(
			"#ed801b", "#004aad", "#ebcd35", "#b64747", "#3fa278",
			"#f7c89f", "#4b6240", "#d48e38", "#623f31", "#684f9b"
	));

	private StackPane questionPane, answerPane, qUpperLayout, qBottomLayout;
	public static Button actionButton;
	private Button homeButton;
	private HBox playerHBox;
	private VBox answersVBox;
	private TextFlow textFlow;

	private final Font burmeseFont = Font.loadFont(getClass().getResourceAsStream(UiConstant.NOTO_REGULAR_PATH), 24);
	private final Font englishFont = Font.font("Arial", 22);

	private final QuestionService questionService;
	private List<QuestionFormat> questionArray;
	private QuestionFormat currentQuestion;

	@Autowired @Lazy TransitionState transitionState;
	@Autowired SceneManager sceneManager;

	@Autowired
	private PlayerManager playerManager;
	@Autowired
	private SoundService soundService;
	@Autowired
	public Question(QuestionService questionService) {
		this.questionService = questionService;
	}

	public void questionState(boolean shuffle, double width, double height) {
		// Data Initialization
		this.questionArray = questionService.getRandomRound(totalQuestion);

		if (questionArray.isEmpty()) {
			System.err.println("No questions found in database!");
			return;
		}

		if (shuffle) {
			Collections.shuffle(questionArray);
			Collections.shuffle(bgColor);
		}

		currentQuestion = questionArray.get(questionPointer);
		root = new StackPane();
		HBox mainLayout = new HBox();

		// UI Components
		questionPane = new StackPane();
		qUpperLayout = new StackPane();
		qBottomLayout = new StackPane();

		homeButton = new Button();
		ImageView homeImage = new ImageView(new Image("images/home/home.png"));
		homeButton.setGraphic(homeImage);
		homeImage.setFitWidth(50);
		homeImage.setFitHeight(50);
		homeButton.getStyleClass().add("bottom_format");
		homeButton.setOnAction(e -> switchBackToHome());

		playerHBox = new HBox(15);
		playerHBox.setAlignment(Pos.CENTER);
		for (int i = 0; i < Player.playerQuantity; i++) {
			ImageView pv = new ImageView();
			pv.setImage(i==0 ? UiConstant.firstPlayer : i==1 ? UiConstant.secondPlayer : UiConstant.thirdPlayer);
			pv.setFitWidth(60);
			pv.setPreserveRatio(true);
			playerHBox.getChildren().add(pv);
			// Store references in the array for the checkPlayerMark logic
			playerSlot[i] = pv;
		}

		actionButton = new Button("Next");
		actionButton.setVisible(false);
		actionButton.setOnAction(e -> nextQuestion());
		qUpperLayout.getChildren().addAll(playerHBox, actionButton);

		Player.rightAns = currentQuestion.getRightAns();
		textFlow = createTextFlow(currentQuestion.getQuestion());
		textFlow.getStyleClass().add("question_text_flow");

		VBox questionCenterer = new VBox(textFlow);
		questionCenterer.setAlignment(Pos.CENTER);
		qBottomLayout.getChildren().add(questionCenterer);

		questionPane.getChildren().addAll(qUpperLayout, qBottomLayout);

		// RIGHT SIDE: Answer Rows as Clickable Buttons
		answerPane = new StackPane();
		answersVBox = new VBox(25);
		answersVBox.setAlignment(Pos.CENTER_LEFT);
		answersVBox.setPadding(new Insets(0, 0, 0, 30));

		String[] keys = {"A", "B", "C"};
		for (String key : keys) {
			HBox row = new HBox(20);
			row.setAlignment(Pos.CENTER_LEFT);
			row.getStyleClass().add("answer_row"); // For hover styling in CSS
			row.setCursor(javafx.scene.Cursor.HAND);

			Label circleLabel = new Label(key);
			circleLabel.getStyleClass().add("ABC");
			circleLabel.setMinWidth(65);
			circleLabel.setMinHeight(65);
			circleLabel.setAlignment(Pos.CENTER);

			TextFlow ansText = createTextFlow(currentQuestion.getAns().getOrDefault(key, ""));
			ansText.getStyleClass().add("answer");
			ansText.setMinWidth(280);
			ansText.setMaxWidth(280);

			row.getChildren().addAll(circleLabel, ansText);

			// MOUSE CLICK LOGIC (Replaces Serial Service)
			row.setOnMouseClicked(event -> {
				handleAnswerSelection(key);
			});

			answersVBox.getChildren().add(row);
		}
		answerPane.getChildren().add(answersVBox);

		mainLayout.getChildren().addAll(questionPane, answerPane);
		root.getChildren().addAll(mainLayout, homeButton);

		root.setStyle("-fx-background-color:" + bgColor.get(questionPointer % bgColor.size()) + ";");

		scene = new Scene(root, width, height);
		applyBindings();
		scene.getStylesheets().add("css/style.css");
		positionPane();
	}

	private void handleAnswerSelection(String choice) {
		// Assign choice to player 1 (assuming 1-player mode for clicks)
		Player.fPlayerAns = choice;

		// Update UI feedback (Changing player icon to confirm state)
		if (playerSlot[0] != null) {
			playerSlot[0].setImage(UiConstant.firstPlayerConfirm);
		}

		// Disable further clicks to prevent double-firing
		answersVBox.setDisable(true);

		// Trigger next question (mimicking the actionButton.fire() from serial)
		actionButton.fire();
	}

	private void applyBindings() {
		questionPane.prefWidthProperty().bind(scene.widthProperty().multiply(0.6));
		answerPane.prefWidthProperty().bind(scene.widthProperty().multiply(0.4));
		qUpperLayout.prefHeightProperty().bind(scene.heightProperty().multiply(0.25));
		qBottomLayout.prefHeightProperty().bind(scene.heightProperty().multiply(0.75));
		textFlow.prefWidthProperty().bind(questionPane.widthProperty().multiply(0.8));
	}

	private void positionPane() {
		StackPane.setAlignment(homeButton, Pos.BOTTOM_LEFT);
		StackPane.setMargin(homeButton, new Insets(30));
		StackPane.setAlignment(qUpperLayout, Pos.TOP_CENTER);
		StackPane.setAlignment(qBottomLayout, Pos.CENTER);
	}

	private TextFlow createTextFlow(String text) {
		TextFlow flow = new TextFlow();
		if (text == null) return flow;
		Pattern englishPattern = Pattern.compile("[A-Za-z]");
		String[] parts = text.split("(?=[A-Za-z])|(?<=[A-Za-z])");
		for (String part : parts) {
			Text t = new Text(part);
			t.setFont(englishPattern.matcher(part).find() ? englishFont : burmeseFont);
			flow.getChildren().add(t);
		}
		return flow;
	}

	public void nextQuestion() {
		questionPointer++;
		if (questionPointer >= totalQuestion || questionPointer >= questionArray.size()) {
			if (Player.rightAns.equalsIgnoreCase(Player.fPlayerAns)) {
				soundService.playSfx("correct");
			}else{
				soundService.playSfx("wrong");
			}
			sceneManager.switchToResult();
		} else {
			if (Player.rightAns.equalsIgnoreCase(Player.fPlayerAns)) {
				soundService.playSfx("correct");
				Player.fPlayerMark++; // Reward player
				transitionState.showTransitionState("မှန်ကန်ပါတယ်!", root, false, true);
			} else {
				soundService.playSfx("wrong");
				transitionState.showTransitionState("မှားယွင်းနေပါတယ်!", root, false, false);
			}
		}
	}

	private void checkPlayerMark() {
		// Logic based on your old mark tracking
		if (Player.rightAns.equalsIgnoreCase(Player.fPlayerAns)) Player.fPlayerMark++;
		if (Player.rightAns.equalsIgnoreCase(Player.sPlayerAns)) Player.sPlayerMark++;
		if (Player.rightAns.equalsIgnoreCase(Player.tPlayerAns)) Player.tPlayerMark++;
	}

	private void switchBackToHome() {
		soundService.playSfx("click");
		playerManager.abandonSession();
		sceneManager.switchToHome(true);
		questionPointer = 0;
		Player.resetPlayerMark();
	}

	public static ImageView[] playerSlot = new ImageView[3];
	public Scene getScene() { return scene; }
}