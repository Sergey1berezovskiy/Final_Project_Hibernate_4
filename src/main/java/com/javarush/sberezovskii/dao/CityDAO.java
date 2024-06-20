package com.javarush.sberezovskii.dao;

import com.javarush.sberezovskii.domain.City;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

public class CityDAO extends AbstractDAO<City> {

    public CityDAO(SessionFactory sessionFactory) {
        super(City.class, sessionFactory);
    }

    @Override
    public City getByID(int id) {
        Query<City> city = getCurrentSession()
                .createQuery("select c from City c join fetch c.country where c.id = :id", City.class);
        city.setParameter("id", id);

        return city.getSingleResult();
    }
}
