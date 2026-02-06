package com.union.brainrush.routing;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.union.brainrush.ui.First;
import com.union.brainrush.ui.Home;
import com.union.brainrush.ui.Question;
import com.union.brainrush.ui.Result;

import javafx.stage.Stage;

@Component
public class SceneManager {
	private static Stage stage;
	private Home home;
	private First first;
	private Question question;
	private Result result;
	@Autowired
	SceneManager(Home home,First first,@Lazy Question question,@Lazy Result result) {
		this.home = home;
		this.first = first;
		this.question = question;
		this.result = result;
	}

	public static void initialize(Stage primaryStage) {
		stage = primaryStage;
	}

	public void switchToHome(boolean check) {
//		if(check) {
//			home.root.getChildren().remove(home.root.getChildren().size() - 1);
//		}
		stage.setScene(home.getScene());
	}
	public void switchToFirst() {
		stage.setScene(first.getScene());
	}
	public void switchToQuestion() {
		stage.setScene(question.getScene());
	}
	public void switchToResult() {
		result.initResultState();stage.setScene(result.getScene());
	}
}

