package com.hl.test;


import java.util.List;

import javax.annotation.Resource;

import org.junit.Test;

import com.fade.domain.Note;
import com.fade.domain.User;
import com.fade.mapper.NoteDao;
import com.fade.mapper.UserDao;
import com.fade.service.NoteService;
import com.fade.service.UserService;
import com.fade.util.RedisUtil;
import com.fade.util.TimeUtil;
import com.fade.util.TokenUtil;

public class UserTest extends BaseTest {
	@Resource(name="userService")
	private UserService userService;
	
	@Resource(name="userDao")
	private UserDao userDao;

	@Resource(name = "noteDao")
	private NoteDao noteDao;
	
	@Resource(name = "tokenUtil")
	private TokenUtil tokenUtil;
	
	@Resource(name = "redisUtil")
	private RedisUtil redisUtil;
	
	@Resource(name = "noteService")
	private NoteService noteService;
	
	@Test
	public void testAdd() throws Exception {
		User user = new User();
		user.setFade_name("test3");
		user.setConcern_num(0);
		user.setFade_num(0);
		user.setFans_num(0);
		user.setRegister_time(com.fade.util.TimeUtil.getCurrentTime());
		userDao.addUser(user);
		System.out.println(user.getUser_id());//自增主键自动设置到对象中
	}

	@Test
	public void testEditNickname() throws Exception {
		User user = new User();
		user.setUser_id(1);
		user.setNickname("aaaa");
		System.out.println(userDao.updateNickname(user));	//返回更新的行数
	}

	@Test
	public void testEditHead() throws Exception {
		System.out.println(userDao.updateHeadUrl("11111", 8));
	}

	@Test
	public void testGetStarUser() throws Exception {
		List<User>users = userDao.getStarUser(1);
		for(User user : users){
			System.out.println(user.getNickname() + "--" + user.getUser_id());
		}
	}
	
	@Test
	public void testGetUserByTelephone() throws Exception {
		User user = userDao.getUserByTel("1233");
		System.out.println(user);
	}
	
	@Test
	public void testList() throws Exception {
		for(int i = 0; i < 10; i++){
			redisUtil.listLeftPush("test", "note_" + i);
		}
		List<String>list = redisUtil.listGetRange("test", 0l, -1l);
		for(String temp : list){
			System.out.println(temp);
		}
	}
	
	@Test
	public void testgetMuchNoteId() throws Exception {
		List<Integer>list = noteDao.getMuchNoteId(16, 83);
		System.out.println(list.get(0));
		System.out.println(list);
	}
	
	@Test
	public void testAddNote() throws Exception {
		for(int i = 0; i < 25; i++){
			Note note = new Note();
			if(i < 10) note.setUser_id(16);
			else if (i < 20) {
				note.setUser_id(29);
			}
			else {
				note.setUser_id(30);
			}
			note.setNote_content("内容测试"+i);
			note.setPost_time(TimeUtil.getCurrentTime());
			noteService.addNote(note, null);
		}
	}

	@Test
	public void getTenNoteByTime() throws Exception {
		System.out.println(noteService.getTenNoteByTime(16, 0));
	}
}
