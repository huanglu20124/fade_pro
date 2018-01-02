package com.hl.test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.apache.shiro.crypto.hash.Md5Hash;
import org.junit.Test;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fade.domain.Comment;
import com.fade.domain.Note;
import com.fade.domain.NoteQuery;
import com.fade.domain.SecondComment;
import com.fade.domain.User;
import com.fade.mapper.CommentDao;
import com.fade.mapper.NoteDao;
import com.fade.mapper.UserDao;
import com.fade.service.CommentService;
import com.fade.service.NoteService;
import com.fade.service.SolrService;
import com.fade.service.UserService;
import com.fade.util.Const;
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
	
	@Resource(name = "commentDao")
	private CommentDao commentDao;
	
	@Resource(name = "tokenUtil")
	private TokenUtil tokenUtil;
	
	@Resource(name = "redisUtil")
	private RedisUtil redisUtil;
	
	@Resource(name = "noteService")
	private NoteService noteService;
	
	@Resource(name = "commentService")
	private CommentService commentService;
	
	@Resource(name = "solrService")
	private SolrService solrService;
	
/*	@Test
	public void testAdd() throws Exception {
		User user = new User();
		user.setFade_name("test3");
		user.setConcern_num(0);
		user.setFade_num(0);
		user.setFans_num(0);
		user.setRegister_time(com.fade.util.TimeUtil.getCurrentTime());
		userDao.addUser(user);
		System.out.println(user.getUser_id());//自增主键自动设置到对象中
	}*/

/*	@Test
	public void testEditNickname() throws Exception {
		User user = new User();
		user.setUser_id(1);
		user.setNickname("aaaa");
		System.out.println(userDao.updateNickname(user));	//返回更新的行数
	}*/

/*	@Test
	public void testEditHead() throws Exception {
		System.out.println(userDao.updateHeadUrl("11111", 8));
	}*/

/*	@Test
	public void testGetStarUser() throws Exception {
		List<User>users = userDao.getStarUser(1);
		for(User user : users){
			System.out.println(user.getNickname() + "--" + user.getUser_id());
		}
	}*/
	
/*	@Test
	public void testGetUserByTelephone() throws Exception {
		User user = userDao.getUserByTel("13763359943");
		System.out.println(user);
	}*/
	
/*	@Test
	public void testList() throws Exception {
		redisUtil.listRightPush("test", "note_1");
		redisUtil.listRightPush("test", "note_2");
		redisUtil.listRightPush("test", "note_3");
		redisUtil.listLeftPop("test");
		System.out.println(redisUtil.listGetAll("test"));
	}*/
		
/*	@Test
	public void testAddNote() throws Exception {
		for(int i = 0; i < 5; i++){
			Note note = new Note();
			note.setUser_id(43);
			note.setNickname("黄路");
			note.setNote_content("内容测试"+i);
			note.setPost_time(TimeUtil.getCurrentTime());
			note.setHead_image_url("image/head/2017-12/2c6dc9ec-f.png");
			noteService.addNote(note, null);			
		}
	}*/

/*	@Test
	public void getTenNoteByTime() throws Exception {
		System.out.println(noteService.getTenNoteByTime(43,26,1));
	}*/
	
/*	@Test
	public void testChangeSecond() throws Exception {
		for(int i = 0; i < 5; i++){
			Note note = new Note();
			note.setUser_id(29);
			note.setNickname("Huanglu");
			note.setTarget_id(445);
			note.setType(1);
			System.out.println(noteService.changeSecond(note));
		}
	}*/
	
/*	@Test
	public void testOnline() throws Exception {
		System.out.println(userService.online(43));
	}*/

/*	@Test
	public void findFans() throws Exception {
		System.out.println(userDao.getAllFansId(29));
	}*/
	
/*	@Test
	public void testGetMore() throws Exception {
		List<Note>list = new ArrayList<>();
		System.out.println(noteService.getMoreNote(43, list));
	}*/
	
/*	@Test
	public void testOffline() throws Exception {
		userService.offline(30);
	}*/
	
/*	@Test
	public void testAddComment() throws Exception {
		int k = 5;
		//while(k > 0){
			Comment comment = new Comment();
			comment.setNote_id(207);
			comment.setHead_image_url("image/head/2017-12/2c6dc9ec-f.png");
			comment.setNickname("黄路");
			comment.setComment_content("这是一级评论");
			comment.setUser_id(43);
			comment.setType(0);
			commentService.addComment(comment); 
			//k--;
		//}
	}*/
	
/*	@Test
	public void testGetTenComment() throws Exception {
		System.out.println(commentService.getTenComment(207, 0));
	}*/
	
/*	@Test
	public void testaddSecondComment() throws Exception {
		SecondComment secondComment = new SecondComment();
		secondComment.setComment_id(16);
		secondComment.setUser_id(43);
		secondComment.setTo_user_id(43);
		secondComment.setTo_nickname("黄路");
		secondComment.setComment_content("这是一条二级评论，回复黄路的(第二条)");
		secondComment.setNickname("黄路");
		secondComment.setNote_id(172);
		System.out.println(commentService.addSecondComment(secondComment));
	}*/

/*	@Test
	public void testConcern() throws Exception {
		System.out.println(userService.concern(47, 45));
	    //System.out.println(userService.cancelConcern(16, 30));
	}*/

/*	@Test
	public void testLogin() throws Exception {
		User user = new User();
		user.setPassword("137137lu");
		user.setTelephone("13763359943");
		System.out.println(userService.loginUser(user));
	}*/

/*	@Test
	public void testGetNotePage() throws Exception {
		System.out.println(noteService.getNotePage(114));
	}*/
	
/*	@Test
	public void testSolr() throws Exception {
	    System.out.println(solrService.getTenUserKeyword("黄路", 0));
	}*/
	
	@Test
	public void testAll() throws Exception {
		System.out.println(userService.getPersonPage(47, 43));
	}

}
