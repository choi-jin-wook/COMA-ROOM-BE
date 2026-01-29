//package com.coma.comaroom.config;
//
//import com.coma.comaroom.event.entity.Event;
//import com.fasterxml.jackson.databind.json.JsonMapper;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.data.redis.serializer.StringRedisSerializer;
//
//@Configuration
//public class RedisConfig {
//
//    // RedisTemplate<String, String> 설정 그대로 유지
//    public RedisTemplate<String, String> redisTemplate(RedisTemplate<?, ?> redisTemplateAuto) {
//        RedisTemplate<String, String> template = new RedisTemplate<>();
//        template.setConnectionFactory(redisTemplateAuto.getConnectionFactory());
//
//        StringRedisSerializer serializer = new StringRedisSerializer();
//        template.setKeySerializer(serializer);
//        template.setValueSerializer(serializer);
//        template.setHashKeySerializer(serializer);
//        template.setHashValueSerializer(serializer);
//
//        template.afterPropertiesSet();
//        return template;
//    }
//
//    // 직렬화: Event -> JSON 문자열, 메서드 내부에서 JsonMapper 생성
//    public String serializeEvent(Event event) {
//        try {
//            JsonMapper jsonMapper = new JsonMapper(); // 여기서 바로 생성
//            return jsonMapper.writeValueAsString(event);
//        } catch (Exception e) {
//            throw new RuntimeException("Failed to serialize Event", e);
//        }
//    }
//
//    // 역직렬화: JSON 문자열 -> Event, 메서드 내부에서 JsonMapper 생성
//    public Event deserializeEvent(String json) {
//        try {
//            JsonMapper jsonMapper = new JsonMapper(); // 여기서 바로 생성
//            return jsonMapper.readValue(json, Event.class);
//        } catch (Exception e) {
//            throw new RuntimeException("Failed to deserialize Event", e);
//        }
//    }
//}
