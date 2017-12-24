package com.fade.domain;

import java.io.Serializable;

public class Image implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -8529188924117202996L;
	/**
	 * 图片表
	 */
	private Integer image_id;
	private String image_url;
	private Integer note_id;
	private String image_size;       //宽高比
	private String image_coordinate; //图片左上角展示中心点的坐标
	private String image_cut_size;   //图片展示比例
	
	public Integer getImage_id() {
		return image_id;
	}
	public void setImage_id(Integer image_id) {
		this.image_id = image_id;
	}
	public String getImage_url() {
		return image_url;
	}
	public void setImage_url(String image_url) {
		this.image_url = image_url;
	}
	public Integer getNote_id() {
		return note_id;
	}
	public void setNote_id(Integer note_id) {
		this.note_id = note_id;
	}
	public String getImage_size() {
		return image_size;
	}
	public void setImage_size(String image_size) {
		this.image_size = image_size;
	}
	public String getImage_coordinate() {
		return image_coordinate;
	}
	public void setImage_coordinate(String image_coordinate) {
		this.image_coordinate = image_coordinate;
	}
	public String getImage_cut_size() {
		return image_cut_size;
	}
	public void setImage_cut_size(String image_cut_size) {
		this.image_cut_size = image_cut_size;
	}
	
	
}
