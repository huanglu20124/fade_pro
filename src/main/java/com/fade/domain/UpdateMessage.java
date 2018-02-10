package com.fade.domain;

public class UpdateMessage {
	//用于更新preference表的类，放在redis中
	
	//执行以下操作的时候，先检查是否有相关记录，没有的话先向数据库插入preference信息
	//1.打开详情页,+0.5
	//2.某人对该帖续或减秒，评论一次+1，
	//3.收藏+2
	private Integer msgId; //用来标识不同的处理
	private Integer note_id;//原贴的id（即原贴id）
	private Integer user_id;//操作者的id
	private Integer owener_id;//原贴作者的id
	
	public UpdateMessage() {
		// TODO Auto-generated constructor stub
	}
	
	public UpdateMessage(Integer msgId, Integer note_id, Integer user_id, Integer owener_id) {
		this.msgId = msgId;
		this.owener_id = owener_id;
		this.user_id = user_id;
		this.note_id = note_id;
	}	
	
	public Integer getUser_id() {
		return user_id;
	}
	public void setUser_id(Integer user_id) {
		this.user_id = user_id;
	}
	public Integer getOwener_id() {
		return owener_id;
	}
	public void setOwener_id(Integer owener_id) {
		this.owener_id = owener_id;
	}
	public Integer getMsgId() {
		return msgId;
	}
	public void setMsgId(Integer msgId) {
		this.msgId = msgId;
	}
	public Integer getNote_id() {
		return note_id;
	}
	public void setNote_id(Integer note_id) {
		this.note_id = note_id;
	}
	
	
}
