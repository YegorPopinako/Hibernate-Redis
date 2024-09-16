package ua.java.repository;

import java.util.List;

public interface CrudRepository<T, I> {

    List<T> getAll();

    T getById(I id);

    T save(T entity);

    void deleteById(I id);

    void delete(T entity);

    void update(T entity);

    List<T> getItems(int offset, int limit);
}
