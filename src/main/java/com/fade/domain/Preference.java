package com.fade.domain;

public class Preference {
	private Integer user_id;
	private Integer note_id;
	private Double score;
	private String timestamp;
	
	public Preference() {
		// TODO Auto-generated constructor stub
	}
	
	public Preference(Integer user_id, Integer note_id, Double score) {
		this.score = score;
		this.user_id = user_id;
		this.note_id = note_id;
	}	
	
	public Integer getUser_id() {
		return user_id;
	}
	public void setUser_id(Integer user_id) {
		this.user_id = user_id;
	}
	public Integer getNote_id() {
		return note_id;
	}
	public void setNote_id(Integer note_id) {
		this.note_id = note_id;
	}
	public Double getScore() {
		return score;
	}

	public void setScore(Double score) {
		this.score = score;
	}

	public String getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}
	
	
}
