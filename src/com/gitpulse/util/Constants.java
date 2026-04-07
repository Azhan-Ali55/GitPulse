package com.gitpulse.util;

import java.io.FileInputStream;
import java.util.Properties;

public class Constants {
    public static final String BASE_URL = "https://api.github.com";
    public static final int TIMEOUT = 10000;
    public static final String TOKEN = loadToken();

    private static String loadToken() {
        try {
            Properties props = new Properties();
            props.load(new FileInputStream("config.properties"));
            return props.getProperty("github.token");
        } catch (Exception e) {
            System.err.println("Token not found");
            return "";
        }
    }
}