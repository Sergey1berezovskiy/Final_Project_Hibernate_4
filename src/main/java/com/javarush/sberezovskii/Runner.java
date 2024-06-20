package com.javarush.sberezovskii;
import com.javarush.sberezovskii.configs.MySessionFactory;
import com.javarush.sberezovskii.controller.Combiner;

public class Runner {

    public static void main(String[] args) {
        MySessionFactory mySessionFactory = new MySessionFactory();
        Combiner combiner = new Combiner(mySessionFactory);
        combiner.start();
        combiner.shutdown();

    }
}