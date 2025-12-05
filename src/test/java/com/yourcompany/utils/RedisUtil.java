package com.yourcompany.utils;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import com.google.gson.Gson;
import java.util.HashMap;
import java.util.Map;

public class RedisUtil {
    private static JedisPool jedisPool;
    private static final Gson gson = new Gson();
    private static long hitCount = 0;
    
    static {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(10);
        poolConfig.setMaxIdle(5);
        poolConfig.setMinIdle(1);
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestOnReturn(true);
        
        jedisPool = new JedisPool(poolConfig, "localhost", 6379);
    }
    
    public static void connect() {
        try (Jedis jedis = jedisPool.getResource()) {
            String response = jedis.ping();
            System.out.println("Redis Connection: " + response);
        } catch (Exception e) {
            System.out.println("Redis Connection Failed: " + e.getMessage());
        }
    }
    
    public static void cacheData(String key, Object data, int ttlSeconds) {
        try (Jedis jedis = jedisPool.getResource()) {
            String json = gson.toJson(data);
            jedis.setex(key, ttlSeconds, json);
            System.out.println("Cached: " + key + " (TTL: " + ttlSeconds + "s)");
        } catch (Exception e) {
            System.out.println("Cache failed: " + e.getMessage());
        }
    }
    
    public static void cacheData(String testCase, String sheetName, Map<String, String> data) {
        cacheData(testCase + ":" + sheetName, data, 300);
    }
    
    public static <T> T getCachedData(String key, Class<T> clazz) {
        hitCount++;
        try (Jedis jedis = jedisPool.getResource()) {
            String json = jedis.get(key);
            if (json != null) {
                System.out.println("Cache HIT: " + key);
                return gson.fromJson(json, clazz);
            } else {
                System.out.println("Cache MISS: " + key);
                return null;
            }
        } catch (Exception e) {
            System.out.println("Cache retrieve failed: " + e.getMessage());
            return null;
        }
    }
    
    public static Map<String, String> getCachedData(String key) {
        return getCachedData(key, Map.class);
    }
    
    public static Map<String, String> getCachedData(String testCase, String sheetName) {
        return getCachedData(testCase + ":" + sheetName);
    }
    
    public static long getHitCount() {
        return hitCount;
    }
    
    public static void clearCache(String pattern) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.keys(pattern).forEach(jedis::del);
            System.out.println("Cleared cache pattern: " + pattern);
        }
    }
    
    public static void close() {
        if (jedisPool != null) {
            jedisPool.close();
            System.out.println("Redis connection pool closed");
        }
    }
    
    public static void testConnection() {
        try (Jedis jedis = jedisPool.getResource()) {
            String response = jedis.ping();
            System.out.println("Redis Server: " + response);
            System.out.println("Redis Info: " + jedis.info("server"));
        } catch (Exception e) {
            System.out.println("Redis test failed: " + e.getMessage());
            throw e;
        }
    }
    
    public static void logScenarioStart(String scenarioName) {
        try (Jedis jedis = jedisPool.getResource()) {
            Map<String, String> logData = new HashMap<>();
            logData.put("start_time", new java.util.Date().toString());
            logData.put("status", "RUNNING");
            
            String json = gson.toJson(logData);
            jedis.setex("scenario:" + scenarioName + ":log", 3600, json);
            System.out.println("Logged scenario start: " + scenarioName);
        }
    }
    
    public static void logScenarioEnd(String scenarioName, String status) {
        try (Jedis jedis = jedisPool.getResource()) {
            String key = "scenario:" + scenarioName + ":log";
            String existing = jedis.get(key);
            
            Map<String, String> logData;
            if (existing != null) {
                logData = gson.fromJson(existing, Map.class);
            } else {
                logData = new HashMap<>();
            }
            
            logData.put("end_time", new java.util.Date().toString());
            logData.put("status", status);
            logData.put("duration_ms", String.valueOf(System.currentTimeMillis()));
            
            String json = gson.toJson(logData);
            jedis.setex(key, 3600, json);
            System.out.println("Logged scenario end: " + scenarioName + " - " + status);
        }
    }
    
    public static void logFailure(String scenarioName, String url, String pageTitle) {
        try (Jedis jedis = jedisPool.getResource()) {
            Map<String, String> failureData = new HashMap<>();
            failureData.put("scenario", scenarioName);
            failureData.put("url", url);
            failureData.put("page_title", pageTitle);
            failureData.put("timestamp", new java.util.Date().toString());
            
            String json = gson.toJson(failureData);
            jedis.lpush("failures:list", json);
            jedis.setex("failure:" + scenarioName, 86400, json);
            
            System.out.println("Logged failure to Redis: " + scenarioName);
        }
    }
    
    public static void saveFailedTestData(String scenarioName, String url) {
        try (Jedis jedis = jedisPool.getResource()) {
            Map<String, String> data = new HashMap<>();
            data.put("scenario", scenarioName);
            data.put("url", url);
            data.put("saved_at", new java.util.Date().toString());
            data.put("note", "Preserved for debugging - test failed");
            
            String json = gson.toJson(data);
            jedis.setex("debug:failed:" + scenarioName, 86400, json);
            System.out.println("Saved failed test data to Redis: " + scenarioName);
        }
    }
    
    public static void clearTestData(String scenarioName) {
        try (Jedis jedis = jedisPool.getResource()) {
            String[] patterns = {
                "test:" + scenarioName + "*",
                "cart:*" + scenarioName + "*",
                "login:*" + scenarioName + "*",
                "checkout:*" + scenarioName + "*"
            };
            
            for (String pattern : patterns) {
                jedis.keys(pattern).forEach(jedis::del);
            }
            System.out.println("Cleared test data for: " + scenarioName);
        }
    }
    
    public static void logScenarioCompletion(String scenarioName, String status, String url) {
        try (Jedis jedis = jedisPool.getResource()) {
            Map<String, String> completionData = new HashMap<>();
            completionData.put("name", scenarioName);
            completionData.put("status", status);
            completionData.put("url", url);
            completionData.put("completed_at", new java.util.Date().toString());
            
            String json = gson.toJson(completionData);
            jedis.lpush("scenario:completions", json);
            jedis.ltrim("scenario:completions", 0, 99);
            
            System.out.println("Logged scenario completion to Redis");
        }
    }
    
    public static String getTestStats() {
        try (Jedis jedis = jedisPool.getResource()) {
            long totalScenarios = jedis.llen("scenario:completions");
            long failures = jedis.llen("failures:list");
            long cacheHits = hitCount;
            
            return String.format(
                "Total Scenarios: %d\nFailures: %d\nCache Hits: %d\nRedis Memory: %s",
                totalScenarios, failures, cacheHits, jedis.info("memory")
            );
        } catch (Exception e) {
            return "Redis stats unavailable: " + e.getMessage();
        }
    }
}