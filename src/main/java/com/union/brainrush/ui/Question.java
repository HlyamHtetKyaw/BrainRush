package com.union.brainrush.ui;

import com.union.brainrush.model.QuestionFormat;
import com.union.brainrush.routing.SceneManager;
import com.union.brainrush.service.Player;
import com.union.brainrush.service.PlayerManager;
import com.union.brainrush.service.QuestionService;
import com.union.brainrush.service.SoundService;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
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
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;
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
	// Game Settings
	public static int questionPointer = 0;
	public static int totalQuestion = 10;
	private final int TIME_LIMIT = 7; // 5 Seconds Timer

	private Scene scene;
	public static StackPane root;
	private final List<String> bgColor = new ArrayList<>(Arrays.asList(
			"#ed801b", "#004aad", "#ebcd35", "#b64747", "#3fa278",
			"#f7c89f", "#4b6240", "#d48e38", "#623f31", "#684f9b"
	));

	// UI Components
	private StackPane questionPane, answerPane, qUpperLayout, qBottomLayout;
	public static Button actionButton;
	private Button homeButton;
	private HBox playerHBox;
	private VBox answersVBox;
	private TextFlow textFlow;

	// Timer Components
	private Label timerLabel;
	private Timeline questionTimer;

	// Fonts
	private final Font burmeseFont = Font.loadFont(getClass().getResourceAsStream(UiConstant.NOTO_REGULAR_PATH), 24);
	private final Font englishFont = Font.font("Arial", 22);

	// Services & Data
	private final QuestionService questionService;
	private List<QuestionFormat> questionArray;
	private QuestionFormat currentQuestion;

	@Autowired @Lazy TransitionState transitionState;
	@Autowired SceneManager sceneManager;
	@Autowired private PlayerManager playerManager;
	@Autowired private SoundService soundService;

	public static ImageView[] playerSlot = new ImageView[3];

	@Autowired
	public Question(QuestionService questionService) {
		this.questionService = questionService;
	}

	/**
	 * Initializes or Updates the Question Scene
	 */
	public void questionState(boolean startNewGame, double width, double height) {
		// 1. Reset answer at the start of every question to detect timeouts later
		Player.fPlayerAns = "";

		// 2. Data Initialization (Only fetch if starting new game)
		if (startNewGame || questionArray == null || questionArray.isEmpty()) {
			this.questionArray = questionService.getRandomRound(totalQuestion);

			if (questionArray.isEmpty()) {
				System.err.println("No questions found in database!");
				return;
			}
			Collections.shuffle(questionArray);
			Collections.shuffle(bgColor);
			questionPointer = 0;
		}

		// 3. Safety Check: If out of bounds, go to result
		if (questionPointer >= questionArray.size()) {
			sceneManager.switchToResult();
			return;
		}

		currentQuestion = questionArray.get(questionPointer);
		root = new StackPane();
		HBox mainLayout = new HBox();

		// 4. UI Layout Setup
		questionPane = new StackPane();
		qUpperLayout = new StackPane();
		qBottomLayout = new StackPane();

		homeButton = new Button();
		ImageView homeImage = new ImageView(new Image("images/home/home.png"));
		homeImage.setFitWidth(50);
		homeImage.setFitHeight(50);
		homeButton.setGraphic(homeImage);
		homeButton.getStyleClass().add("bottom_format");
		homeButton.setOnAction(e -> switchBackToHome());

		// --- TIMER LABEL SETUP ---
		timerLabel = new Label(String.valueOf(TIME_LIMIT));
		timerLabel.getStyleClass().add("timer-label"); // CSS styling
		StackPane.setAlignment(timerLabel, Pos.TOP_CENTER);
		StackPane.setMargin(timerLabel, new Insets(100,0,0,0));
		// ------------------------

		// Player Icons
		playerHBox = new HBox(15);
		playerHBox.setAlignment(Pos.CENTER);
		for (int i = 0; i < Player.playerQuantity; i++) {
			ImageView pv = new ImageView();
			pv.setImage(i==0 ? UiConstant.firstPlayer : i==1 ? UiConstant.secondPlayer : UiConstant.thirdPlayer);
			pv.setFitWidth(60);
			pv.setPreserveRatio(true);
			playerHBox.getChildren().add(pv);
			playerSlot[i] = pv;
		}

		actionButton = new Button("Next");
		actionButton.setVisible(false);
		actionButton.setOnAction(e -> nextQuestion());

		// Add Timer to Layout
		qUpperLayout.getChildren().addAll(playerHBox, actionButton, timerLabel);

		Player.rightAns = currentQuestion.getRightAns();
		textFlow = createTextFlow(currentQuestion.getQuestion());
		textFlow.getStyleClass().add("question_text_flow");

		VBox questionCenterer = new VBox(textFlow);
		questionCenterer.setAlignment(Pos.CENTER);
		qBottomLayout.getChildren().add(questionCenterer);

		questionPane.getChildren().addAll(qUpperLayout, qBottomLayout);

		// Answer Buttons
		answerPane = new StackPane();
		answersVBox = new VBox(25);
		answersVBox.setAlignment(Pos.CENTER_LEFT);
		answersVBox.setPadding(new Insets(0, 0, 0, 30));

		String[] keys = {"A", "B", "C"};
		for (String key : keys) {
			HBox row = new HBox(20);
			row.setAlignment(Pos.CENTER_LEFT);
			row.getStyleClass().add("answer_row");
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

		// 5. Start the countdown
		startTimer();
	}

	// --- TIMER LOGIC ---
	private void startTimer() {
		if (questionTimer != null) questionTimer.stop();

		final int[] timeSeconds = {TIME_LIMIT};
		timerLabel.setText(String.valueOf(timeSeconds[0]));

		questionTimer = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
			timeSeconds[0]--;
			timerLabel.setText(String.valueOf(timeSeconds[0]));

			if (timeSeconds[0] <= 0) {
				questionTimer.stop();
				handleTimeout();
			}
		}));
		questionTimer.setCycleCount(TIME_LIMIT);
		questionTimer.play();
	}

	private void handleTimeout() {
		// Disable UI
		if(answersVBox != null) answersVBox.setDisable(true);
		// Ensure answer is empty (Timeout indicator)
		Player.fPlayerAns = "";
		// Move next
		actionButton.fire();
	}
	// -------------------

	private void handleAnswerSelection(String choice) {
		// Stop timer on click
		if (questionTimer != null) questionTimer.stop();

		Player.fPlayerAns = choice;
		if (playerSlot[0] != null) {
			playerSlot[0].setImage(UiConstant.firstPlayerConfirm);
		}
		answersVBox.setDisable(true);
		actionButton.fire();
	}

	public void nextQuestion() {
		questionPointer++;

		// GAME OVER CHECK
		if (questionPointer >= totalQuestion || questionPointer >= questionArray.size()) {
			if (Player.rightAns.equalsIgnoreCase(Player.fPlayerAns)) {
				soundService.playSfx("correct");
				playerManager.updateMark(playerManager.getMark()+1);
			} else {
				soundService.playSfx("wrong");
			}
			this.questionArray = null; // Clear list for next game
			sceneManager.switchToResult();
		} else {
			// NEXT QUESTION CHECK
			if (Player.rightAns.equalsIgnoreCase(Player.fPlayerAns)) {
				// Correct Answer
				soundService.playSfx("correct");
				Player.fPlayerMark++;
				playerManager.updateMark(playerManager.getMark()+1);
				transitionState.showTransitionState("မှန်ကန်ပါတယ်!", root, false, true);
			} else {
				// Wrong Answer logic
				soundService.playSfx("wrong");

				// Check if it was a timeout (Answer is empty)
				if (Player.fPlayerAns == null || Player.fPlayerAns.isEmpty()) {
					transitionState.showTransitionState("အချိန်ကုန်သွားပါပြီ!", root, false, false);
				} else {
					transitionState.showTransitionState("မှားယွင်းနေပါတယ်!", root, false, false);
				}
			}
		}
	}

	private void switchBackToHome() {
		if (questionTimer != null) questionTimer.stop(); // Important: Stop timer

		soundService.playSfx("click");
		playerManager.abandonSession();
		questionPointer = 0;
		this.questionArray = null;
		Player.resetPlayerMark();
		sceneManager.switchToHome(true);
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

	public Scene getScene() { return scene; }
}