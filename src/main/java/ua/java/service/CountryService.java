package ua.java.service;

import jakarta.persistence.EntityNotFoundException;
import ua.java.domain.entity.Country;
import ua.java.domain.exception.DatabaseOperationException;
import ua.java.repository.CountryRepository;

import java.lang.reflect.Field;
import java.util.List;

import static java.util.Objects.isNull;

public class CountryService {

    private final CountryRepository repository;

    public CountryService(CountryRepository repository) {
        this.repository = repository;
    }

    public List<Country> getAll() {
        return repository.getAll();
    }

    public Country getById(Integer id) {
        if (isNull(id) || id <= 0) {
            throw new IllegalArgumentException("Id cannot be null or less than or equal to 0");
        }
        try {
            return repository.getById(id);
        } catch (Exception e) {
            throw new EntityNotFoundException("Country with id " + id + " not found");
        }
    }

    public Country save(Country entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Country entity cannot be null");
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
            throw new EntityNotFoundException("Country with id " + id + " not found");
        }
        repository.deleteById(id);
    }

    public void delete(Country entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Country entity cannot be null");
        }
        try {
            repository.delete(entity);
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
        } catch (Exception e) {
            throw new DatabaseOperationException("Error updating Country");
        }
    }

    public void updateById(Integer id, Country entity) {
        if (isNull(id) || id <= 0 || entity == null) {
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
}
