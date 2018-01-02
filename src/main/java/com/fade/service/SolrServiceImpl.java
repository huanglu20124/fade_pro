package com.fade.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

import com.fade.domain.User;

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
		document.setField("wallpaper_url", user.getWallpaper_url());
		document.setField("school", user.getSchool());
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
	public List<User> getTenUserKeyword(String keyword, Integer page) {
		//调用solr数据库，分页查询
		SolrQuery solrQuery = new SolrQuery();
		solrQuery.setQuery("nickname:" + keyword + " or " + "fade_name:" + keyword);
		//设置默认域
		//solrQuery.set("df", "user_keyword");
		solrQuery.setStart(page * 10);
		solrQuery.setRows(10);
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
		for(SolrDocument document : docs){
			User user = new User();
			user.setUser_id((Integer) document.get("user_id"));
			user.setNickname((String) document.get("nickname"));
			user.setArea((String) document.get("area"));
			user.setFade_name((String) document.get("fade_name")); 
			user.setConcern_num((Integer) document.get("concern_num"));
			user.setFade_num((Integer) document.get("fade_num"));
			user.setFans_num((Integer) document.get("fans_num"));
			user.setHead_image_url((String) document.get("head_image_url"));
			user.setRegister_time((String) document.get("register_time"));
			user.setSchool((String) document.get("school"));
			user.setSex((String) document.get("sex"));
			user.setSummary((String) document.get("summary"));
			user.setUuid((String) document.get("id"));
			user.setTelephone((String) document.get("telephone"));
			users.add(user);
		}
		return users;
	}

}
