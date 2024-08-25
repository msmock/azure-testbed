package org.nahsi.example.mongo.controller;

import org.hl7.fhir.r4.model.Patient;
import org.nahsi.example.mongo.service.PersistenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import ca.uhn.fhir.context.FhirContext;

/**
 * Manage data in FHIR format in mongo db.
 *
 * @author Martin Smock
 */
@RestController
@RequestMapping(value = "/persistence")
public class PersistenceController {

    @Autowired
    private PersistenceService service;

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public ResponseEntity<String> getById(@PathVariable("id") String id) {
        Patient patient = service.getById(id);
        String serialized = FhirContext.forR4().newJsonParser().encodeResourceToString(patient);
        return ResponseEntity.ok(serialized);
    }

    @PostMapping
    public ResponseEntity<String> create(@RequestBody String request) {
        Patient patient = FhirContext.forR4().newJsonParser().parseResource(Patient.class, request);
        return ResponseEntity.ok(service.create(patient));
    }

    @PutMapping
    public ResponseEntity<String> update(@RequestBody String request) {
        Patient patient = FhirContext.forR4().newJsonParser().parseResource(Patient.class, request);
        return ResponseEntity.ok(service.update(patient));
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<String> delete(@PathVariable("id") String id) {
        return ResponseEntity.ok(service.delete(id));
    }
}