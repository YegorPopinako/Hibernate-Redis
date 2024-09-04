package ua.java.repository;

public interface CrudRepository<T, I> {

    T getById(I id);

    T save(T entity);

    void deleteById(I id);

    void delete(T entity);

    void update(T entity);

    void updateById(I id, T entity);
}
