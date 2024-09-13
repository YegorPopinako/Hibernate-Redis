package ua.java.service;

import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.java.cache.RedisRepository;
import ua.java.domain.entity.City;
import ua.java.domain.exception.DatabaseOperationException;
import ua.java.redis.CityCountry;
import ua.java.redis.DataTransformer;
import ua.java.repository.CityRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Objects.isNull;

public class CityService {

    private static final Logger logger = LoggerFactory.getLogger(CityService.class);
    private final CityRepository repository;
    private final RedisRepository redisRepository;
    private final Map<Integer, Integer> requestCountMap = new HashMap<>();
    private final Map<Integer, String> requestNameMap = new HashMap<>();
    private final int CACHE_THRESHOLD = 2;

    public CityService(CityRepository repository, RedisRepository redisRepository) {
        this.repository = repository;
        this.redisRepository = redisRepository;
    }

    public List<City> getAll() {
        return repository.getAll();
    }

    public City getById(Integer id) {
        if (isNull(id) || id <= 0) {
            logger.error("Invalid id provided: {}", id);
            throw new IllegalArgumentException("Id cannot be null or less than or equal to 0");
        }

        if (requestCountMap.containsKey(id) && requestCountMap.get(id) >= CACHE_THRESHOLD) {
            logger.info("Returning City info with id {} from Redis cache", id);
            CityCountry cityCountry = redisRepository.getDataByName(requestNameMap.get(id));
            return DataTransformer.fromJsonToCityEntity(cityCountry);
        }

        requestCountMap.put(id, requestCountMap.getOrDefault(id, 0) + 1);

        City city;
        try {
            city = repository.getById(id);
        } catch (Exception e) {
            logger.error("City with id {} not found", id);
            throw new EntityNotFoundException("City with id " + id + " not found");
        }

        if (requestCountMap.get(id) >= CACHE_THRESHOLD) {
            requestNameMap.put(id, city.getName());
            CityCountry cityCountry = DataTransformer.transformDataToJson(city);
            redisRepository.cacheData(cityCountry);
            logger.info("Frequent City and Country data with id {} cached in Redis", id);
        }

        return city;
    }

    public City save(City entity) {
        if (entity == null) {
            throw new IllegalArgumentException("City entity cannot be null");
        }
        return repository.save(entity);
    }

    public void deleteById(Integer id) {
        if (isNull(id) || id <= 0) {
            logger.error("Invalid id provided: {}", id);
            throw new IllegalArgumentException("Id cannot be null or less than or equal to 0");
        }
        logger.info("Attempting to delete city with id: {}", id);
        try {
            repository.getById(id);
        } catch (Exception e) {
            logger.error("City with id {} not found", id);
            throw new EntityNotFoundException("City with id " + id + " not found");
        }
        repository.deleteById(id);
        logger.info("Successfully deleted city with id {}", id);
    }

    public void delete(City entity) {
        if (entity == null) {
            throw new IllegalArgumentException("City entity cannot be null");
        }
        try {
            repository.delete(entity);
            logger.info("Successfully deleted city");
        } catch (Exception e) {
            throw new DatabaseOperationException("Error deleting city");
        }
    }

    public void update(City entity) {
        if (entity == null) {
            throw new IllegalArgumentException("City entity cannot be null");
        }
        try {
            repository.update(entity);
            logger.info("Successfully updated City entity");
        } catch (Exception e) {
            throw new DatabaseOperationException("Error updating city");
        }
    }

    public void updateById(Integer id, City entity) {
        if (isNull(id) || id <= 0 || entity == null) {
            logger.error("Invalid id or city entity provided: {}, {}", id, entity);
            throw new IllegalArgumentException("Invalid id or city entity");
        }
        City existingCity;
        try {
            existingCity = repository.getById(id);
        } catch (Exception e) {
            throw new EntityNotFoundException("City with id " + id + " not found");
        }
        existingCity.setId(id);
        existingCity.setCountry(entity.getCountry());
        existingCity.setName(entity.getName());
        existingCity.setDistrict(entity.getDistrict());
        existingCity.setPopulation(entity.getPopulation());
        repository.update(existingCity);
        logger.info("Successfully updated city with id {}", id);
    }

    public List<City> getItems(int offset, int limit) {
        if (offset < 0 || limit < 0) {
            throw new IllegalArgumentException("Offset or limit cannot be less than 0");
        }
        return repository.getItems(offset, limit);
    }

    public int getCount() {
        return repository.getCount();
    }

    public Map<Integer, Integer> getRequestCountMap() {
        return requestCountMap;
    }

    public Map<Integer, String> getRequestNameMap() {
        return requestNameMap;
    }
}
