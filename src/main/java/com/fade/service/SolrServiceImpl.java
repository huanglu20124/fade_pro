package com.fade.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.stereotype.Service;

import com.fade.domain.Note;
import com.fade.domain.NoteQuery;
import com.fade.domain.User;
import com.fade.domain.UserQuery;

@Service("solrService")
public class SolrServiceImpl implements SolrService {

	@Resource(name = "solrServer")
	private SolrServer solrServer;
	
	public static Logger logger = Logger.getLogger(SolrServiceImpl.class);
	
	@Override
	public void solrAddUpdateUser(User user) {
		//solr添加更新用户数据
		//添加到索引库
		SolrInputDocument document = new SolrInputDocument();
		document.setField("user_id", user.getUser_id());
		document.setField("nickname", user.getNickname());
		document.setField("sex", user.getSex());
		document.setField("head_image_url", user.getHead_image_url());
		document.setField("register_time", user.getRegister_time());
		document.setField("summary", user.getSummary());
		document.setField("concern_num", user.getConcern_num());
		document.setField("fans_num", user.getFans_num());
		document.setField("fade_num", user.getFade_num());
		document.setField("area", user.getArea());
		document.setField("school_id", user.getSchool_id());
		document.setField("school_name", user.getSchool_name());
		document.setField("id", user.getUuid());
		document.setField("fade_name", user.getFade_name());
		try {
			solrServer.add(document);
			solrServer.commit();
			logger.info("用户" + user.getUser_id() + "成功添加到索引库");
		} catch (SolrServerException e) {
			System.out.println("添加到索引库失败");
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public UserQuery getTenUserKeyword(String keyword, Integer page) {
		UserQuery query = new UserQuery();
		query.setStart(page + 1);
		//调用solr数据库，分页查询
		SolrQuery solrQuery = new SolrQuery();
		solrQuery.setQuery("user_keyword:" + keyword);
		//page=0的时候，先查询结果总数
		if(page == 0){
			try {
				QueryResponse sumResponse = solrServer.query(solrQuery);
				SolrDocumentList sumDocs = sumResponse.getResults();
				query.setSum(new Long(sumDocs.getNumFound()).intValue());
			} catch (SolrServerException e) {
				System.out.println("连接搜索引擎出错");
				e.printStackTrace();
				return null;
			}
		}
		//设置默认域
		//solrQuery.set("df", "user_keyword");
		solrQuery.setStart(page * 10);
		solrQuery.setRows(10);
		//设置高亮
		solrQuery.setHighlight(true);
		solrQuery.addHighlightField("nickname");
		solrQuery.addHighlightField("fade_name");
		solrQuery.setHighlightSimplePre("<font color=#29abe2>");
		solrQuery.setHighlightSimplePost("</font>");
		//得到结果
		QueryResponse response;
		try {
			response = solrServer.query(solrQuery);
		} catch (SolrServerException e) {
			System.out.println("连接搜索引擎出错");
			e.printStackTrace();
			return null;
		}
		// 文档结果集
		SolrDocumentList docs = response.getResults();
		System.out.println("记录条数为=" + docs.getNumFound());
		List<User>users = new ArrayList<>();
		Boolean isOne = false;
		Map<String, Map<String, List<String>>> map = response.getHighlighting();  
		for(SolrDocument document : docs){
			User user = new User();
			user.setUser_id((Integer) document.get("user_id"));
			String nickname = (String) document.get("nickname");
			String fade_name = (String) document.get("fade_name");
			if((nickname != null && nickname.equals(keyword)) || 
					(fade_name != null && fade_name.equals(keyword))){
					//完全匹配
				isOne = true;
			}
			user.setNickname(nickname);
			user.setArea((String) document.get("area"));
			user.setFade_name(fade_name); 
			user.setConcern_num((Integer) document.get("concern_num"));
			user.setFade_num((Integer) document.get("fade_num"));
			user.setFans_num((Integer) document.get("fans_num"));
			user.setHead_image_url((String) document.get("head_image_url"));
			user.setRegister_time((String) document.get("register_time"));
			user.setSchool_id((Integer)document.get("school_id"));
			user.setSchool_name((String) document.get("school_name"));
			user.setSex((String) document.get("sex"));
			user.setSummary((String) document.get("summary"));
			user.setUuid((String) document.get("id"));
			user.setTelephone((String) document.get("telephone"));
			Map<String, List<String>>docMap = map.get(document.get("id"));
			List<String>hightlight_nickname = docMap.get("nickname");
			List<String>hightlight_fade_name = docMap.get("fade_name");
			if(hightlight_nickname != null && hightlight_nickname.size() > 0){
				user.setNickname(hightlight_nickname.get(0));
			}
			if(hightlight_fade_name != null && hightlight_fade_name.size() > 0){
				user.setFade_name(hightlight_fade_name.get(0));
			}
			if(isOne){
				users.clear();
				users.add(user);
				query.setList(users);
				return query;
			}
			users.add(user);
		}
		query.setList(users);
		return query;
	}

	
	@Override
	public void solrAddUpdateNote(Note note) {
		// TODO Auto-generated method stub
		SolrInputDocument document = new SolrInputDocument();
		//solr仅仅存储4个属性
		document.setField("id", note.getUuid());
		document.setField("note_id", note.getNote_id());
		document.setField("note_content", note.getNote_content());
		document.setField("is_die", note.getIs_die());
		try {
			solrServer.add(document);
			solrServer.commit();
			logger.info("帖子" + note.getNote_id() + "成功添加到索引库");
		} catch (SolrServerException e) {
			System.out.println("添加到索引库失败");
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	
	@Override
	public NoteQuery getTenNoteKeyWord(String keyword, Integer page, Integer isAlive) {
		NoteQuery query = new NoteQuery();
		query.setStart(page + 1); 
		SolrQuery solrQuery = new SolrQuery();
		solrQuery.setQuery("note_content:" + keyword);
		if(isAlive == 1){
			//设置为活帖
			solrQuery.setFilterQueries("is_die:1");
		}else {
			//设置为死帖
			solrQuery.setFilterQueries("is_die:0");
		}
		//page=0的时候，先查询结果总数
		if(page == 0){
			try {
				QueryResponse sumResponse = solrServer.query(solrQuery);
				SolrDocumentList docs = sumResponse.getResults();
				query.setSum(new Long(docs.getNumFound()).intValue());
			} catch (SolrServerException e) {
				System.out.println("连接搜索引擎出错");
				e.printStackTrace();
				return null;
			}
		}
		//设置默认域
		//solrQuery.set("df", "user_keyword");
		solrQuery.setStart(page * 10);
		solrQuery.setRows(10);
		//设置高亮
		solrQuery.setHighlight(true);
		solrQuery.addHighlightField("note_content");
		solrQuery.setHighlightSimplePre("<font color=#29abe2>");
		solrQuery.setHighlightSimplePost("</font>");
		//得到结果
		QueryResponse response;
		try {
			response = solrServer.query(solrQuery);
		} catch (SolrServerException e) {
			logger.error("连接搜索引擎出错");
			e.printStackTrace();
			return null;
		}
		// 文档结果集
		//设置高亮
		Map<String, Map<String, List<String>>> map = response.getHighlighting();  
		SolrDocumentList docs = response.getResults();
		System.out.println("记录条数为=" + docs.getNumFound());
		List<Note>list = new ArrayList<>();
		for(int i = 0; i < docs.size(); i++){
			SolrDocument document = docs.get(i);
			Note note = new Note();
			note.setNote_id((Integer)document.get("note_id"));
			//note.setNote_content((String)document.get("note_content"));
			Map<String, List<String>>docMap = map.get(document.get("id"));
			List<String>hightlightWords = docMap.get("note_content");
			//System.out.println(hightlightWords);
			note.setNote_content(hightlightWords.get(0));
			note.setIs_die((Integer)document.get("is_die"));
			list.add(note);
		}
		query.setList(list);
		return query;
	}



	
}
