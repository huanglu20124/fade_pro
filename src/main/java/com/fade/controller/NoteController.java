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
import com.fade.domain.Image;
import com.fade.domain.Note;
import com.fade.exception.FadeException;
import com.fade.service.NoteService;

@Controller
public class NoteController {
	
	@Resource(name = "noteService")
	private NoteService noteService;
	
	@RequestMapping(value = "/addNote", method = RequestMethod.POST)
	@ResponseBody
	public String addNote(Integer user_id, String note_content, String nickname,
			String images,@RequestParam("file")MultipartFile[]files) throws FadeException{
		Note note = new Note();
		note.setUser_id(user_id); 
		note.setNote_content(note_content);
		note.setNickname(nickname);
		note.setImages(JSON.parseArray(images, Image.class));
		return noteService.addNote(note,files); 
	}

	@RequestMapping(value = "/getTenNoteByTime/{user_id}/{start}", method = RequestMethod.GET)
	@ResponseBody
	public String getTenNoteByTime(@PathVariable("user_id")Integer user_id,
			@PathVariable("start")Integer start){
		//按照时间顺序上拉加载10条，一开始start填0或者不发
		return noteService.getTenNoteByTime(user_id,start);
	}
}
