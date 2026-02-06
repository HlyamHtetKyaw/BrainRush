package com.union.brainrush.ui;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.union.brainrush.routing.SceneManager;
import com.union.brainrush.service.Player;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Duration;

@Component
@Lazy
public class TransitionState {
	private StackPane overlayPane;
	private VBox vBox;
	private StackPane upperLayout, middleLayout, underLayout;
	private HBox hBox;

	// Player slots referencing the views in UiConstant
	private ImageView[] playerViews = { UiConstant.fpV, UiConstant.spV, UiConstant.tpV };

	private Label counter;
	private Font counter_small_font;
	private Timeline timeline;
	private int remain_counter;

	private Label announcedLabel;
	private Font label_small_font;

	@Autowired
	private SceneManager sceneManager;

	@Autowired
	private Question questionState;

	/**
	 * @param announcedString The text to display (e.g., "Correct!" or "Wrong!")
	 * @param root The parent StackPane to attach the overlay to
	 * @param isNewRound Whether to initialize a new set of questions
	 * @param isCorrect Whether the player got the answer right (for image swapping)
	 */
	public void showTransitionState(String announcedString, StackPane root, boolean isNewRound, boolean isCorrect) {
		remain_counter = 1;

		vBox = new VBox();
		upperLayout = new StackPane();
		middleLayout = new StackPane();
		underLayout = new StackPane();

		// --- DYNAMIC IMAGE LOGIC ---
		if (isCorrect) {
			UiConstant.fpV.setImage(UiConstant.firstPlayerConfirm);
			UiConstant.spV.setImage(UiConstant.secondPlayerConfirm);
			UiConstant.tpV.setImage(UiConstant.thirdPlayerConfirm);
		} else {
			UiConstant.fpV.setImage(UiConstant.firstPlayer);
			UiConstant.spV.setImage(UiConstant.secondPlayer);
			UiConstant.tpV.setImage(UiConstant.thirdPlayer);
		}

		hBox = new HBox();
		hBox.setSpacing(20);
		hBox.setAlignment(Pos.BOTTOM_CENTER);
		for (int i = 0; i < Player.playerQuantity; i++) {
			hBox.getChildren().add(playerViews[i]);
		}
		upperLayout.getChildren().add(hBox);

		// Counter Setup
		counter = new Label("၃");
		counter_small_font = Font.loadFont(getClass().getResourceAsStream(UiConstant.NOTO_REGULAR_PATH), 40);
		counter.setFont(counter_small_font);
		counter.setAlignment(Pos.CENTER);
		counter.setStyle("-fx-background-color: white; -fx-background-radius: 100;"); // Circle
		counter.setMaxSize(200, 200);

		// Countdown logic
		String[] counterText = { "၁", "၂" };
		timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
			if (remain_counter == -1) {
				UiConstant.WIDTH = (int) overlayPane.getWidth();
				UiConstant.HEIGHT = (int) overlayPane.getHeight();

				// Initialize next state
				questionState.questionState(isNewRound, overlayPane.getWidth(), overlayPane.getHeight());
				sceneManager.switchToQuestion();

				// Remove overlay before leaving
				root.getChildren().remove(overlayPane);
			} else {
				counter.setText(counterText[remain_counter]);
				remain_counter--;
			}
		}));
		timeline.setCycleCount(3);
		timeline.play();
		middleLayout.getChildren().add(counter);

		// Announced Text
		announcedLabel = new Label(announcedString);
		announcedLabel.setTextFill(Color.WHITE);
		label_small_font = Font.loadFont(getClass().getResourceAsStream(UiConstant.NOTO_REGULAR_PATH), 25);
		announcedLabel.setFont(label_small_font);
		StackPane.setAlignment(announcedLabel, Pos.TOP_CENTER);
		underLayout.getChildren().add(announcedLabel);

		// Layout bindings
		vBox.getChildren().addAll(upperLayout, middleLayout, underLayout);
		double[] proportions = { 0.325, 0.35, 0.325 };
		for (int i = 0; i < vBox.getChildren().size(); i++) {
			if (vBox.getChildren().get(i) instanceof Region) {
				((Region) vBox.getChildren().get(i)).prefHeightProperty()
						.bind(vBox.heightProperty().multiply(proportions[i]));
			}
		}

		overlayPane = new StackPane();
		overlayPane.setBackground(Background.fill(Color.rgb(0, 0, 0, 0.8)));
		overlayPane.getChildren().add(vBox);
		vBox.maxWidthProperty().bind(overlayPane.widthProperty().divide(3));

		root.getChildren().add(overlayPane);
		responsive();
	}

	private void responsive() {
		overlayPane.widthProperty().addListener((obs, oldVal, newVal) -> {
			double width = newVal.doubleValue();
			if (width > 1440) {
				label_small_font = Font.loadFont(getClass().getResourceAsStream(UiConstant.NOTO_REGULAR_PATH), 30);
				announcedLabel.setFont(label_small_font);
				counter.setScaleX(1.5); counter.setScaleY(1.5);
			} else {
				label_small_font = Font.loadFont(getClass().getResourceAsStream(UiConstant.NOTO_REGULAR_PATH), 25);
				announcedLabel.setFont(label_small_font);
				counter.setScaleX(1.0); counter.setScaleY(1.0);
			}
		});
	}
}