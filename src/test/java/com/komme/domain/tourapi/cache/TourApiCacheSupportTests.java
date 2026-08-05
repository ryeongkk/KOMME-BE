package com.komme.domain.tourapi.cache;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TourApiCacheSupportTests {

    private static final String CACHE_KEY = "tourapi:test:key";
    private static final Duration TTL = Duration.ofHours(1);

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // 캐시 미스 시 loader를 호출하고 결과를 TTL과 함께 저장하는지 검증
    @Test
    void getOrLoadCallsLoaderAndCachesResultOnMiss() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(CACHE_KEY)).thenReturn(null);
        AtomicInteger loaderCallCount = new AtomicInteger();

        List<String> result = createSupport().getOrLoad(
                CACHE_KEY,
                TTL,
                new TypeReference<List<String>>() {
                },
                () -> {
                    loaderCallCount.incrementAndGet();
                    return List.of("a", "b");
                }
        );

        assertThat(result).containsExactly("a", "b");
        assertThat(loaderCallCount).hasValue(1);
        verify(valueOperations).set(eq(CACHE_KEY), any(String.class), eq(TTL));
    }

    // 캐시 히트 시 loader를 호출하지 않고 캐시값을 반환하는지 검증
    @Test
    void getOrLoadReturnsCachedValueWithoutCallingLoaderOnHit() throws Exception {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(CACHE_KEY)).thenReturn(objectMapper.writeValueAsString(List.of("cached")));
        AtomicInteger loaderCallCount = new AtomicInteger();

        List<String> result = createSupport().getOrLoad(
                CACHE_KEY,
                TTL,
                new TypeReference<List<String>>() {
                },
                () -> {
                    loaderCallCount.incrementAndGet();
                    return List.of("fresh");
                }
        );

        assertThat(result).containsExactly("cached");
        assertThat(loaderCallCount).hasValue(0);
    }

    // 캐시값이 손상되어 역직렬화에 실패하면 캐시 미스로 처리하고 loader를 호출하는지 검증
    @Test
    void getOrLoadFallsBackToLoaderWhenCachedValueIsCorrupted() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(CACHE_KEY)).thenReturn("not-valid-json");
        AtomicInteger loaderCallCount = new AtomicInteger();

        List<String> result = createSupport().getOrLoad(
                CACHE_KEY,
                TTL,
                new TypeReference<List<String>>() {
                },
                () -> {
                    loaderCallCount.incrementAndGet();
                    return List.of("recovered");
                }
        );

        assertThat(result).containsExactly("recovered");
        assertThat(loaderCallCount).hasValue(1);
    }

    private TourApiCacheSupport createSupport() {
        return new TourApiCacheSupport(redisTemplate);
    }
}
