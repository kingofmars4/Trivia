package me.kingofmars4.trivia;

public class Question {
	
	private String question;
	private String answer;
	private double reward;
	
	public Question(String question, String answer, double reward) {
		this.question = question;
		this.answer = answer;
		this.reward = reward;
	}

	public String getQuestion() {
		return question;
	}
	public String getAnswer() {
		return answer;
	}


	public double getReward() {
		return reward;
	}
	
	

}
