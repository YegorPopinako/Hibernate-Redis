package ua.java.service;

import jakarta.persistence.EntityNotFoundException;
import ua.java.domain.entity.City;
import ua.java.domain.exception.DatabaseOperationException;
import ua.java.repository.CityRepository;

import java.lang.reflect.Field;
import java.util.List;

import static java.util.Objects.isNull;

public class CityService {

    private final CityRepository repository;

    public CityService(CityRepository repository) {
        this.repository = repository;
    }

    public List<City> getAll() {
        return repository.getAll();
    }

    public City getById(Integer id) {
        if (isNull(id) || id <= 0) {
            throw new IllegalArgumentException("Id cannot be null or less than or equal to 0");
        }
        try {
            return repository.getById(id);
        } catch (Exception e) {
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
            throw new IllegalArgumentException("Id cannot be null or less than or equal to 0");
        }
        try {
            repository.getById(id);
        } catch (Exception e) {
            throw new EntityNotFoundException("City with id " + id + " not found");
        }
        repository.deleteById(id);
    }

    public void delete(City entity) {
        if (entity == null) {
            throw new IllegalArgumentException("City entity cannot be null");
        }
        try {
            repository.delete(entity);
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
        } catch (Exception e) {
            throw new DatabaseOperationException("Error updating city");
        }
    }

    public void updateById(Integer id, City entity) {
        if (isNull(id) || id <= 0 || entity == null) {
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
