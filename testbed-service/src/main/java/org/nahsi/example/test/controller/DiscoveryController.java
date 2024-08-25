package org.nahsi.example.test.controller;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.nahsi.example.test.service.DiscoveryService;

import java.util.List;

/**
 * Controller to verify that the service is registered in the discovery service by service name.
 *
 * @author Martin Smock
 */
@RestController
public class DiscoveryController {

    @Autowired
    private DiscoveryService discoveryService;

	private final String defaultValue = "poc-app-service";

    @GetMapping("/discovery")
    public ResponseEntity<List<ServiceInstance>> service(
            @RequestParam(value = "name", defaultValue = defaultValue) String name) {
        return ResponseEntity.ok(discoveryService.servicesByName(name));
	}

}
