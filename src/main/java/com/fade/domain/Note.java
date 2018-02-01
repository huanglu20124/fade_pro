package com.fade.domain;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import javax.jws.soap.SOAPBinding.Use;

public class Note implements Serializable {
	/**
	 * 帖子
	 * 如果是转发贴（target_id不为0的话，显示的续秒数、减秒数、评论数都是原贴origin的）
	 */
	private static final long serialVersionUID = 3337872547982353088L;
	private Integer note_id;
	private Integer user_id;
	private String nickname;
	private String head_image_url;
	private String note_content;
	private String post_time;       //发布时间
	private Integer is_die;         //是否死亡
	private Integer comment_num;    //评论数量
	private Integer sub_num;        //减一秒数量
	private Integer add_num;        //增一秒数量
	private Integer target_id;      //代表原贴id（不为0的话）
	private Integer type;           //代表帖子是增/减/原创, 1/2/0
	private Integer action;         //动作，0为没动作，1为对这个帖子增过，0为对这个帖子减过
	private Long fetchTime;//给客户端的此刻时间
	private List<Image> images;     // 图片集合
	private Note origin;// 原贴

	private String note_area; //地理位置
	
	private Integer baseComment_num; //一级评论数量，后端缓存判断用到，数据库不保存
	
	private String exampleImage;//示例图片
	
	private String uuid;//后端索引用到的
	
	private Long liveTime;//存活时间，死贴专有属性
	
	//转发帖具有的属性
	private List<User>addUsers;//续秒用户
	
	private List<User>subUsers;//减秒用户
	
	
	
	public List<User> getAddUsers() {
		return addUsers;
	}

	public void setAddUsers(List<User> addUsers) {
		this.addUsers = addUsers;
	}

	public List<User> getSubUsers() {
		return subUsers;
	}

	public void setSubUsers(List<User> subUsers) {
		this.subUsers = subUsers;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getExampleImage() {
		return exampleImage;
	}

	public void setExampleImage(String exampleImage) {
		this.exampleImage = exampleImage;
	}

	public String getNote_area() {
		return note_area;
	}

	public void setNote_area(String note_area) {
		this.note_area = note_area;
	}

	public Note() {
		this.comment_num = 0;
		this.add_num = 0;
		this.sub_num = 0;
		this.target_id = 0;
		this.type = 0;
		this.action = 0;
		this.baseComment_num = 0;
		this.is_die = 1;
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

	public Integer getBaseComment_num() {
		return baseComment_num;
	}

	public void setBaseComment_num(Integer baseComment_num) {
		this.baseComment_num = baseComment_num;
	}

	
	public Long getFetchTime() {
		return fetchTime;
	}

	public void setFetchTime(Long fetchTime) {
		this.fetchTime = fetchTime;
	}

	
	public Long getLiveTime() {
		return liveTime;
	}

	public void setLiveTime(Long liveTime) {
		this.liveTime = liveTime;
	}

	//如果note_id相等，则两个帖子相等
	@Override
	public int hashCode() {
		return Objects.hash(note_id.intValue());
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == this) return true; 
		if(!(obj instanceof Note)) return false;	
		Note note = (Note) obj;
		return note_id.equals(note.getNote_id());
	}
}
