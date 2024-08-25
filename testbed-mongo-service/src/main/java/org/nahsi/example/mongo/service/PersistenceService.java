package org.nahsi.example.mongo.service;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.conversions.Bson;
import org.hl7.fhir.r4.model.Patient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

import org.bson.Document;

import static com.mongodb.client.model.Filters.eq;

/**
 * @author Martin Smock
 */
@Service
public class PersistenceService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    // cloud mongo db
    String ppw = "";  // see mongo connection key;
    String uri = "mongodb://mongo-spring-app-account:"+ppw+"@mongo-spring-app-account.mongo.cosmos.azure.com:10255/?ssl=true&retrywrites=false&replicaSet=globaldb&maxIdleTimeMS=120000&appName=@mongo-spring-app-account@";
    String databaseName = "dbautoscale";
    String collectionName = "test-collection";

    /*
    // local mongo db
    String uri = "mongodb://localhost:27017";
    String databaseName = "poc-test";
    String collectionName = "patients";
    */

    /**
     * Search a patient from DB by fhir id
     *
     * @param id the fhir id of the patient object
     * @return HAPI Patient object
     */
    public Patient getById(String id) {

        var patient = new Patient();

        try (MongoClient mongoClient = MongoClients.create(uri)) {

            var database = mongoClient.getDatabase(databaseName);
            var collection = database.getCollection(collectionName);

            var doc = collection.find(eq("id", id)).first();

            if (doc != null) {
                FhirContext ctx = FhirContext.forR4();
                IParser parser = ctx.newJsonParser();
                patient = parser.parseResource(Patient.class, doc.toJson());
            }
        }

        return patient;
    }

    /**
     * Save hapi patient in DB
     *
     * @param patient HAPI FHIR patient
     * @return the serialized object stored
     */
    public String create(Patient patient) {

        String serialized;

        try (MongoClient mongoClient = MongoClients.create(uri)) {

            var database = mongoClient.getDatabase(databaseName);
            var collection = database.getCollection(collectionName);

            // check if the patient object with fhir id already exists
            String id = getUnqualifiedId(patient);
            if (collection.find(eq("id", id)).first() != null)
                return "Patient with id = " + id + " already exists.";

            // else
            FhirContext ctx = FhirContext.forR4();
            IParser parser = ctx.newJsonParser();
            serialized = parser.encodeResourceToString(patient);

            Document doc = Document.parse(serialized);
            InsertOneResult result = collection.insertOne(doc);

            logger.info("Accepted patient object: {}", result.getInsertedId());
        }

        return serialized;
    }

    /**
     * Update a hapi patient by using the fhir id
     *
     * @param patient the updated hapi patient
     * @return a report
     */
    public String update(Patient patient) {

        UpdateResult result;

        try (MongoClient mongoClient = MongoClients.create(uri)) {

            var database = mongoClient.getDatabase(databaseName);
            var collection = database.getCollection(collectionName);

            // convert to bson object
            FhirContext ctx = FhirContext.forR4();
            IParser parser = ctx.newJsonParser();
            String serialized = parser.encodeResourceToString(patient);
            Document doc = Document.parse(serialized);

            // filter for object to be replaced
            Bson query = eq("id", getUnqualifiedId(patient));

            // Instructs the driver to insert a new document if none match the query
            ReplaceOptions opts = new ReplaceOptions().upsert(true);

            // Replaces the first document that matches the filter with a new document
            result = collection.replaceOne(query, doc, opts);

            // log the number of modified documents and the upserted document ID, if an upsert was performed
            logger.info("Modified document count: {}", result.getModifiedCount());
            logger.info("Upserted id: {}", result.getUpsertedId());
        }

        return "Updated " + result.getModifiedCount() + " objects, and inserted object with id " + result.getUpsertedId();
    }

    /**
     * Delete a object in DB by fhir id
     *
     * @param id the fhir id of the object to be deleted
     * @return a report
     */
    public String delete(String id) {

        DeleteResult result;

        try (MongoClient mongoClient = MongoClients.create(uri)) {

            var database = mongoClient.getDatabase(databaseName);
            var collection = database.getCollection(collectionName);

            // filter for object to be deleted
            Bson query = eq("id", id);

            result = collection.deleteOne(query);
            logger.info("Deleted document count: {} ", result.getDeletedCount());
        }

        return "Deleted "+result.getDeletedCount()+" objects.";
    }

    /**
     * Search a patient from DB by family name
     *
     * @param name the family name of the patient object
     * @return HAPI Patient object
     */
    public Patient getByName(String name) {

        var patient = new Patient();

        try (MongoClient mongoClient = MongoClients.create(uri)) {

            var database = mongoClient.getDatabase(databaseName);
            var collection = database.getCollection(collectionName);

            var doc = collection.find(eq("name.family", name)).first();

            if (doc != null) {
                FhirContext ctx = FhirContext.forR4();
                IParser parser = ctx.newJsonParser();
                patient = parser.parseResource(Patient.class, doc.toJson());
            }
        }

        return patient;
    }


    /**
     * extract the fhir id from the qualified id (e.g., Patient/12345)
     *
     * @param patient the hapi patient
     * @return the unqualified id
     */
    private String getUnqualifiedId(Patient patient) {
        String[] idParts = patient.getId().split("/");
        if (idParts.length == 2 && idParts[0].equals("Patient"))
            return patient.getId().split("/")[1];
        return null;
    }

}
