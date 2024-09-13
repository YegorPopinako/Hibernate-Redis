package ua.java.service;

import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.java.cache.RedisRepository;
import ua.java.domain.entity.City;
import ua.java.domain.entity.Country;
import ua.java.domain.exception.DatabaseOperationException;
import ua.java.redis.CityCountry;
import ua.java.redis.DataTransformer;
import ua.java.repository.CountryRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Objects.isNull;

public class CountryService {
    private static final Logger logger = LoggerFactory.getLogger(CountryService.class);
    private final CountryRepository repository;
    private final RedisRepository redisRepository;
    private final Map<Integer, Integer> requestCountMap = new HashMap<>();
    private final Map<Integer, String> requestNameMap = new HashMap<>();
    private final int CACHE_THRESHOLD = 2;

    public CountryService(CountryRepository repository, RedisRepository redisRepository) {
        this.repository = repository;
        this.redisRepository = redisRepository;
    }

    public List<Country> getAll() {
        return repository.getAll();
    }

    public Country getById(Integer id) {
        if (isNull(id) || id <= 0) {
            logger.error("Invalid id provided: {}", id);
            throw new IllegalArgumentException("Id cannot be null or less than or equal to 0");
        }

        if (requestCountMap.containsKey(id) && requestCountMap.get(id) >= CACHE_THRESHOLD) {
            logger.info("Returning Country info with id {} from Redis cache", id);
            CityCountry cityCountry = redisRepository.getDataByName(requestNameMap.get(id));
            return DataTransformer.fromJsonToCountryEntity(cityCountry);
        }

        requestCountMap.put(id, requestCountMap.getOrDefault(id, 0) + 1);

        Country country;
        try {
            country = repository.getById(id);
        } catch (Exception e) {
            logger.error("Country with id {} not found", id);
            throw new EntityNotFoundException("Country with id " + id + " not found");
        }

        if (requestCountMap.get(id) >= CACHE_THRESHOLD) {
            City city = repository.getCityByCountryId(country.getId());
            requestNameMap.put(id, city.getName());
            CityCountry cityCountry = DataTransformer.transformDataToJson(city);
            redisRepository.cacheData(cityCountry);
            logger.info("Frequent City and Country data with id {} cached in Redis", id);
        }

        return country;
    }

    public Country save(Country entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Country entity cannot be null");
        }
        return repository.save(entity);
    }

    public void deleteById(Integer id) {
        if (isNull(id) || id <= 0) {
            logger.error("Invalid id provided: {}", id);
            throw new IllegalArgumentException("Id cannot be null or less than or equal to 0");
        }
        try {
            repository.getById(id);
        } catch (Exception e) {
            logger.error("Country with id {} not found", id);
            throw new EntityNotFoundException("Country with id " + id + " not found");
        }
        repository.deleteById(id);
        logger.info("Successfully deleted city with id {}", id);
    }

    public void delete(Country entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Country entity cannot be null");
        }
        try {
            repository.delete(entity);
            logger.info("Successfully deleted city");
        } catch (Exception e) {
            throw new DatabaseOperationException("Error deleting Country");
        }
    }

    public void update(Country entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Country entity cannot be null");
        }
        try {
            repository.update(entity);
            logger.info("Successfully updated Country entity");
        } catch (Exception e) {
            throw new DatabaseOperationException("Error updating Country");
        }
    }

    public void updateById(Integer id, Country entity) {
        if (isNull(id) || id <= 0 || entity == null) {
            logger.error("Invalid id or city entity provided: {}, {}", id, entity);
            throw new IllegalArgumentException("Invalid id or Country entity");
        }
        Country existingCountry;
        try {
            existingCountry = repository.getById(id);
        } catch (Exception e) {
            throw new EntityNotFoundException("Country with id " + id + " not found");
        }
        existingCountry.setId(id);
        existingCountry.setCode(entity.getCode());
        existingCountry.setSecondCode(entity.getSecondCode());
        existingCountry.setName(entity.getName());
        existingCountry.setContinent(entity.getContinent());
        existingCountry.setRegion(entity.getRegion());
        existingCountry.setSurfaceArea(entity.getSurfaceArea());
        existingCountry.setIndependenceYear(entity.getIndependenceYear());
        existingCountry.setPopulation(entity.getPopulation());
        existingCountry.setLifeExpectancy(entity.getLifeExpectancy());
        existingCountry.setGnp(entity.getGnp());
        existingCountry.setGnpoId(entity.getGnpoId());
        existingCountry.setLocalName(entity.getLocalName());
        existingCountry.setGovernmentForm(entity.getGovernmentForm());
        existingCountry.setHeadOfState(entity.getHeadOfState());
        existingCountry.setCapital(entity.getCapital());
        existingCountry.setLanguages(entity.getLanguages());
        repository.update(existingCountry);
        logger.info("Successfully updated country with id {}", id);
    }

    public List<Country> getItems(int offset, int limit) {
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
