package org.nahsi.example.test.controller;

import org.nahsi.example.test.service.DiscoveryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller to test the access to the key vault.
 *
 * @author Martin Smock
 */
@RestController
public class VaultController {

    // annotation should match the key of the object stored in vault
    @Value("${connection}")
    private String secret;

    @GetMapping("/vault")
    public ResponseEntity<String> service() {

        var display = String.format("Connection String stored in Azure Key Vault:\n%s\n", secret);
        return ResponseEntity.ok(display);
	}

}
