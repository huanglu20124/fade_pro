package com.fade.util;
import java.util.ArrayList;
import java.util.List;

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
	
	public List<String> getRangeKey(String array_name) {
		//得到整个队列
		List<Object> origins=  redisTemplate.opsForList().range(array_name, 0l, -1l);
		List<String>keys = new ArrayList<>();
		for(Object object : origins){
			keys.add((String)object);
		}
		return keys;
	}

	public void leftPush(String array_name,String uuid){
		//将string插入到队列左边
		redisTemplate.opsForList().leftPush(array_name, uuid);
	}

	public void removeListIndex(String array_name,String key){
		//删除队列中的key
		redisTemplate.opsForList().remove(array_name, 1, key);
	}
}
