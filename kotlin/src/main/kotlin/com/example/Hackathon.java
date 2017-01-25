package com.example;

public class Hackathon {

    private static String rate;

    public static String getFixedRate() {
        System.out.println("Getting hacked rate from store " + rate);
        return rate;
    }

    public static void setFixedRate(final String rate) {
        System.out.println("Hacking rate into store " + rate);
        Hackathon.rate = rate;
    }
}
