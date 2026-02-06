package com.union.brainrush.model;

import java.util.HashMap;

import jakarta.persistence.*;
import java.util.Map;

@Entity
@Table(name = "questions")
public class QuestionFormat {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	private String question;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "question_answers", joinColumns = @JoinColumn(name = "question_id"))
	@MapKeyColumn(name = "answer_key", nullable = false)
	@Column(name = "answer_value")
	private Map<String, String> ans;

	private String rightAns;

	public QuestionFormat() {}

	public int getId() { return id; }
	public void setId(int id) { this.id = id; }

	public String getQuestion() { return question; }
	public void setQuestion(String question) { this.question = question; }

	public Map<String, String> getAns() { return ans; }
	public void setAns(Map<String, String> ans) { this.ans = ans; }

	public String getRightAns() { return rightAns; }
	public void setRightAns(String rightAns) { this.rightAns = rightAns; }
}
