package com.javarush.sberezovskii.dao;

import com.javarush.sberezovskii.domain.CountryLanguage;
import org.hibernate.SessionFactory;

public class CountryLanguageDAO extends AbstractDAO<CountryLanguage> {
    public CountryLanguageDAO(SessionFactory sessionFactory) {
        super(CountryLanguage.class, sessionFactory);
    }
}
