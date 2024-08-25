package org.nahsi.example.mongo.controller;

import ca.uhn.fhir.context.FhirContext;
import org.hl7.fhir.r4.model.Patient;
import org.nahsi.example.mongo.service.PersistenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Manage data in FHIR format in mongo db.
 *
 * @author Martin Smock
 */
@RestController
@RequestMapping(value = "/persistence/byname")
public class PersistenceByNameController {

    @Autowired
    private PersistenceService service;

    @RequestMapping(value = "/{name}", method = RequestMethod.GET)
    public ResponseEntity<String> getById(@PathVariable("name") String name) {
        Patient patient = service.getByName(name);
        String serialized = FhirContext.forR4().newJsonParser().encodeResourceToString(patient);
        return ResponseEntity.ok(serialized);
    }

}