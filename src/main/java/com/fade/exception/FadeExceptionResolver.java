package com.fade.exception;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSON;
import com.fade.domain.ErrorMessage;

public class FadeExceptionResolver implements HandlerExceptionResolver {

	@Override
	public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler,
			Exception ex) {
		try {
			request.setCharacterEncoding("utf-8");
			response.setCharacterEncoding("utf-8");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		// 输出异常
		//ex.printStackTrace();
		//记录异常
		
		// 统一异常处理代码
		// 针对系统自定义的FadeException异常，就可以直接从异常类中获取异常信息，将异常处理在错误页面展示
		FadeException fadeException = null;
		// 如果ex是系统 自定义的异常，直接取出异常信息
		if (ex instanceof FadeException) {
			fadeException = (FadeException) ex;
		} else {
			ErrorMessage errorMessage = new ErrorMessage();
			errorMessage.setErr("服务器未知错误");
			errorMessage.setDescription(ex.getMessage());
			fadeException = new FadeException(errorMessage);
		}
		try {
			//json格式返回
			response.getWriter().write(JSON.toJSONString(fadeException.getErrorMessage()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		//不返回视图，直接json返回
		return null;
	}
	

}
