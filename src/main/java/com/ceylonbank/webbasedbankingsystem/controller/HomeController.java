package com.ceylonbank.webbasedbankingsystem.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String index() {
        return "index"; // Loads templates/index.html
    }



    @GetMapping("/access-denied")
    public String accessDenied() {
        return "access-denied"; // Loads templates/access-denied.html
    }
}