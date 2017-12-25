package com.fade.domain;

import java.io.Serializable;
import java.util.List;

public class Note implements Serializable {
	/**
	 * 帖子表
	 */
	private static final long serialVersionUID = 3337872547982353088L;
	private Integer note_id;
	private Integer user_id;
	private String nickname;
	private String head_image_url;

	private String note_content;
	private String post_time;
	private Integer is_die;

	private Integer comment_num;
	private Integer sub_num;
	private Integer add_num;
	private Integer target_id;
	private Integer type;//代表帖子是增/减/原创, 1/2/0
	private Integer action;//动作，0为没动作，1为对这个帖子增过，0为对这个帖子减过

	private List<Image> images;// 图片集合

	private Note origin;// 原贴

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

	public Integer getComment_num() {
		return comment_num;
	}

	public void setComment_num(Integer comment_num) {
		this.comment_num = comment_num;
	}

	public Integer getIs_die() {
		return is_die;
	}

	public void setIs_die(Integer is_die) {
		this.is_die = is_die;
	}

	public Integer getSub_num() {
		return sub_num;
	}

	public void setSub_num(Integer sub_num) {
		this.sub_num = sub_num;
	}

	public Integer getAdd_num() {
		return add_num;
	}

	public void setAdd_num(Integer add_num) {
		this.add_num = add_num;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "note_id=" + note_id + " note_content=" + note_content + " nickname=" + nickname;
	}

	public List<Image> getImages() {
		return images;
	}

	public void setImages(List<Image> images) {
		this.images = images;
	}

	public Note getOrigin() {
		return origin;
	}

	public void setOrigin(Note origin) {
		this.origin = origin;
	}

	public Integer getTarget_id() {
		return target_id;
	}

	public void setTarget_id(Integer target_id) {
		this.target_id = target_id;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	
	public Integer getAction() {
		return action;
	}

	
	public void setAction(Integer action) {
		this.action = action;
	}

	
}
