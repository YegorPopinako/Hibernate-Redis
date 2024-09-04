package ua.java.repository;

import org.hibernate.SessionFactory;
import ua.java.domain.entity.City;

public class CityRepository implements CrudRepository<City, Integer> {

    private final SessionFactory sessionFactory;

    public CityRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public City getById(Integer id) {
        return null;
    }

    @Override
    public City save(City entity) {
        return null;
    }

    @Override
    public void deleteById(Integer id) {

    }

    @Override
    public void delete(City entity) {

    }

    @Override
    public void update(City entity) {

    }

    @Override
    public void updateById(Integer id, City entity) {

    }
}
