package com.example.testbrick.config;

import com.example.testbrick.constant.StaticParams;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
public class SeleniumConfig {

    @PostConstruct
    void postConstruct() {
        System.setProperty("webdriver.chrome.driver", "chromedriver.exe");
    }

    @Bean
    public ChromeDriver driver() {
        final ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.setHeadless(true);
        chromeOptions.addArguments(StaticParams._USER_AGENT); // trick to make headless work
        return new ChromeDriver();
    }

}
