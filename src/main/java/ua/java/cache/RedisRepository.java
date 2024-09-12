package ua.java.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import redis.clients.jedis.Jedis;
import ua.java.redis.CityCountry;

public class RedisRepository {

    private final Jedis redisClient;
    private final ObjectMapper objectMapper;

    public RedisRepository() {
        this.redisClient = new Jedis("localhost", 6379);
        this.objectMapper = new ObjectMapper();
    }

    public CityCountry getDataByName(String name) {
        String cityCountryKey = "cityName:" + name;
        String cityJson = redisClient.get(cityCountryKey);
        if (cityJson != null) {
            try {
                return objectMapper.readValue(cityJson, CityCountry.class);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Could not deserialize city");
            }
        }
        return null;
    }

    public void cacheData(CityCountry data) {
        String cityCountryKey = "cityName:" + data.getName();
        try {
            String cityJson = objectMapper.writeValueAsString(data);
            redisClient.set(cityCountryKey, cityJson);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Could not serialize city");
        }
    }

    public void removeCity(Integer id) {
        String cityKey = "city:" + id;
        redisClient.del(cityKey);
    }

    public void close() {
        redisClient.close();
    }
}
