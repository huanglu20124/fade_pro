package com.fade.domain;

import java.io.Serializable;

public class Note implements Serializable{


	/**
	 * 
	 */
	private static final long serialVersionUID = 3337872547982353088L;
	private Integer note_id;
	private Integer user_id;
	private String nickname;
	private String head_image_url;
	
	private String note_content;
	private String post_time;
	private Integer isDie_fans;
	private Integer isDie_stranger;
	
	private Integer comment_num;
	private Integer relay_num;
	private Integer good_num;
	
	private Integer isRelay;
	
	private String post_area;

	

	public String getPost_area() {
		return post_area;
	}

	public void setPost_area(String post_area) {
		this.post_area = post_area;
	}

	public Integer getNote_id() {
		return note_id;
	}

	public void setNote_id(Integer note_id) {
		this.note_id = note_id;
	}

	public Integer getUser_id() {
		return user_id;
	}

	public void setUser_id(Integer user_id) {
		this.user_id = user_id;
	}

	public Integer getIsRelay() {
		return isRelay;
	}

	public void setIsRelay(Integer isRelay) {
		this.isRelay = isRelay;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getHead_image_url() {
		return head_image_url;
	}

	public void setHead_image_url(String head_image_url) {
		this.head_image_url = head_image_url;
	}

	public String getNote_content() {
		return note_content;
	}

	public void setNote_content(String note_content) {
		this.note_content = note_content;
	}

	public String getPost_time() {
		return post_time;
	}

	public void setPost_time(String post_time) {
		this.post_time = post_time;
	}

	public Integer getIsDie_fans() {
		return isDie_fans;
	}

	public void setIsDie_fans(Integer isDie_fans) {
		this.isDie_fans = isDie_fans;
	}

	public Integer getIsDie_stranger() {
		return isDie_stranger;
	}

	public void setIsDie_stranger(Integer isDie_stranger) {
		this.isDie_stranger = isDie_stranger;
	}

	public Integer getComment_num() {
		return comment_num;
	}

	public void setComment_num(Integer comment_num) {
		this.comment_num = comment_num;
	}

	public Integer getRelay_num() {
		return relay_num;
	}

	public void setRelay_num(Integer relay_num) {
		this.relay_num = relay_num;
	}

	public Integer getGood_num() {
		return good_num;
	}

	public void setGood_num(Integer good_num) {
		this.good_num = good_num;
	}


	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "note_id="+note_id
				+" note_content="+note_content
				+" nickname="+ nickname;
	}
}
