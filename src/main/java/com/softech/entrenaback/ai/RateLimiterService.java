package com.softech.entrenaback.ai;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimiterService {

    private final Map<String, UserBucket> buckets = new ConcurrentHashMap<>();
    private final int maxRequests = 10;
    private final long windowMs = 60000;

    public boolean isAllowed(String email) {
        var now = System.currentTimeMillis();
        var bucket = buckets.computeIfAbsent(email, k -> new UserBucket());

        synchronized (bucket) {
            bucket.clean(now, windowMs);
            if (bucket.count >= maxRequests) {
                return false;
            }
            bucket.count++;
            bucket.requests.add(now);
            return true;
        }
    }

    private static class UserBucket {
        final java.util.List<Long> requests = new java.util.ArrayList<>();
        int count = 0;

        void clean(long now, long windowMs) {
            requests.removeIf(t -> now - t > windowMs);
            count = requests.size();
        }
    }
}
