package com.fade.domain;

public class PushMessage {
	//个推 推送所用的bean
	private Object obj;
	private Integer msgId;//1为续秒通知，2为评论通知，3为粉丝通知
	

	public PushMessage(Object obj, Integer msgId) {
		super();
		this.obj = obj;
		this.msgId = msgId;
	}
	public Object getObj() {
		return obj;
	}
	
	
	public void setObj(Object obj) {
		this.obj = obj;
	}
	public Integer getMsgId() {
		return msgId;
	}
	public void setMsgId(Integer msgId) {
		this.msgId = msgId;
	}
	
	
}
