package com.fade.exception;

/**
 * 自定义的异常类
 * @author huanglu
 *
 */
public class FadeException extends Exception {

	private static final long serialVersionUID = 1L;
	//异常信息
	private String errorMessage;
	
	public FadeException(String errorMessage){
		this.errorMessage = errorMessage;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}




	
}
