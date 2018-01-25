package com.fade.service;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;
import com.fade.domain.Note;
import com.fade.exception.FadeException;

public interface NoteService {

	String addNote(Note note, MultipartFile[] files)throws FadeException;

	String getTenNoteByTime(Integer user_id, Integer start, Integer concern_num);

	String getMoreNote(Integer user_id, List<Note>updateList);

	String changeSecond(Note note)throws FadeException;

	String getNotePage(Integer note_id) throws FadeException;

	String deleteNote(Integer note_id, Integer user_id);

	String getMyNote(Integer user_id, Integer start);

	String getOtherPersonNote(Integer user_id, Integer my_id, Integer start);
	
	void checkAction(List<Note>notes, Integer user_id);

	void addImage(List<Note> list);

	String getFullNote(Integer note_id, Integer user_id);
}
