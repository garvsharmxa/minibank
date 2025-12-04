package com.minibank.authservice.Controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/")
    public String sayHello(HttpServletRequest request) {
        return "Hello World " + request.getSession().getId();
    }

}
