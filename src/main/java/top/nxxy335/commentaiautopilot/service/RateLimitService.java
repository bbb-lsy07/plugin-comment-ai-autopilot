package top.nxxy335.commentaiautopilot.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
public class RateLimitService implements DisposableBean {
    private final ConcurrentHashMap<Long, AtomicInteger> windowMap = new ConcurrentHashMap<>();
    private final Thread cleanupThread;
    private volatile boolean running = true;

    public RateLimitService() {
        // 每5分钟清理过期窗口，防止内存泄漏
        cleanupThread = new Thread(() -> {
            while (running && !Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(5 * 60 * 1000);
                    if (!running) break;
                    cleanup();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }, "rate-limit-cleanup");
        cleanupThread.setDaemon(true);
        cleanupThread.start();
    }

    public boolean tryAcquire(int limit) {
        long currentWindow = System.currentTimeMillis() / 60000; // 每分钟一个窗口
        AtomicInteger counter = windowMap.computeIfAbsent(currentWindow, k -> new AtomicInteger(0));
        return counter.incrementAndGet() <= limit;
    }

    public int getCurrentCount() {
        long currentWindow = System.currentTimeMillis() / 60000;
        AtomicInteger counter = windowMap.get(currentWindow);
        return counter != null ? counter.get() : 0;
    }

    // 清理过期窗口
    public void cleanup() {
        long currentWindow = System.currentTimeMillis() / 60000;
        int removed = 0;
        var iter = windowMap.keySet().iterator();
        while (iter.hasNext()) {
            if (iter.next() < currentWindow - 5) {
                iter.remove();
                removed++;
            }
        }
        if (removed > 0) {
            log.debug("[RateLimit] Cleaned up {} expired windows", removed);
        }
    }

    @Override
    public void destroy() {
        running = false;
        if (cleanupThread != null) {
            cleanupThread.interrupt();
        }
        log.info("[RateLimit] Cleanup thread stopped");
    }
}
