package org.grobid.service.controller;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.io.JsonStringEncoder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.grobid.core.data.Dataset;
import org.grobid.core.data.Dataset.DatasetType;
import org.grobid.core.engines.DataseerClassifier;
import org.grobid.core.engines.DatasetParser;
import org.grobid.service.configuration.DatastetConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

import static org.grobid.service.controller.DatastetServiceUtils.isResultOK;

/**
 * @author Patrice
 */
@Singleton
public class DatastetProcessString {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatastetProcessString.class);

    private final DatastetConfiguration datastetConfiguration;
    private final DataseerClassifier dataseerClassifier;
    private final DatasetParser datasetParser;

    @Inject
    public DatastetProcessString(DatastetConfiguration configuration,
                                 DatasetParser datasetParser,
                                 DataseerClassifier dataseerClassifier) {

        this.datasetParser = datasetParser;
        this.dataseerClassifier = dataseerClassifier;
        this.datastetConfiguration = configuration;
    }

    /**
     * Determine if a provided sentence introduces a dataset and classify the type of the dataset.
     *
     * @param text raw sentence string
     * @return a json response object containing the information related to possible dataset
     */
    public Response processDataseerSentence(String text) {
        LOGGER.debug(methodLogIn());
        Response response = null;
        try {
            LOGGER.debug(">> set raw sentence text for stateless service'...");

            text = text.replaceAll("\\n", " ").replaceAll("\\t", " ");
            long start = System.currentTimeMillis();
            String retValString = this.dataseerClassifier.classify(text);
            long end = System.currentTimeMillis();

            // TBD: update json with runtime and software/version 

            if (!isResultOK(retValString)) {
                response = Response.status(Response.Status.NO_CONTENT).build();
            } else {
                response = Response.status(Response.Status.OK).entity(retValString).type(MediaType.TEXT_PLAIN).build();
            }
        } catch (NoSuchElementException nseExp) {
            LOGGER.error("Could not get an instance of DataseerClassifier. Sending service unavailable.");
            response = Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
        } catch (Exception e) {
            LOGGER.error("An unexpected exception occurs. ", e);
            response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
        LOGGER.debug(methodLogOut());
        return response;
    }

    public Response processDataseerSentences(String sentencesAsJson) {
        LOGGER.debug(methodLogIn());
        Response response = null;
        StringBuilder retVal = new StringBuilder();
        try {
            LOGGER.debug(">> set raw sentence text for stateless service'...");

            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
            JsonNode jsonNodes = null;

            try {
                jsonNodes = mapper.readTree(sentencesAsJson);
            } catch (IOException ex) {
                throw new RuntimeException("Cannot parse input JSON. " + Response.Status.BAD_REQUEST);
            }
            if (jsonNodes == null || jsonNodes.isMissingNode()) {
                throw new RuntimeException("The request is invalid or malformed." + Response.Status.BAD_REQUEST);
            }

            List<String> texts = new ArrayList<>();
            for (JsonNode node : jsonNodes) {
                String text = node.asText();
                text = text.replaceAll("\\n", " ").replaceAll("\\t", " ");
                texts.add(text);
            }

//            List<String> textsNormalized = texts.stream()
//                    .map(text -> text.replaceAll("\\n", " ").replaceAll("\\t", " "))
//                    .collect(Collectors.toList());

            long start = System.currentTimeMillis();
            String retValString = this.dataseerClassifier.classify(texts);
            long end = System.currentTimeMillis();

            if (!isResultOK(retValString)) {
                response = Response.status(Response.Status.NO_CONTENT).build();
            } else {
                response = Response.status(Response.Status.OK).entity(retValString).type(MediaType.TEXT_PLAIN).build();
            }
        } catch (NoSuchElementException nseExp) {
            LOGGER.error("Could not get an instance of DataseerClassifier. Sending service unavailable.");
            response = Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
        } catch (Exception e) {
            LOGGER.error("An unexpected exception occurs. ", e);
            response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
        LOGGER.debug(methodLogOut());
        return response;
    }

    /**
     * Label dataset names, implicit datasets and data acquisition devices in a sentence.
     *
     * @param text raw sentence string
     * @return a json response object containing the labeling information related to possible
     * dataset mentions
     */
    public Response processDatasetSentence(String text) {
        LOGGER.debug(methodLogIn());
        Response response = null;
        StringBuilder retVal = new StringBuilder();
        JsonStringEncoder encoder = JsonStringEncoder.getInstance();
        boolean disambiguate = true;
        try {
            LOGGER.debug(">> set raw sentence text for stateless service'...");

            text = text.replaceAll("\\n", " ").replaceAll("\\t", " ");
            long start = System.currentTimeMillis();
            List<Dataset> result = this.datasetParser.processingString(text, disambiguate);

            // building JSON response
            StringBuilder json = new StringBuilder();
            json.append("{");
            json.append(DatastetServiceUtils.applicationDetails(this.datastetConfiguration.getVersion()));

            byte[] encoded = encoder.quoteAsUTF8(text);
            String output = new String(encoded);

            //json.append(", \"text\": \"" + output + "\"");
            json.append(", \"mentions\": [");

            ObjectMapper mapper = new ObjectMapper();

            String classifierJson = dataseerClassifier.classify(text);

            JsonNode rootNode = mapper.readTree(classifierJson);

            // get best type
            double bestScore = 0.0;
            String bestType = null;
            double hasDatasetScore = 0.0;

            JsonNode classificationsNode = rootNode.findPath("classifications");
            if ((classificationsNode != null) && (!classificationsNode.isMissingNode())) {

                if (classificationsNode.isArray()) {
                    ArrayNode classificationsArray = (ArrayNode) classificationsNode;
                    JsonNode classificationNode = classificationsArray.get(0);

                    Iterator<String> iterator = classificationNode.fieldNames();
                    Map<String, Double> scoresPerDatatypes = new TreeMap<>();
                    while (iterator.hasNext()) {
                        String field = iterator.next();

                        if (field.equals("has_dataset")) {
                            JsonNode hasDatasetNode = rootNode.findPath("has_dataset");
                            if ((hasDatasetNode != null) && (!hasDatasetNode.isMissingNode())) {
                                hasDatasetScore = hasDatasetNode.doubleValue();
                            }
                        } else {
                            scoresPerDatatypes.put(field, classificationNode.get(field).doubleValue());
                        }
                    }

                    for (Map.Entry<String, Double> entry : scoresPerDatatypes.entrySet()) {
                        if (entry.getValue() > bestScore) {
                            bestScore = entry.getValue();
                            bestType = entry.getKey();
                        }
                    }
                }
            }

            boolean startList = true;
            for (Dataset dataset : result) {
                if (startList)
                    startList = false;
                else
                    json.append(", ");

                if (dataset.getType() == DatasetType.DATASET && (bestType != null) && dataset.getDataset() != null) {
                    dataset.getDataset().setBestDataType(bestType);
                    dataset.getDataset().setBestDataTypeScore(bestScore);
                    dataset.getDataset().setHasDatasetScore(hasDatasetScore);
                }

                json.append(dataset.toJson());
            }
            json.append("]");

            long end = System.currentTimeMillis();
            float runtime = ((float) (end - start) / 1000);
            json.append(", \"runtime\": " + runtime);
            json.append("}");

            //System.out.println(json.toString());

            Object finalJsonObject = mapper.readValue(json.toString(), Object.class);
            String retValString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(finalJsonObject);

            if (!isResultOK(retValString)) {
                response = Response.status(Response.Status.NO_CONTENT).build();
            } else {
                response = Response.status(Response.Status.OK).entity(retValString).type(MediaType.TEXT_PLAIN).build();
            }
        } catch (NoSuchElementException nseExp) {
            LOGGER.error("Could not get an instance of DatasetParser. Sending service unavailable.");
            response = Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
        } catch (Exception e) {
            LOGGER.error("An unexpected exception occurs. ", e);
            response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
        LOGGER.debug(methodLogOut());
        return response;
    }

    public static String methodLogIn() {
        return ">> " + DatastetProcessString.class.getName() + "." + Thread.currentThread().getStackTrace()[1].getMethodName();
    }


    public static String methodLogOut() {
        return "<< " + DatastetProcessString.class.getName() + "." + Thread.currentThread().getStackTrace()[1].getMethodName();
    }

}
