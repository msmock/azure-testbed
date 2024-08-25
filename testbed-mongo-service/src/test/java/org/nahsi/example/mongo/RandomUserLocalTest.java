package org.nahsi.example.mongo;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.hl7.fhir.r4.model.*;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.UUID;

import static com.mongodb.client.model.Filters.eq;

/**
 * Create patient test data from randomuser.me stored in a file in classpath
 */
public class RandomUserLocalTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    @Test
    public void createRandomUsers() throws IOException, ParseException {

        IParser iParser = FhirContext.forR4().newJsonParser();

        MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
        MongoDatabase database = mongoClient.getDatabase("poc-test");

        MongoCollection<Document> collection = database.getCollection("patients");
        collection.drop();

        // create a new collection
        database.createCollection("patients");

        ObjectMapper objectMapper = new ObjectMapper();

        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("randomuser-500.json").getFile());
        JsonNode rootNode = objectMapper.readTree(file);

        var count = 0;

        for (JsonNode node : rootNode.get("results")) {

            Patient patient = parseRandomUser(node);
            patient.setId(UUID.randomUUID().toString());
            String serialized = iParser.encodeResourceToString(patient);

            // filter for object to be replaced
            Bson query = eq("id", getUnqualifiedId(patient));

            // Instructs the driver to insert a new document if none match the query
            ReplaceOptions opts = new ReplaceOptions().upsert(true);

            // Replaces the first document that matches the filter with a new document
            Document bsonDoc = Document.parse(serialized);
            UpdateResult result = collection.replaceOne(query, bsonDoc, opts);

            count++;
        }

        logger.info("Added " + count + " patient object to DB.");

    }

    /**
     * @param node a jackson json node from parsing randomuser response
     * @return org. hl7.fhir. r4.model.Patient
     */
    private Patient parseRandomUser(JsonNode node) throws ParseException {

        String title = node.get("name").get("title").asText();
        String first = node.get("name").get("first").asText();
        String last = node.get("name").get("last").asText();

        String gender = node.get("gender").asText();

        String birthDate = node.get("dob").get("date").asText();
        String ahvn13 = node.get("id").get("value").asText();

        String street = node.get("location").get("street").get("name").asText();
        String house = node.get("location").get("street").get("number").asText();

        String city = node.get("location").get("city").asText();
        String state = node.get("location").get("state").asText();
        // String country = node.get("location").get("country").asText();
        String postCode = node.get("location").get("postcode").asText();

        var patient = new Patient();

        // set the name
        var fhirName = new HumanName();
        fhirName.setPrefix(List.of(new StringType(title)));
        fhirName.setFamily(last);
        fhirName.setGiven(List.of(new StringType(first)));
        patient.setName(List.of(fhirName));

        // gender
        var fhirGender = new Enumerations.AdministrativeGenderEnumFactory().fromCode(gender);
        patient.setGender(fhirGender);

        // birthdate
        patient.setBirthDate(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(birthDate));

        // identifier
        var identifier = new Identifier();
        identifier.setSystem("http://terminology.hl7.org/CodeSystem/v2-0203");
        identifier.setValue(ahvn13);
        patient.setIdentifier(List.of(identifier));

        // address
        var fhirAddress = new Address();
        fhirAddress.setCountry("CH"); // Switzerland
        fhirAddress.setState(state);
        fhirAddress.setCity(city);
        fhirAddress.setPostalCode(postCode);
        fhirAddress.setLine(List.of(new StringType(street + " " + house)));
        patient.addAddress(fhirAddress);

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