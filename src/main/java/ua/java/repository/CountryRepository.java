package ua.java.repository;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.java.domain.entity.City;
import ua.java.domain.entity.Country;

import java.util.List;

public class CountryRepository implements CrudRepository<Country, Integer> {

    Logger logger = LoggerFactory.getLogger(CountryRepository.class);
    private final SessionFactory sessionFactory;

    public CountryRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public List<Country> getAll() {
        try(Session session = sessionFactory.getCurrentSession()) {
            List<Country> result;
            session.beginTransaction();
            result = session.createQuery("select c from Country c join fetch c.languages", Country.class).list();
            session.getTransaction().commit();
            return result;
        }
    }

    @Override
    public Country getById(Integer id) {
        try (Session session = sessionFactory.getCurrentSession()) {
            session.beginTransaction();
            Country result = sessionFactory.getCurrentSession().createQuery("select c from Country c join fetch c.languages where c.id = :ID", Country.class)
                    .setParameter("ID", id)
                    .getSingleResult();
            session.getTransaction().commit();
            return result;
        }
    }

    @Override
    public Country save(Country entity) {
        try (Session session = sessionFactory.getCurrentSession()) {
            session.beginTransaction();
            session.persist(entity);
            session.getTransaction().commit();
            logger.info("City entity saved");
            return entity;
        }
    }

    @Override
    public void deleteById(Integer id) {
        try (Session session = sessionFactory.getCurrentSession()) {
            session.beginTransaction();
            session.createQuery("delete from Country c where c.id = :ID")
                    .setParameter("ID", id);
            session.getTransaction().commit();
        }
    }

    @Override
    public void delete(Country entity) {
        try (Session session = sessionFactory.getCurrentSession()) {
            session.beginTransaction();
            session.delete(entity);
            session.getTransaction().commit();
        }
    }

    @Override
    public void update(Country entity) {
        try(Session session = sessionFactory.getCurrentSession()) {
            session.beginTransaction();
            session.update(entity);
            session.getTransaction().commit();
        }
    }

    @Override
    public List<Country> getItems(int offset, int limit) {
        try (Session session = sessionFactory.getCurrentSession()) {
            session.beginTransaction();
            List<Country> result = session.createQuery("select c from Country c", Country.class)
                    .setFirstResult(offset)
                    .setMaxResults(limit)
                    .list();
            session.getTransaction().commit();
            return result;
        }
    }

    public City getCityByCountryId(Integer countryId) {
        try (Session session = sessionFactory.getCurrentSession()) {
            session.beginTransaction();
            Country country = session.createQuery("select c from Country c left join fetch c.capital where c.id = :countryId", Country.class)
                    .setParameter("countryId", countryId)
                    .getSingleResult();
            session.getTransaction().commit();
            return country.getCapital();
        }
    }


    public int getCount() {
        try(Session session = sessionFactory.getCurrentSession()) {
            session.beginTransaction();
            Long result = session.createQuery("select count(c) from Country c", Long.class).uniqueResult();
            session.getTransaction().commit();
            return Math.toIntExact(result);
        }
    }
}
