package com.fade.exception;

import com.fade.domain.ErrorMessage;

/**
 * 自定义的异常类
 * @author huanglu
 *
 */
public class FadeException extends Exception {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	//异常信息
	private ErrorMessage errorMessage;
	
	public FadeException(ErrorMessage errorMessage){
		this.errorMessage = errorMessage;
	}

	public ErrorMessage getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(ErrorMessage errorMessage) {
		this.errorMessage = errorMessage;
	}
	
}
