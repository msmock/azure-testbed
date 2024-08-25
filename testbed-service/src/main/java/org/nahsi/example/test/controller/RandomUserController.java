package org.nahsi.example.test.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

/** 
* calls an external RESTful service via internet.
*/
@RestController
@RequestMapping(value = "/randomuser")
public class RandomUserController {

  private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

  @GetMapping
  public ResponseEntity<String> retrieveData() {

    logger.info("Call randomuser.me for test data ...");

    try {

      RestClient restClient = RestClient.create();
      String responseAsString = restClient.get()
          .uri("https://randomuser.me/api/?nat=ch")
          .retrieve()
          .body(String.class);

      logger.info("Received test data from randomuser.me.");

      return ResponseEntity.ok(responseAsString);
    
    } catch (Exception e) {
      logger.error(e.getMessage());
      return ResponseEntity.ok("Internal server error: " + e.getMessage());
    }

  }

}
