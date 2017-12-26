package com.hl.test;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
		User user = userDao.getUserByTel("13763359943");
		System.out.println(user);
	}
	
	@Test
	public void testList() throws Exception {
		redisUtil.listRightPush("test", "note_1");
		redisUtil.listRightPush("test", "note_2");
		redisUtil.listRightPush("test", "note_3");
		redisUtil.listLeftPop("test");
		System.out.println(redisUtil.listGetAll("test"));
	}
		
	@Test
	public void testAddNote() throws Exception {
		for(int i = 0; i < 5; i++){
			Note note = new Note();
			if(i < 3) note.setUser_id(16);
			else note.setUser_id(30);
			note.setNickname("黄路啊");
			note.setNote_content("内容测试"+i);
			note.setPost_time(TimeUtil.getCurrentTime());
			noteService.addNote(note, null);			
		}
	}

	@Test
	public void getTenNoteByTime() throws Exception {
		System.out.println(noteService.getTenNoteByTime(16, 26));
	}
	
	@Test
	public void testChangeSecond() throws Exception {
		for(int i = 0; i < 5; i++){
			Note note = new Note();
			note.setUser_id(29);
			note.setNickname("Huanglu");
			note.setTarget_id(407);
			note.setType(1);
			System.out.println(noteService.changeSecond(note));
		}
	}
	
	@Test
	public void testOnline() throws Exception {
		System.out.println(userService.online(16));
	}

	@Test
	public void findFans() throws Exception {
		System.out.println(userDao.getAllFansId(29));
	}
	
	@Test
	public void testGetMore() throws Exception {
		//System.out.println(noteService.getMoreNote(16));
		Note note1 = new Note();
		note1.setNote_id(1);
		Note note2 = new Note();
		note2.setNote_id(1);
		System.out.println(note1.equals(note2));
		Set<Note>set = new HashSet<>();
		set.add(note1);
		set.add(note2);
		System.out.println(set);
	}
}
