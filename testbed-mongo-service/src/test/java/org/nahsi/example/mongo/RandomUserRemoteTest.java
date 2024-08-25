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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import javax.print.Doc;
import java.io.File;
import java.io.IOException;
import java.sql.Array;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.mongodb.client.model.Filters.eq;

/**
 * Create patient test data from randomuser.me stored in a file in classpath
 */
public class RandomUserRemoteTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Test
    public void createRandomUsers() throws IOException, ParseException, InterruptedException {

        var iParser = FhirContext.forR4().newJsonParser();

        // mongo stuff
        var ppw = "";  // see mongo connection key;
        var uri = "mongodb://mongo-spring-app-account:"+ppw+"@mongo-spring-app-account.mongo.cosmos.azure.com:10255/?ssl=true&retrywrites=false&replicaSet=globaldb&maxIdleTimeMS=120000&appName=@mongo-spring-app-account@";


        // parsing
        var objectMapper = new ObjectMapper();

        var classLoader = getClass().getClassLoader();
        var file = new File(classLoader.getResource("randomuser-500.json").getFile());
        var rootNode = objectMapper.readTree(file);

        List<Document> bsonDocs = new ArrayList<>();

        for (JsonNode node : rootNode.get("results")) {
            Patient patient = parseRandomUser(node);
            patient.setId(UUID.randomUUID().toString());
            var serialized = iParser.encodeResourceToString(patient);
            bsonDocs.add(Document.parse(serialized));
        }

        List<Document> bucket = new ArrayList<>();
        int j = 0;

        for (int i = j; i < bsonDocs.size(); i++) {

            bucket.add(bsonDocs.get(i));

            if (i % 10 == 0) {

                logger.info("pushed bucket ...");

                var mongoClient = MongoClients.create(uri);
                var database = mongoClient.getDatabase("dbautoscale");
                MongoCollection<Document> collection = database.getCollection("test-collection");
                collection.insertMany(bucket);
                mongoClient.close();

                j = +1;

                TimeUnit.MINUTES.sleep(5);
                bucket = new ArrayList<>();
            }

        }

        // collection.insertMany(bsonDocs);

        logger.info("Added " + bsonDocs.size() + " patient object to DB.");

    }

    /**
     * @param node a jackson json node from parsing randomuser response
     * @return org. hl7.fhir. r4.model.Patient
     */
    private Patient parseRandomUser(JsonNode node) throws ParseException {

        var title = node.get("name").get("title").asText();
        var first = node.get("name").get("first").asText();
        var last = node.get("name").get("last").asText();

        var gender = node.get("gender").asText();

        var birthDate = node.get("dob").get("date").asText();
        var ahvn13 = node.get("id").get("value").asText();

        var street = node.get("location").get("street").get("name").asText();
        var house = node.get("location").get("street").get("number").asText();

        var city = node.get("location").get("city").asText();
        var state = node.get("location").get("state").asText();
        // String country = node.get("location").get("country").asText();
        var postCode = node.get("location").get("postcode").asText();

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


}