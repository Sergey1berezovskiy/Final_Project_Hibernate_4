package com.javarush.sberezovskii.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.javarush.sberezovskii.configs.MySessionFactory;
import com.javarush.sberezovskii.constants.Constants;
import com.javarush.sberezovskii.dao.CityDAO;
import com.javarush.sberezovskii.dao.CountryDAO;
import com.javarush.sberezovskii.domain.City;
import com.javarush.sberezovskii.domain.Country;
import com.javarush.sberezovskii.domain.CountryLanguage;
import com.javarush.sberezovskii.redis.CityCountry;
import com.javarush.sberezovskii.redis.Language;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisStringCommands;
import lombok.Getter;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;

@Getter
public class Combiner {
    private final RedisClient redisClient;
    private final ObjectMapper mapper;
    private final CityDAO cityDAO;
    private final CountryDAO countryDAO;
    private final MySessionFactory mySessionFactory;


    public Combiner(MySessionFactory mySessionFactory) {
        this.mySessionFactory = mySessionFactory;
        this.cityDAO = new CityDAO(getSessionFactory());
        this.countryDAO = new CountryDAO(getSessionFactory());
        mapper = new ObjectMapper();
        redisClient = preparedRedisClient();
    }

    public void start() {
        List<City> allCities = fetchData();
        List<CityCountry> preparedData = transformData(allCities);
        pushToRedis(preparedData);

        getSessionFactory().getCurrentSession().close();
        List<Integer> ids = List.of(10, 4070, 256, 79, 189, 89, 2508, 1500, 1999, 5);

        long startRedis = System.currentTimeMillis();
        testRedisData(ids);
        long stopRedis = System.currentTimeMillis();
        long startMySQL = System.currentTimeMillis();
        testMySQLData(ids);
        long stopMySQL = System.currentTimeMillis();

        System.out.printf("%s:\t%d ms\n", "Redis", (stopRedis - startRedis));
        System.out.printf("%s:\t%d ms\n", "MySQL", (stopMySQL - startMySQL));

    }

    private SessionFactory getSessionFactory(){
        return mySessionFactory.getSessionFactory();
    }

    private List<City> fetchData(){
        try(Session session = getSessionFactory().getCurrentSession()){
            session.beginTransaction();
            List<Country> countries = countryDAO.getAll();
            List<City> cities = new ArrayList<>();

            int totalCount = cityDAO.getTotalCounts();
            int step = Constants.STEP_FOR_GETTING_CITIES;
            for(int i = 0; i < totalCount; i+=step){
                cities.addAll(cityDAO.getItems(i, step));
            }
            session.getTransaction().commit();
            return cities;
        }
    }

    private List<CityCountry> transformData(List<City> cities) {
        return cities.stream().map(city -> {
            CityCountry res = new CityCountry();
            res.setId(city.getId());
            res.setName(city.getName());
            res.setPopulation(city.getPopulation());
            res.setDistrict(city.getDistrict());

            Country country = city.getCountry();
            res.setCode2(country.getCode2());
            res.setContinent(country.getContinent());
            res.setCode(country.getCode());
            res.setCountryName(country.getName());
            res.setCountryPopulation(country.getPopulation());
            res.setCountryRegion(country.getRegion());
            res.setCountrySurfaceArea(country.getSurfaceArea());
            Set<CountryLanguage> countryLanguageSet = country.getLanguages();

            Set<Language> languages = countryLanguageSet.stream()
                    .map(countryLanguage -> {
                        Language language = new Language();
                        language.setLanguage(countryLanguage.getLanguage());
                        language.setIsOfficial(countryLanguage.getIsOfficial());
                        language.setPercentage(countryLanguage.getPercentage());
                        return language;
                    }).collect(Collectors.toSet());
            res.setLanguages(languages);
            return res;
        }).collect(Collectors.toList());

    }

    private RedisClient preparedRedisClient() {
        RedisClient redisClient = RedisClient.create(RedisURI.create("localhost", 6379));
        try(StatefulRedisConnection<String, String> connection = redisClient.connect()){
            System.out.println("\nConnected to Redis\n");
        }
        return redisClient;
    }

    private void pushToRedis(List<CityCountry> preparedData) {
        try(StatefulRedisConnection<String, String> connection = redisClient.connect()){
            RedisStringCommands<String, String> sync = connection.sync();
            for (CityCountry cityCountry : preparedData) {
                try{
                    sync.set(String.valueOf(cityCountry.getId()), mapper.writeValueAsString(cityCountry));
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void shutdown() {
        if (nonNull(getSessionFactory())) {
            getSessionFactory().close();
        }
        if (nonNull(redisClient)) {
            redisClient.shutdown();
        }
    }

    private void testRedisData(List<Integer> ids){
        try(StatefulRedisConnection<String, String> connection = redisClient.connect()){
            RedisStringCommands<String, String> sync = connection.sync();
            for (Integer id : ids) {
                String value = String.valueOf(id);
                try{
                    mapper.readValue(value, CityCountry.class);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    private void testMySQLData(List<Integer> ids){
        try(Session session = getSessionFactory().getCurrentSession()){
            session.beginTransaction();
            for (Integer id : ids) {
                City city = cityDAO.getByID(id);
                Set<CountryLanguage> languages = city.getCountry().getLanguages();
            }
            session.getTransaction().commit();
        }
    }

}
