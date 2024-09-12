package ua.java.redis;

import ua.java.domain.entity.City;
import ua.java.domain.entity.Country;
import ua.java.domain.entity.CountryLanguage;

import java.util.Set;
import java.util.stream.Collectors;

public class DataTransformer {

    public static CityCountry transformDataToJson(City city) {

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
    }

    public static City fromJsonToCityEntity(CityCountry cityCountry) {
        City city = new City();
        city.setId(cityCountry.getId());
        city.setName(cityCountry.getName());
        city.setPopulation(cityCountry.getPopulation());
        city.setDistrict(cityCountry.getDistrict());
        return city;
    }

    public static Country fromJsonToCountryEntity(CityCountry cityCountry) {
        Country country = new Country();
        country.setId(cityCountry.getId());
        country.setCode(cityCountry.getCountryCode());
        country.setName(cityCountry.getCountryName());
        country.setContinent(cityCountry.getContinent());
        country.setRegion(cityCountry.getCountryRegion());
        country.setSurfaceArea(cityCountry.getCountrySurfaceArea());
        country.setPopulation(cityCountry.getCountryPopulation());
        country.setSecondCode(cityCountry.getAlternativeCountryCode());
        return country;
    }
}
