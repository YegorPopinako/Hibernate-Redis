package ua.java.service;

import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.java.domain.entity.City;
import ua.java.domain.exception.DatabaseOperationException;
import ua.java.repository.CityRepository;

import java.util.List;

import static java.util.Objects.isNull;

public class CityService {

    private static final Logger logger = LoggerFactory.getLogger(CityService.class);
    private final CityRepository repository;

    public CityService(CityRepository repository) {
        this.repository = repository;
    }

    public List<City> getAll() {
        return repository.getAll();
    }

    public City getById(Integer id) {
        if (isNull(id) || id <= 0) {
            logger.error("Invalid id provided: {}", id);
            throw new IllegalArgumentException("Id cannot be null or less than or equal to 0");
        }
        try {
            return repository.getById(id);
        } catch (Exception e) {
            logger.error("City with id {} not found", id);
            throw new EntityNotFoundException("City with id " + id + " not found");
        }
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
}
