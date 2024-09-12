package ua.java;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisStringCommands;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import ua.java.cache.RedisRepository;
import ua.java.domain.entity.City;
import ua.java.domain.entity.Country;
import ua.java.domain.entity.CountryLanguage;
import ua.java.redis.CityCountry;
import ua.java.redis.Language;
import ua.java.repository.CityRepository;
import ua.java.repository.CountryRepository;
import ua.java.service.CityService;
import ua.java.service.CountryService;
import ua.java.util.HibernateUtil;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Main {

    public static void main(String[] args) {
        RedisClient redisClient = prepareRedisClient();
        ObjectMapper mapper = new ObjectMapper();
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        RedisRepository redisRepository = new RedisRepository();

        CountryRepository countryRepository = new CountryRepository(sessionFactory);
        CityRepository repository = new CityRepository(sessionFactory);

        CountryService countryService = new CountryService(countryRepository, redisRepository);
        CityService service = new CityService(repository, redisRepository);

        service.getById(3);
        service.getById(3);
        service.getById(3);

        /*Country existingCountry = new Country();
        existingCountry.setCode("UA");
        existingCountry.setSecondCode("entity.getSecondCode()");
        existingCountry.setName("entity.getName()");
        existingCountry.setContinent(Continent.AFRICA);
        existingCountry.setRegion("entity.getRegion()");
        existingCountry.setSurfaceArea(BigDecimal.ONE);
        existingCountry.setIndependenceYear((short)11);
        existingCountry.setPopulation(111);
        existingCountry.setLifeExpectancy(BigDecimal.ONE);
        existingCountry.setGnp(BigDecimal.ONE);
        existingCountry.setGnpoId(BigDecimal.valueOf(1));
        existingCountry.setLocalName("entity.getLocalName()");
        existingCountry.setGovernmentForm("entity.getGovernmentForm()");
        existingCountry.setHeadOfState("entity.getHeadOfState()");
        existingCountry.setCapital(new City());
        existingCountry.setLanguages(Set.of(new CountryLanguage()));*/

        /*City city = new City();
        city.setName("Kyiv");
        city.setDistrict("Kyiv");
        city.setPopulation(100000);
        city.setCountry(country);
        service.save(city);*/

        /*service.deleteById(0);*/

        /*List<City> allCities = repository.getAll();
        List<CityCountry> preparedData = transformData(allCities);
        pushToRedis(preparedData, redisClient, mapper);*/
        /*List<Integer> ids = new ArrayList<>(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));

        long startRedis = System.currentTimeMillis();
        testRedisData(ids, redisClient, mapper);
        long stopRedis = System.currentTimeMillis();

        long startMySql = System.currentTimeMillis();
        testMysqlData(ids, sessionFactory, repository);
        long stopMYSql = System.currentTimeMillis();

        System.out.printf("%s:\t%d ms\n", "Redis", (stopRedis - startRedis));
        System.out.printf("%s:\t%d ms\n", "MySQL", (stopMYSql - startMySql));*/
    }

    private static List<CityCountry> transformData(List<City> cities) {
        return cities.stream().map(city -> {
            CityCountry res = new CityCountry();
            res.setId(city.getId());
            res.setName(city.getName());
            res.setPopulation(city.getPopulation());
            res.setDistrict(city.getDistrict());

            Country country = city.getCountry();
            res.setAlternativeCountryCode(country.getSecondCode());
            res.setContinent(country.getContinent());
            res.setCountryCode(country.getCode());
            res.setCountryName(country.getName());
            res.setCountryPopulation(country.getPopulation());
            res.setCountryRegion(country.getRegion());
            res.setCountrySurfaceArea(country.getSurfaceArea());

            Set<CountryLanguage> countryLanguages = country.getLanguages();
            Set<Language> languages = countryLanguages.stream().map(cl -> {
                Language language = new Language();
                language.setLanguage(cl.getLanguage());
                language.setOfficial(cl.getOfficial());
                language.setPercentage(cl.getPercentage());
                return language;
            }).collect(Collectors.toSet());
            res.setLanguages(languages);

            return res;
        }).collect(Collectors.toList());
    }

    private static RedisClient prepareRedisClient() {
        RedisClient redisClient = RedisClient.create(RedisURI.create("localhost", 6379));
        try (StatefulRedisConnection<String, String> connection = redisClient.connect()) {
            System.out.println("\nConnected to Redis\n");
        }
        return redisClient;
    }

    private static void pushToRedis(List<CityCountry> data, RedisClient redisClient, ObjectMapper mapper) {
        try (StatefulRedisConnection<String, String> connection = redisClient.connect()) {
            RedisStringCommands<String, String> sync = connection.sync();
            for (CityCountry cityCountry : data) {
                try {
                    sync.set(String.valueOf(cityCountry.getId()), mapper.writeValueAsString(cityCountry));
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static void testRedisData(List<Integer> ids, RedisClient redisClient, ObjectMapper mapper) {
        try (StatefulRedisConnection<String, String> connection = redisClient.connect()) {
            RedisStringCommands<String, String> sync = connection.sync();
            for (Integer id : ids) {
                String value = sync.get(String.valueOf(id));
                try {
                    mapper.readValue(value, CityCountry.class);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static void testMysqlData(List<Integer> ids, SessionFactory sessionFactory, CityRepository cityDAO) {
        try (Session session = sessionFactory.getCurrentSession()) {
            session.beginTransaction();
            for (Integer id : ids) {
                City city = cityDAO.getById(id);
                Set<CountryLanguage> languages = city.getCountry().getLanguages();
            }
            session.getTransaction().commit();
        }
    }
}
