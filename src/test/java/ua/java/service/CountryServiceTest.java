package ua.java.service;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.java.cache.RedisRepository;
import ua.java.domain.entity.City;
import ua.java.domain.entity.Country;
import ua.java.domain.exception.DatabaseOperationException;
import ua.java.redis.CityCountry;
import ua.java.repository.CountryRepository;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class CountryServiceTest {

    @Mock
    private CountryRepository repository;

    @Mock
    private RedisRepository redisRepository;

    @InjectMocks
    private CountryService countryService;

    @Test
    void testGetAll() {
        Country country1 = new Country();
        Country country2 = new Country();
        List<Country> countries = Arrays.asList(country1, country2);

        when(repository.getAll()).thenReturn(countries);

        List<Country> result = countryService.getAll();

        assertEquals(2, result.size());
        verify(repository, times(1)).getAll();
    }

    @Test
    void testGetById() {
        Integer id = 1;
        Country country = new Country();
        country.setId(id);
        City city = new City();
        city.setName("TestCity");

        when(repository.getById(id)).thenReturn(country);

        Country result = countryService.getById(id);

        assertNotNull(result);
        verify(repository, times(1)).getById(id);
        verify(redisRepository, never()).cacheData(any());
    }

    @Test
    void testGetByIdFromRedisCache() {
        Integer validId = 2;
        int CACHE_THRESHOLD = 2;
        CityCountry cityCountry = new CityCountry();
        cityCountry.setName("TestCityFromCache");

        countryService.getRequestNameMap().put(validId, "TestCityFromCache");

        countryService.getRequestCountMap().put(validId, CACHE_THRESHOLD + 1);

        when(redisRepository.getDataByName("TestCityFromCache")).thenReturn(cityCountry);

        Country result = countryService.getById(validId);

        assertNotNull(result);
        verify(redisRepository, times(1)).getDataByName("TestCityFromCache");
        verify(repository, never()).getById(validId);
    }

    @Test
    void testGetByNullId() {

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> countryService.getById(null));

        assertEquals("Id cannot be null or less than or equal to 0", exception.getMessage());
        verify(repository, never()).getById(any());
        verify(redisRepository, never()).getDataByName(any());
    }

    @Test
    void testGetByInvalidId() {
        Integer invalidId = 0;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> countryService.getById(invalidId));

        assertEquals("Id cannot be null or less than or equal to 0", exception.getMessage());
        verify(repository, never()).getById(any());
        verify(redisRepository, never()).getDataByName(any());
    }

    @Test
    void testGetByIdCountryNotFound() {
        Integer validId = 3;

        when(repository.getById(validId)).thenThrow(new EntityNotFoundException("Country with id " + validId + " not found"));

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> countryService.getById(validId));

        assertEquals("Country with id " + validId + " not found", exception.getMessage());
        verify(repository, times(1)).getById(validId);
        verify(redisRepository, never()).getDataByName(any());
    }

    @Test
    public void testSaveCountry() {
        Country country = new Country();
        country.setId(1);
        country.setName("TestCountry");

        when(repository.save(country)).thenReturn(country);

        Country result = countryService.save(country);

        assertNotNull(result);
        assertEquals(country.getId(), result.getId());
        assertEquals(country.getName(), result.getName());
        verify(repository, times(1)).save(country);
    }

    @Test
    public void testSaveNullCountry() {

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> countryService.save(null));

        assertEquals("Country entity cannot be null", exception.getMessage());
    }

    @Test
    public void testDeleteById() {
        Integer id = 1;

        when(repository.getById(id)).thenReturn(new Country());

        countryService.deleteById(id);

        verify(repository, times(1)).getById(id);
        verify(repository, times(1)).deleteById(id);
    }

    @Test
    void testDeleteByInvalidId() {
        Integer invalidId = 0;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> countryService.getById(invalidId));

        assertEquals("Id cannot be null or less than or equal to 0", exception.getMessage());
        verify(repository, never()).getById(any());
        verify(redisRepository, never()).getDataByName(any());
    }

    @Test
    public void testDeleteEntity() {
        Country country = new Country();
        country.setId(1);

        doNothing().when(repository).delete(country);

        countryService.delete(country);

        verify(repository, times(1)).delete(country);
    }

    @Test
    public void testDeleteWithException() {
        Country country = new Country();
        country.setId(1);

        doThrow(new DatabaseOperationException("Error deleting Country")).when(repository).delete(country);

        DatabaseOperationException exception = assertThrows(DatabaseOperationException.class, () -> countryService.delete(country));

        assertEquals("Error deleting Country", exception.getMessage());
    }

    @Test
    public void testUpdateEntity() {
        Country country = new Country();
        country.setId(1);

        doNothing().when(repository).update(country);

        countryService.update(country);

        verify(repository, times(1)).update(country);
    }

    @Test
    public void testUpdateWithException() {
        Country country = new Country();
        country.setId(1);

        doThrow(new RuntimeException("Error updating Country")).when(repository).update(country);

        DatabaseOperationException exception = assertThrows(DatabaseOperationException.class, () -> countryService.update(country));

        assertEquals("Error updating Country", exception.getMessage());

    }

    @Test
    public void testUpdateById() {
        Integer id = 1;
        Country existingCountry = new Country();
        existingCountry.setId(id);

        Country updatedCountry = new Country();
        updatedCountry.setCode("NEW_CODE");

        when(repository.getById(id)).thenReturn(existingCountry);
        doNothing().when(repository).update(existingCountry);

        countryService.updateById(id, updatedCountry);

        verify(repository, times(1)).getById(id);
        verify(repository, times(1)).update(existingCountry);
        assertEquals("NEW_CODE", existingCountry.getCode());
    }

    @Test
    public void testUpdateByInvalidId() {
        Integer id = 0;
        Country updatedCountry = new Country();
        updatedCountry.setCode("NEW_CODE");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> countryService.updateById(id, updatedCountry));

        assertEquals("Invalid id or Country entity", exception.getMessage());

        verify(repository, never()).getById(any());
        verify(repository, never()).update(any());
    }

    @Test
    public void testUpdateWithNullEntity() {
        Integer id = 1;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> countryService.updateById(id, null));

        assertEquals("Invalid id or Country entity", exception.getMessage());

        verify(repository, never()).getById(any());
        verify(repository, never()).update(any());
    }

    @Test
    public void testUpdateNonExistingEntity() {
        Integer id = 1;
        Country updatedCountry = new Country();
        updatedCountry.setCode("NEW_CODE");

        when(repository.getById(id)).thenThrow(new RuntimeException("Entity not found"));

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> countryService.updateById(id, updatedCountry));

        assertEquals("Country with id " + id + " not found", exception.getMessage());
        verify(repository, times(1)).getById(id);
        verify(repository, never()).update(any());
    }

    @Test
    public void testGetItemsWithValidParameters() {
        int offset = 0;
        int limit = 10;
        List<Country> countries = Arrays.asList(new Country(), new Country());

        when(repository.getItems(offset, limit)).thenReturn(countries);

        List<Country> result = countryService.getItems(offset, limit);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(repository, times(1)).getItems(offset, limit);
    }

    @Test
    public void testGetItemsWithInvalidParameters() {
        int offset = -1;
        int limit = 0;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> countryService.getItems(offset, limit));

        assertEquals("Offset or limit cannot be less than 0", exception.getMessage());

        verify(repository, never()).getItems(offset, limit);
    }


    @Test
    public void testGetCount() {
        int count = 5;

        when(repository.getCount()).thenReturn(count);

        int result = countryService.getCount();

        assertEquals(count, result);
        verify(repository, times(1)).getCount();
    }
}
