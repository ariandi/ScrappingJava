package com.example.testbrick.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TopedScrappingController {

    @GetMapping("/")
    public String index() {
        return "123";
    }

}
