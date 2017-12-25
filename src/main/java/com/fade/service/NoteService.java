package com.fade.service;

import org.springframework.web.multipart.MultipartFile;

import com.fade.domain.Note;
import com.fade.exception.FadeException;

public interface NoteService {

	String addNote(Note note, MultipartFile[] files)throws FadeException;

	String getTenNoteByTime(Integer user_id, Integer start);

	String getMoreNote(Integer user_id);

	String changeSecond(Note note)throws FadeException;

}
