package com.javarush.sberezovskii.dao;

import com.javarush.sberezovskii.domain.Country;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import java.util.List;

public class CountryDAO extends AbstractDAO<Country> {
    public CountryDAO(SessionFactory sessionFactory) {
        super(Country.class, sessionFactory);
    }

    @Override
    public List<Country> getAll() {
        Query<Country> objectList = getCurrentSession()
                .createQuery("select c from Country c join fetch c.languages" , Country.class);
        return objectList.list();
    }
}
