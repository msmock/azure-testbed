package org.nahsi.example.test.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller to verify if the configuration is dynamically loaded from the config server
 *
 * @see org.nahsi.example.test.config.ConfigClientAutoRefreshConfiguration
 */
@RefreshScope
@RestController
public class PropertyTestController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    @Value("${testProperty}")
    private String myProperty;

    @GetMapping("/testproperty")
    public ResponseEntity<String> hello() {

        logger.info("Test property is {}", myProperty);
        return ResponseEntity.ok("Test property is: "+myProperty);

    }

}
