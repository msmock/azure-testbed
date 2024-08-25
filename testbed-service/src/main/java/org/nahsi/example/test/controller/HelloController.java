package org.nahsi.example.test.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    @GetMapping("/")
    public ResponseEntity<String> hello() {
        logger.info("Received incoming request");
        return ResponseEntity.ok("Thanks for the request. I'm alive!");
    }

}
