package com.union.brainrush;

import com.union.brainrush.service.SoundService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import com.union.brainrush.routing.SceneManager;
import javafx.application.Application;
import javafx.stage.Stage;

@SpringBootApplication
public class BrainRushApplication extends Application {

	private ConfigurableApplicationContext springContext;

	@Override
	public void init() {
		springContext = SpringApplication.run(BrainRushApplication.class);

		SoundService soundService = springContext.getBean(SoundService.class);

		soundService.playBgSound();
	}

	@Override
	public void start(Stage primaryStage) {
		SceneManager.initialize(primaryStage);

		springContext.getBean(SceneManager.class).switchToHome(false);

		primaryStage.setTitle("Brain Rush");
		primaryStage.show();
	}

	@Override
	public void stop() {
		if (springContext != null) {
			springContext.close();
		}
	}

	public static void main(String[] args) {
		launch(args);
	}
}