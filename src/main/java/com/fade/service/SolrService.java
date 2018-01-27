package com.fade.service;

import java.util.List;

import com.fade.domain.Note;
import com.fade.domain.User;

public interface SolrService {

	void solrAddUpdateUser(User user);

	List<User> getTenUserKeyword(String keyword, Integer page);

	void solrAddUpdateNote(Note note);

	List<Note> getTenNoteKeyWord(String keyword, Integer page, Integer isAlive);
	
}
