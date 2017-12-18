package com.fade.domain;

public class TokenModel {
	private Integer user_id;//用于找到登录用户的队列,key格式为"user_"+user_id。同时又作为token的value
	private String token;//登录后唯一生成，作为秘钥，作为单个value
	
	public TokenModel(Integer user_id, String token) {
		this.token = token;
		this.user_id = user_id;
	}

	public Integer getUser_id() {
		return user_id;
	}

	public void setUser_id(Integer user_id) {
		this.user_id = user_id;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}
	
	
}
