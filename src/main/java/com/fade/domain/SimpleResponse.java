package com.fade.domain;

import java.util.Map;

public class SimpleResponse {
	private String success;
	private String err;
	private Map<String, Object> extra; //多余的信息以json格式放到这里面
	
	public SimpleResponse() {
	}
	
	public SimpleResponse(String success,String err) {
		this.err = err;
		this.success = success;
	}
	
	public SimpleResponse(String success,String err,Map<String, Object>extra) {
		this.err = err;
		this.success = success;
		this.extra = extra;
	}	
	
	
	public String getSuccess() {
		return success;
	}
	public void setSuccess(String success) {
		this.success = success;
	}
	public String getErr() {
		return err;
	}
	public void setErr(String err) {
		this.err = err;
	}

	public Map<String, Object> getExtra() {
		return extra;
	}

	public void setExtra(Map<String, Object> extra) {
		this.extra = extra;
	}


	
	
}
