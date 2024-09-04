package ua.java.repository;

import org.hibernate.SessionFactory;
import ua.java.domain.entity.Country;

public class CountryRepository implements CrudRepository<Country, Integer> {

    private final SessionFactory sessionFactory;

    public CountryRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }
    @Override
    public Country getById(Integer id) {
        return null;
    }

    @Override
    public Country save(Country entity) {
        return null;
    }

    @Override
    public void deleteById(Integer id) {

    }

    @Override
    public void delete(Country entity) {

    }

    @Override
    public void update(Country entity) {

    }

    @Override
    public void updateById(Integer id, Country entity) {

    }
}
