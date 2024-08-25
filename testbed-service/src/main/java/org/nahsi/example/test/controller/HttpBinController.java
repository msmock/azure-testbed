package org.nahsi.example.test.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

/** 
* calls an external service via internet which inspects the request.
*/
@RestController
@RequestMapping(value = "/httpbin")
public class HttpBinController {

  private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

  @GetMapping
  public ResponseEntity<String> retrieveData() {

    logger.info("Call httpbin.org to inspect the request ...");

    var uri = "https://httpbin.org/anything";

    try {

      RestClient restClient = RestClient.create();
      String responseAsString = restClient.get()
          .uri(uri)
          .retrieve()
          .body(String.class);

      logger.info("Received request inspection from hhtpbin.");

      return ResponseEntity.ok(responseAsString);
    
    } catch (Exception e) {
      logger.error(e.getMessage());
      return ResponseEntity.ok("Internal server error: " + e.getMessage());
    }

  }

}
