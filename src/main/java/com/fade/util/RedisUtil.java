package com.fade.util;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;

public class RedisUtil{

	// 8月26号更新，统一redis连接池，加入spring
	private RedisTemplate<String, Object> redisTemplate;
	public RedisTemplate<String, Object> getRedisTemplate() {
		return redisTemplate;
	}

	public void setRedisTemplate(RedisTemplate<String, Object> redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	public void addKey(String key, Object value) {
		//增加key
		redisTemplate.opsForValue().set(key, value);
	}
	
	public void addKey(String key, Object value,Long timeout,TimeUnit unit){
		redisTemplate.opsForValue().set(key, value, timeout, unit);
	}
	
	public void deleteKey(String key) {
		//删除key
		redisTemplate.delete(key);
	}
	
	public void addSelf(String key){
		//自增
		redisTemplate.opsForValue().increment(key, 1);
	}
	
	public Object getValue(String key) {
		//得到对应的value
		return redisTemplate.opsForValue().get(key);
	}
	
	public List<String> listGetAll(String array_name) {
		//得到整个队列
		List<Object> origins=  redisTemplate.opsForList().range(array_name, 0l, -1l);
		List<String>keys = new ArrayList<>();
		for(Object object : origins){
			keys.add((String)object);
		}
		return keys;
	}

	public List<String> listGetRange(String array_name,Long start,Long end) {
		//得到整个队列
		List<Object> origins=  redisTemplate.opsForList().range(array_name, start, end);
		List<String>keys = new ArrayList<>();
		for(Object object : origins){
			keys.add((String)object);
		}
		return keys;
	}
	
	public void listLeftPush(String array_name,String value){
		//插入到队列左边
		redisTemplate.opsForList().leftPush(array_name, value);
	}

	public void listLeftPop(String array_name){
		redisTemplate.opsForList().leftPop(array_name);
	}
	
	public void listRightPop(String array_name){
		redisTemplate.opsForList().rightPop(array_name);
	}	
	
	public void listRightPush(String array_name,String value){
		//插入到队列左边
		redisTemplate.opsForList().rightPush(array_name, value);
	}	
	
	public void listRemoveValue(String array_name,String value){
		//删除队列中的元素
		redisTemplate.opsForList().remove(array_name, 1, value);
	}
	
	public void zsetAddKey(String key, Object value, Double score){
		//加入到zset里面
		redisTemplate.opsForZSet().add(key, value, score);
	}
	
	public Long listGetSize(String key){
		return redisTemplate.opsForList().size(key);
	}

	public void setAddKey(String key, Object...values){
		redisTemplate.opsForSet().add(key, values);
	}
	
	public Set<String> setGetAll(String key){
		Set<Object>origin = redisTemplate.opsForSet().members(key);
		Set<String>set = new HashSet<>();
		Iterator<Object>iterator = origin.iterator();
		while(iterator.hasNext()){
			set.add((String)iterator.next());
		}
		return set;
	}
	
	public Boolean setIsMember(String key, Object value){
		return redisTemplate.opsForSet().isMember(key, value);
	}
	
	
}
