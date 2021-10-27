package com.example.api.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RequestMapping("/api/v1/test")
@RestController
public class AuthController {

    @GetMapping("/permit-all")
    public Object getTest() throws Exception {
        return "permit";
    }

    @GetMapping("/auth")
    public Object getTest2() throws Exception {
        return "auth";
    }

}