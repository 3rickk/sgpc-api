package br.com.sgpc.sgpc_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class SgpcApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(SgpcApiApplication.class, args);
    }
}

@RestController
@RequestMapping("/api")
class HelloWorldController {

    @GetMapping("/hello")
    public String helloWorld() {
        return "Hello, SGPC!";
    }
}