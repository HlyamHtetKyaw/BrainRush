package com.union.brainrush.ui;

import com.union.brainrush.service.PlayerManager;
import com.union.brainrush.routing.SceneManager;
import com.union.brainrush.service.Player;
import com.union.brainrush.service.SoundService;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@Lazy
public class Result {
	private Scene scene;
	private StackPane root;
	private VBox vBox;

	@Autowired
	private SceneManager sceneManager;

	@Autowired
	private PlayerManager playerManager;
	@Autowired
	private SoundService soundService;

	// UI Elements declared at class level
	private HBox firstSlot;
	private StackPane fTextSlot, fImageSlot, fProgressSlot;
	private Label fLabel, fMark;
	private ImageView fImage;
	private ProgressBar firstProgressBar;
	private Button homeButton;

	private final String EXCELLENT = "တအားတော်တာပဲ";
	private final String GOOD = "တော်တယ်နော်";
	private final String KEEP_IT_UP = "ကြိုးစားထားပါ";
	private final String TRY_HARDER = "ပိုကြိုးစားပါဦးနော်";

	public Result() {
		// Initialize the root and scene once
		root = new StackPane();
		scene = new Scene(root, UiConstant.WIDTH, UiConstant.HEIGHT);
	}

	/**
	 * This method MUST be called by the SceneManager before switching to the Result scene.
	 * It resets the UI and re-runs the logic with the latest score.
	 */
	public void initResultState() {
		root.getChildren().clear(); // Important: Clear old UI state

		vBox = new VBox();
		double[] vBoxProportions = { 0.3, 0.4, 0.3 };

		firstSlot = new HBox();
		fTextSlot = new StackPane();
		fLabel = new Label();
		fTextSlot.getChildren().add(fLabel);

		fImageSlot = new StackPane();
		fImage = new ImageView(new Image("images/result/eaistein.png"));
		fImageSlot.getChildren().add(fImage);

		fProgressSlot = new StackPane();
		fMark = new Label("0/10");
		firstProgressBar = new ProgressBar(0);

		// Responsive bindings
		responsive();

		fProgressSlot.getChildren().addAll(firstProgressBar, fMark);
		firstSlot.getChildren().addAll(fTextSlot, fImageSlot, fProgressSlot);

		vBox.getChildren().addAll(new StackPane(), firstSlot, new StackPane());
		vBox.maxWidthProperty().bind(root.widthProperty().multiply(0.8));

		// --- HOME BUTTON ---
		homeButton = new Button();
		ImageView homeImage = new ImageView(new Image("images/home/home.png"));
		homeImage.setFitWidth(50);
		homeImage.setFitHeight(50);
		homeButton.setGraphic(homeImage);
		homeButton.getStyleClass().add("bottom_format");
		homeButton.setOnAction(e -> {
			soundService.playSfx("click");
			Player.resetPlayerMark();
			Question.questionPointer = 0;
			sceneManager.switchToHome(true);
		});

		root.setBackground(Background.fill(Color.web("#2c3e50")));
		root.getChildren().addAll(vBox, homeButton);

		StackPane.setAlignment(homeButton, Pos.BOTTOM_LEFT);
		StackPane.setMargin(homeButton, new Insets(40));

		// Layout Ratios
		for (int i = 0; i < vBox.getChildren().size(); i++) {
			if (vBox.getChildren().get(i) instanceof Region) {
				((Region) vBox.getChildren().get(i)).prefHeightProperty()
						.bind(vBox.heightProperty().multiply(vBoxProportions[i]));
			}
		}

		// START ANIMATION & DB UPDATE
		runSinglePlayerLogic();
		positionPane();
	}
	private void positionPane() {
		StackPane.setAlignment(homeButton, Pos.BOTTOM_LEFT);
		StackPane.setMargin(homeButton, new Insets(30));
	}
	private void runSinglePlayerLogic() {
		firstProgressBar.setStyle("-fx-accent: #27ae60;");

		ImageView[] animationFrames = {
				new ImageView(new Image("images/result/eaistein.png")),
				new ImageView(new Image("images/result/newton.png")),
				new ImageView(new Image("images/result/leo.png"))
		};

		Task<Void> resultTask = new Task<>() {
			@Override
			protected Void call() throws InterruptedException {
				int finalScore = Player.fPlayerMark;

				// 1. Animation Loop
				for (int i = 0; i <= playerManager.getMark() * 10; i++) {
					updateProgress(i, 100);
					final String currentText = String.valueOf(i / 10);
					final int frameIndex = (i / 5) % 3;

					Platform.runLater(() -> {
						fMark.setText(currentText + "/10");
						fImageSlot.getChildren().clear();
						fImageSlot.getChildren().add(animationFrames[frameIndex]);
					});
					Thread.sleep(40);
				}

				// 2. Database Update (Persistent)
				// 2. Database Update (Persistent)

				// 3. UI Finalization
				Platform.runLater(() -> {
					labelText(fLabel);
					fLabel.setText(calculateFeedback(playerManager.getMark()));
					fImageSlot.getChildren().clear();
					fImageSlot.getChildren().add(new ImageView(new Image("images/result/eaistein.png")));
				});
				return null;
			}
		};

		firstProgressBar.progressProperty().bind(resultTask.progressProperty());
		fMark.setFont(Font.font("Arial", 40));
		fMark.setStyle("-fx-font-weight:bold; -fx-text-fill: white;");
		StackPane.setAlignment(fMark, Pos.TOP_CENTER);

		Thread thread = new Thread(resultTask);
		thread.setDaemon(true);
		thread.start();
	}

	private String calculateFeedback(int score) {
		if (score >= 9) return EXCELLENT;
		if (score >= 7) return GOOD;
		if (score >= 5) return KEEP_IT_UP;
		return TRY_HARDER;
	}

	private void labelText(Label label) {
		Font label_small_font = Font.loadFont(getClass().getResourceAsStream(UiConstant.NOTO_REGULAR_PATH), 30);
		label.setFont(label_small_font);
		label.setTextFill(Color.WHITE);
		label.setStyle("-fx-font-weight: bold;");
	}

	private void responsive() {
		fTextSlot.prefWidthProperty().bind(firstSlot.widthProperty().divide(4));
		fImageSlot.prefWidthProperty().bind(firstSlot.widthProperty().divide(4));
		fProgressSlot.prefWidthProperty().bind(firstSlot.widthProperty().multiply(2).divide(4));

		firstProgressBar.prefWidthProperty().bind(fProgressSlot.widthProperty().multiply(0.9));
		firstProgressBar.prefHeightProperty().bind(fProgressSlot.heightProperty().multiply(0.2));
	}

	public Scene getScene() {
		return scene;
	}
}