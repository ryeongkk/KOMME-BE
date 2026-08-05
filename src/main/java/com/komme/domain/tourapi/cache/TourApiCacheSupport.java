package com.komme.domain.tourapi.cache;

import java.time.Duration;
import java.util.function.Supplier;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// TourAPI/카카오 응답 Look-Aside 캐싱 지원 컴포넌트
@Slf4j
@Component
@RequiredArgsConstructor
public class TourApiCacheSupport {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    // 캐시 조회 후 없으면 loader를 호출해 원본을 가져오고, 결과를 TTL과 함께 캐시에 저장
    public <T> T getOrLoad(String key, Duration ttl, TypeReference<T> typeReference, Supplier<T> loader) {
        T cached = readCache(key, typeReference);
        if (cached != null) {
            return cached;
        }

        T loaded = loader.get();
        writeCache(key, ttl, loaded);
        return loaded;
    }

    // 캐시 값 조회 (역직렬화 실패 시 캐시 미스로 간주)
    private <T> T readCache(String key, TypeReference<T> typeReference) {
        String cached = redisTemplate.opsForValue().get(key);
        if (cached == null) {
            return null;
        }

        try {
            return objectMapper.readValue(cached, typeReference);
        } catch (JsonProcessingException exception) {
            log.warn("[*] TourApi 캐시 역직렬화 실패, 캐시 미스로 처리합니다. key={}", key, exception);
            return null;
        }
    }

    // 캐시 값 저장 (직렬화 실패해도 원본 응답 자체는 정상 반환되도록 예외를 흡수)
    private <T> void writeCache(String key, Duration ttl, T value) {
        try {
            redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(value), ttl);
        } catch (JsonProcessingException exception) {
            log.warn("[*] TourApi 캐시 저장 실패, 캐시 없이 진행합니다. key={}", key, exception);
        }
    }
}
