package com.fade.controller;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSON;
import com.fade.domain.Note;
import com.fade.exception.FadeException;
import com.fade.service.NoteService;

@Controller
public class NoteController {
	
	@Resource(name = "noteService")
	private NoteService noteService;
	
	@RequestMapping(value = "/addNote", method = RequestMethod.POST)
	@ResponseBody
	public String addNote(String note,@RequestParam("file")MultipartFile[]files) throws FadeException{
		//增加的一定是原创帖，需要发的属性有user_id,nickname,note_content,head_image_url
		return noteService.addNote(JSON.parseObject(note, Note.class),files); 
	}

	@RequestMapping(value = "/getTenNoteByTime/{user_id}/{start}", method = RequestMethod.GET)
	@ResponseBody
	public String getTenNoteByTime(@PathVariable("user_id")Integer user_id,
			@PathVariable("start")Integer start){
		//初次加载以及向下翻，  按照时间顺序上拉加载10条，一开始start填0或者不发
		return noteService.getTenNoteByTime(user_id,start);
	}
	
	@RequestMapping(value = "/getMoreNote/{user_id}/{update_list}", method = RequestMethod.GET)
	@ResponseBody
	public String getMoreNote(@PathVariable("user_id")Integer user_id, @PathVariable("update_list")String update_list){
		//顶部下拉刷新，同时更新之前发过的帖子的信息,update_list的一项要包含note_id,target_id
		return noteService.getMoreNote(user_id,JSON.parseArray(update_list, Note.class));
	}	

	@RequestMapping(value = "/changeSecond", method = RequestMethod.POST)
	@ResponseBody
	public String changeSecond(String note) throws FadeException{
		//增和减秒的请求，实则是增加帖子,需要发的属性有user_id,nickname,note_content,target_id,type,head_image_url 
		return noteService.changeSecond(JSON.parseObject(note,Note.class));
	}
}
