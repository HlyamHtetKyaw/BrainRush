package com.union.brainrush.ui;

import com.union.brainrush.service.PlayerManager;
import com.union.brainrush.service.SoundService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.union.brainrush.service.Player;
import com.union.brainrush.service.PortCommunication;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

@Component
public class Home {
	// Root and Scene
	public StackPane root;
	Scene scene;

	// Create the StackPane (rightmost part)
	StackPane rightPane;
	ImageView infoImage;

	// Create the VBox (middle part)
	VBox vBox;

	// Create the StackPane (leftmost part)
	StackPane leftPane;
	ImageView uniImage;
	Button setting;
	ImageView buttonImage;
	
	// Create top slot (titleImage)
	ImageView titleImage;
	StackPane imagelayout;

	// Create second slot (Label)
	Label label;
	Font label_small_font;

	// Create third slots (for 3 Buttons)
	StackPane stackpane1;
	StackPane stackpane2;
	StackPane stackpane3;
	Font button_small_font;

	// 3 Buttons
	Button firstPlayerButton;
	Button secondPlayerButton;
	Button thirdPlayerButton;
	
	@Autowired
	@Lazy
	TransitionState transitionState;
	
	@Autowired
	@Lazy
	Setting settingWindow;

	@Autowired
	private PlayerManager playerManager;

	@Autowired
	private ManagementHub managementHub;

	@Autowired
	private SoundService soundService;

	PortCommunication pc = new PortCommunication();
	
	@Autowired
	public Home() {
		
		// Right Layout
		rightPart();

		// Middle Layout
		middlePart();

		// Buttons Action
		firstPlayerButton.setOnAction(e -> {
			soundService.playSfx("click");
			buttonsAction(1);
		});
		secondPlayerButton.setOnAction(e -> {
			buttonsAction(2);
		});
		thirdPlayerButton.setOnAction(e -> {
			buttonsAction(3);
		});
		// Left Layout
		leftPart();

		// Create the root StackPane
		root = new StackPane(rightPane, vBox, leftPane);
		scene = new Scene(root, UiConstant.WIDTH, UiConstant.HEIGHT);
		scene.getStylesheets().add("css/style.css");
		rightPane.maxWidthProperty().bind(scene.widthProperty().divide(3));
		vBox.maxWidthProperty().bind(scene.widthProperty().divide(3));
		leftPane.maxWidthProperty().bind(scene.widthProperty().divide(3));
		
		// Responsive
		responsive();
		smallImageSize();
	}

	private void rightPart() {
		// Rightmost part
		rightPane = new StackPane();
		StackPane.setAlignment(rightPane, Pos.CENTER_RIGHT);

		// Create info image
		infoImage = new ImageView(new Image("images/home/infoImage.png"));
		rightPane.getChildren().add(infoImage);
	}

