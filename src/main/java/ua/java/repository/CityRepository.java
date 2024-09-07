package ua.java.repository;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import ua.java.domain.entity.City;

import java.util.List;

public class CityRepository implements CrudRepository<City, Integer> {

    private final SessionFactory sessionFactory;

    public CityRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public List<City> getAll() {
        try (Session session = sessionFactory.getCurrentSession()) {
            session.beginTransaction();
            List<City> result = session.createQuery("select c from City c", City.class).list();
            session.getTransaction().commit();
            return result;
        }
    }

    @Override
    public City getById(Integer id) {
        try (Session session = sessionFactory.getCurrentSession()) {
            session.beginTransaction();
            City result = sessionFactory.getCurrentSession().createQuery("select c from City c join fetch c.country where c.id = :ID", City.class)
                    .setParameter("ID", id)
                    .getSingleResult();
            session.getTransaction().commit();
            return result;
        }
    }

    @Override
    public City save(City entity) {
        try (Session session = sessionFactory.getCurrentSession()) {
            session.beginTransaction();
            session.persist(entity);
            session.getTransaction().commit();
            return entity;
        }
    }

    @Override
    public void deleteById(Integer id) {
        try (Session session = sessionFactory.getCurrentSession()) {
            session.beginTransaction();
            session.createQuery("delete from City where id = :ID")
                    .setParameter("ID", id)
                    .executeUpdate();
            session.getTransaction().commit();
        }
    }

    @Override
    public void delete(City entity) {
        try (Session session = sessionFactory.getCurrentSession()) {
            session.beginTransaction();
            session.delete(entity);
            session.getTransaction().commit();
        }
    }

    @Override
    public void update(City entity) {
        try(Session session = sessionFactory.getCurrentSession()) {
            session.beginTransaction();
            session.update(entity);
            session.getTransaction().commit();
        }
    }

    @Override
    public List<City> getItems(int offset, int limit) {
        try (Session session = sessionFactory.getCurrentSession()) {
            session.beginTransaction();
            List<City> result = session.createQuery("select c from City c", City.class)
                    .setFirstResult(offset)
                    .setMaxResults(limit)
                    .list();
            session.getTransaction().commit();
            return result;
        }
    }

    public int getCount() {
        try(Session session = sessionFactory.getCurrentSession()) {
            session.beginTransaction();
            Long result = session.createQuery("select count(c) from City c", Long.class).uniqueResult();
            session.getTransaction().commit();
            return Math.toIntExact(result);
        }
    }
}