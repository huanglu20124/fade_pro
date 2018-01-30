package com.fade.domain;

public class CommentMessage {
	//消息通知类
	private Integer message_id;
	private String comment_content;
	private Integer from_id;//对方id
	private String from_nickname;//对方名字
	private String from_head;//对方头像
	private Integer to_id;//我的id
	private Integer note_id;//所属帖子id
	private String note_content;
	private String exampleImage;
	private String comment_time;
	private Integer comment_id;//若是一级评论，则不为空
	private Integer second_id;//若是二级评论，则不为空
	//通过判断comment_id和second_id哪个为空判断是哪级评论
	//2级评论要加“回复了你”四个字
	
	
	public Integer getMessage_id() {
		return message_id;
	}
	public void setMessage_id(Integer message_id) {
		this.message_id = message_id;
	}
	public String getComment_content() {
		return comment_content;
	}
	public void setComment_content(String comment_content) {
		this.comment_content = comment_content;
	}
	public Integer getFrom_id() {
		return from_id;
	}
	public void setFrom_id(Integer from_id) {
		this.from_id = from_id;
	}
	public String getFrom_nickname() {
		return from_nickname;
	}
	public void setFrom_nickname(String from_nickname) {
		this.from_nickname = from_nickname;
	}
	public String getFrom_head() {
		return from_head;
	}
	public void setFrom_head(String from_head) {
		this.from_head = from_head;
	}
	public Integer getTo_id() {
		return to_id;
	}
	public void setTo_id(Integer to_id) {
		this.to_id = to_id;
	}
	public String getNote_content() {
		return note_content;
	}
	public void setNote_content(String note_content) {
		this.note_content = note_content;
	}
	public String getExampleImage() {
		return exampleImage;
	}
	public void setExampleImage(String exampleImage) {
		this.exampleImage = exampleImage;
	}
	public String getComment_time() {
		return comment_time;
	}
	public void setComment_time(String comment_time) {
		this.comment_time = comment_time;
	}
	public Integer getNote_id() {
		return note_id;
	}
	public void setNote_id(Integer note_id) {
		this.note_id = note_id;
	}
	public Integer getComment_id() {
		return comment_id;
	}
	public void setComment_id(Integer comment_id) {
		this.comment_id = comment_id;
	}
	public Integer getSecond_id() {
		return second_id;
	}
	public void setSecond_id(Integer second_id) {
		this.second_id = second_id;
	}
	
	
	
}
