package ua.java.service;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.java.cache.RedisRepository;
import ua.java.domain.entity.City;
import ua.java.domain.exception.DatabaseOperationException;
import ua.java.redis.CityCountry;
import ua.java.repository.CityRepository;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CityServiceTest {

    @Mock
    private CityRepository repository;

    @Mock
    private RedisRepository redisRepository;

    @InjectMocks
    private CityService cityService;

    @Test
    void testGetAll() {
        City city1 = new City();
        City city2 = new City();
        List<City> cities = Arrays.asList(city1, city2);

        when(repository.getAll()).thenReturn(cities);

        List<City> result = cityService.getAll();

        assertEquals(2, result.size());
        verify(repository, times(1)).getAll();
    }

    @Test
    void testGetById() {
        Integer id = 1;
        City city = new City();
        city.setId(id);

        when(repository.getById(id)).thenReturn(city);

        City result = cityService.getById(id);

        assertNotNull(result);
        verify(repository, times(1)).getById(id);
        verify(redisRepository, never()).getDataByName(any());
    }

    @Test
    void testGetByIdFromRedisCache() {
        Integer validId = 2;
        int CACHE_THRESHOLD = 2;
        CityCountry cityCountry = new CityCountry();
        cityCountry.setName("TestCityFromCache");

        cityService.getRequestNameMap().put(validId, "TestCityFromCache");
        cityService.getRequestCountMap().put(validId, CACHE_THRESHOLD + 1);

        when(redisRepository.getDataByName("TestCityFromCache")).thenReturn(cityCountry);

        City result = cityService.getById(validId);

        assertNotNull(result);
        verify(redisRepository, times(1)).getDataByName("TestCityFromCache");
        verify(repository, never()).getById(validId);
    }

    @Test
    void testGetByNullId() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> cityService.getById(null));

        assertEquals("Id cannot be null or less than or equal to 0", exception.getMessage());
        verify(repository, never()).getById(any());
        verify(redisRepository, never()).getDataByName(any());
    }

    @Test
    void testGetByInvalidId() {
        Integer invalidId = 0;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> cityService.getById(invalidId));

        assertEquals("Id cannot be null or less than or equal to 0", exception.getMessage());
        verify(repository, never()).getById(any());
        verify(redisRepository, never()).getDataByName(any());
    }

    @Test
    void testGetByIdCityNotFound() {
        Integer validId = 3;

        when(repository.getById(validId)).thenThrow(new EntityNotFoundException("City with id " + validId + " not found"));

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> cityService.getById(validId));

        assertEquals("City with id " + validId + " not found", exception.getMessage());
        verify(repository, times(1)).getById(validId);
        verify(redisRepository, never()).getDataByName(any());
    }

    @Test
    void testSaveCity() {
        City city = new City();
        city.setId(1);
        city.setName("TestCity");

        when(repository.save(city)).thenReturn(city);

        City result = cityService.save(city);

        assertNotNull(result);
        assertEquals(city.getId(), result.getId());
        assertEquals(city.getName(), result.getName());
        verify(repository, times(1)).save(city);
    }

    @Test
    void testSaveNullCity() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> cityService.save(null));

        assertEquals("City entity cannot be null", exception.getMessage());
    }

    @Test
    void testDeleteById() {
        Integer id = 1;

        when(repository.getById(id)).thenReturn(new City());

        cityService.deleteById(id);

        verify(repository, times(1)).getById(id);
        verify(repository, times(1)).deleteById(id);
    }

    @Test
    void testDeleteByInvalidId() {
        Integer invalidId = 0;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> cityService.deleteById(invalidId));

        assertEquals("Id cannot be null or less than or equal to 0", exception.getMessage());
        verify(repository, never()).getById(any());
    }

    @Test
    void testDeleteEntity() {
        City city = new City();
        city.setId(1);

        doNothing().when(repository).delete(city);

        cityService.delete(city);

        verify(repository, times(1)).delete(city);
    }

    @Test
    void testDeleteWithException() {
        City city = new City();
        city.setId(1);

        doThrow(new DatabaseOperationException("Error deleting city")).when(repository).delete(city);

        DatabaseOperationException exception = assertThrows(DatabaseOperationException.class, () -> cityService.delete(city));

        assertEquals("Error deleting city", exception.getMessage());
    }

    @Test
    void testUpdateEntity() {
        City city = new City();
        city.setId(1);

        doNothing().when(repository).update(city);

        cityService.update(city);

        verify(repository, times(1)).update(city);
    }

    @Test
    void testUpdateWithException() {
        City city = new City();
        city.setId(1);

        doThrow(new RuntimeException("Error updating city")).when(repository).update(city);

        DatabaseOperationException exception = assertThrows(DatabaseOperationException.class, () -> cityService.update(city));

        assertEquals("Error updating city", exception.getMessage());
    }

    @Test
    void testUpdateById() {
        Integer id = 1;
        City existingCity = new City();
        existingCity.setId(id);

        City updatedCity = new City();
        updatedCity.setName("NewCityName");

        when(repository.getById(id)).thenReturn(existingCity);
        doNothing().when(repository).update(existingCity);

        cityService.updateById(id, updatedCity);

        verify(repository, times(1)).getById(id);
        verify(repository, times(1)).update(existingCity);
        assertEquals("NewCityName", existingCity.getName());
    }

    @Test
    void testUpdateByInvalidId() {
        Integer id = 0;
        City updatedCity = new City();
        updatedCity.setName("NewCityName");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> cityService.updateById(id, updatedCity));

        assertEquals("Invalid id or city entity", exception.getMessage());

        verify(repository, never()).getById(any());
        verify(repository, never()).update(any());
    }

    @Test
    void testUpdateWithNullEntity() {
        Integer id = 1;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> cityService.updateById(id, null));

        assertEquals("Invalid id or city entity", exception.getMessage());

        verify(repository, never()).getById(any());
        verify(repository, never()).update(any());
    }

    @Test
    void testUpdateNonExistingEntity() {
        Integer id = 1;
        City updatedCity = new City();
        updatedCity.setName("NewCityName");

        when(repository.getById(id)).thenThrow(new RuntimeException("Entity not found"));

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> cityService.updateById(id, updatedCity));

        assertEquals("City with id " + id + " not found", exception.getMessage());
        verify(repository, times(1)).getById(id);
        verify(repository, never()).update(any());
    }

    @Test
    void testGetItemsWithValidParameters() {
        int offset = 0;
        int limit = 10;
        List<City> cities = Arrays.asList(new City(), new City());

        when(repository.getItems(offset, limit)).thenReturn(cities);

        List<City> result = cityService.getItems(offset, limit);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(repository, times(1)).getItems(offset, limit);
    }

    @Test
    void testGetItemsWithInvalidParameters() {
        int offset = -1;
        int limit = 0;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> cityService.getItems(offset, limit));

        assertEquals("Offset or limit cannot be less than 0", exception.getMessage());

        verify(repository, never()).getItems(offset, limit);
    }

    @Test
    void testGetCount() {
        int count = 5;

        when(repository.getCount()).thenReturn(count);

        int result = cityService.getCount();

        assertEquals(count, result);
        verify(repository, times(1)).getCount();
    }
}
