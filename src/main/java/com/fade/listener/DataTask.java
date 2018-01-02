package com.fade.listener;

import java.util.TimerTask;

import javax.servlet.ServletContext;

import org.apache.log4j.Logger;

public class DataTask extends TimerTask {

	private Logger logger;
	private ServletContext servletContext;
	
	public DataTask(ServletContext servletContext, Logger logger) {
		this.logger = logger;
		this.servletContext = servletContext;
	}
	
	@Override
	public void run() {
		 

	}

}