	private void middlePart() {
		// Create the VBox
		vBox = new VBox();

		// Set the VBox's height ratios
		double[] proportions = { 3.0 / 10, 1.0 / 10, 2.0 / 10, 2.0 / 10, 2.0 / 10 };

		// Create title image
		titleImage = new ImageView(new Image("images/home/title/BrainRushTitle.png"));

		// Create top slot (titleImage)
		imagelayout = new StackPane();
		imagelayout.getChildren().add(titleImage);

		// Create second slot (Label)
		label = new Label("ကစားဖို့အသင့်ပဲလား...?");
		label_small_font = Font.loadFont(getClass().getResourceAsStream(UiConstant.NOTO_REGULAR_PATH), 25);
		label.setFont(label_small_font);
		label.setStyle("-fx-alignment: center;");
		label.setMaxWidth(Double.MAX_VALUE);
		label.setMaxHeight(Double.MAX_VALUE);

		// Create third slots (for 3 Buttons)
		stackpane1 = new StackPane();
		stackpane2 = new StackPane();
		stackpane3 = new StackPane();

		// 3 Buttons
		firstPlayerButton = new Button("စတင်မယ်");
		secondPlayerButton = new Button("နှစ်ယောက်");
		thirdPlayerButton = new Button("သုံးယောက်");
		secondPlayerButton.setDisable(true);
		thirdPlayerButton.setDisable(true);
		// Font for buttons
		button_small_font = Font.loadFont(getClass().getResourceAsStream(UiConstant.NOTO_REGULAR_PATH), 19);

		// Color for buttons
		firstPlayerButton.setStyle("-fx-background-color:#f97e04;");
		secondPlayerButton.setStyle("-fx-background-color:#a7a3a8");
		thirdPlayerButton.setStyle("-fx-background-color:#34724a");

		// Looping array
		StackPane[] stackpanes = { stackpane1};
		Button[] buttons = { firstPlayerButton};

		int index = 0;
		for (StackPane stackpane : stackpanes) {
			buttons[index].setTextFill(Color.WHITE);
			buttons[index].setFont(button_small_font);
			buttons[index].getStyleClass().add("bottom_format");
			stackpane.getChildren().add(buttons[index]);

			// Setting width and height of stackpane layout
			stackpane.setMaxWidth(Double.MAX_VALUE);
			stackpane.setMaxHeight(Double.MAX_VALUE);

			// Setting width and height of buttons
			buttons[index].prefWidthProperty().bind(stackpane.widthProperty().multiply(3).divide(5));
			buttons[index].prefHeightProperty().bind(stackpane.heightProperty().multiply(2).divide(5));

			index++;
		}

		// Add children to VBox
		vBox.getChildren().addAll(imagelayout, label, stackpane1, stackpane2, stackpane3);

		// Bind the height of each child to the proportions of the VBox
		// Bind heights to proportions
		for (int i = 0; i < vBox.getChildren().size(); i++) {
			if (vBox.getChildren().get(i) instanceof Region) { // Only bind for nodes that are Regions
				((Region) vBox.getChildren().get(i)).prefHeightProperty()
						.bind(vBox.heightProperty().multiply(proportions[i]));
			}
		}

	}

	private void leftPart() {
		// Leftmost part
		leftPane = new StackPane();
		StackPane.setAlignment(leftPane, Pos.CENTER_LEFT);
		setting = new Button();
		
		StackPane.setAlignment(setting, Pos.BOTTOM_LEFT);
		StackPane.setMargin(setting, new Insets(50));
		buttonImage = new ImageView(new Image("images/home/setting.png"));
		buttonImage.setFitWidth(50);
		buttonImage.setFitHeight(50);
		setting.setOnAction(e->{
			soundService.playSfx("click");
			managementHub.show(root);
		});
		setting.setGraphic(buttonImage);
		setting.setMaxSize(50, 50);
		setting.getStyleClass().add("bottom_format");
		// Create uni image
		uniImage = new ImageView(new Image("images/home/uniImage.png"));
		leftPane.getChildren().addAll(uniImage,setting);

	}

	private void smallImageSize() {
		infoImage.setFitHeight(170);
		infoImage.setFitWidth(170);
		titleImage.setFitHeight(200);
		titleImage.setFitWidth(300);
		uniImage.setFitHeight(150);
		uniImage.setFitWidth(120);
	}
	private void buttonsAction(int mode) {
		Player.playerQuantity = mode;
		playerManager.startNewSession();
		if(mode==1) {
			Player.sentMessage="PC:{choice:True1}";
		}
		if(mode==2) {
			Player.sentMessage="PC:{choice:True2}";
		}
		if(mode==3) {
			Player.sentMessage="PC:{choice:True3}";
		}
	    transitionState.showTransitionState("အဆင်သင့်ပြင်ထားနော်",root,true,true);
	}

	private void responsive() {
		scene.widthProperty().addListener((observable, oldValue, newValue) -> {
			double width = newValue.doubleValue();

			if (width > 1024 && width <= 1440) {
				// Medium Screen

				// InfoImage transform
				infoImage.setFitHeight(220);
				infoImage.setFitWidth(220);

				// TitleImage transform
				StackPane.setMargin(titleImage, new Insets(0, 60, 0, 0));
				titleImage.setFitHeight(250);
				titleImage.setFitWidth(350);

				// Label font size
				Font medium_font = Font.loadFont(getClass().getResourceAsStream(UiConstant.NOTO_REGULAR_PATH), 30);
				label.setFont(medium_font);

				// UniImage transform
				uniImage.setFitHeight(200);
				uniImage.setFitWidth(170);

			} else if (width > 1440) {
				// Large Screen

				// TitleImage transform
				titleImage.setFitHeight(300);
				titleImage.setFitWidth(400);
			} else {
				// Small Screen
				smallImageSize();
			}
		});
	}

	public Scene getScene() {
		return scene;
	}
}
