package com.fade.service;

import com.fade.domain.Note;
import com.fade.domain.NoteQuery;
import com.fade.domain.User;
import com.fade.domain.UserQuery;

public interface SolrService {

	void solrAddUpdateUser(User user);

	 UserQuery getTenUserKeyword(String keyword, Integer page);

	void solrAddUpdateNote(Note note);

	NoteQuery getTenNoteKeyWord(String keyword, Integer page, Integer isAlive);
	
}
