package com.javarush.sberezovskii.dao;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import java.util.List;


public class AbstractDAO<T>{

    private Class<T> clazz;
    private SessionFactory sessionFactory;

    public AbstractDAO(Class<T> clazz, SessionFactory sessionFactory) {
        this.clazz = clazz;
        this.sessionFactory = sessionFactory;
    }

    public T getByID(int id){
           return getCurrentSession().get(clazz, id);
    }

    public List<T> getAll(){
        Query<T> objectList = getCurrentSession()
                .createQuery("from " + clazz.getName(), clazz);
        return objectList.list();
    }

    public List<T> getItems(int offset, int count){
        Query<T> objectList = getCurrentSession()
                .createQuery("from " + clazz.getName(), clazz);
        objectList.setFirstResult(offset);
        objectList.setMaxResults(count);
        return objectList.list();
    }

    public int getTotalCounts(){
        Query<Long> objectList = getCurrentSession()
                .createQuery("select count(c) from " + clazz.getName() + " c", Long.class);
        return Math.toIntExact(objectList.uniqueResult());
    }

    public void deleteById(int id){
        T object = getByID(id);
        getCurrentSession().delete(object);
    }

    public void delete(final T entity){
        getCurrentSession().delete(entity);
    }



    public T save(final T entity){
        getCurrentSession().saveOrUpdate(entity);
        return entity;
    }

    public T update(final T entity){
        getCurrentSession().merge(entity);
        return entity;
    }

    protected Session getCurrentSession() {
        return sessionFactory.getCurrentSession();
    }
}
