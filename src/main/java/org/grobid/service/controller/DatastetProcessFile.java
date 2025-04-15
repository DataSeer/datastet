package org.grobid.service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.grobid.core.data.BibDataSet;
import org.grobid.core.data.Dataset;
import org.grobid.core.document.Document;
import org.grobid.core.engines.DataseerClassifier;
import org.grobid.core.engines.DatasetParser;
import org.grobid.core.layout.Page;
import org.grobid.core.utilities.ArticleUtilities;
import org.grobid.core.utilities.GrobidProperties;
import org.grobid.core.utilities.IOUtilities;
import org.grobid.service.configuration.DatastetConfiguration;
import org.grobid.service.exceptions.DatastetServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.DatatypeConverter;
import java.io.File;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.List;
import java.util.NoSuchElementException;

import static org.grobid.service.controller.DatastetServiceUtils.isResultOK;

/**
 * @author Patrice
 */
@Singleton
public class DatastetProcessFile {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatastetProcessFile.class);

    private final DatastetConfiguration datastetConfiguration;
    private final DataseerClassifier dataseerClassifier;
    private final DatasetParser datasetParser;

    @Inject
    public DatastetProcessFile(DatastetConfiguration configuration,
                               DatasetParser datasetParser,
                               DataseerClassifier dataseerClassifier) {

        this.datasetParser = datasetParser;
        this.dataseerClassifier = dataseerClassifier;
        this.datastetConfiguration = configuration;
    }

    /**
     * Uploads a TEI document, identify dataset introductory section, segment and classify sentences.
     *
     * @param inputStream the data of origin TEI document
     * @return a response object which contains an enriched TEI representation of the document
     */
    public Response processTEI(final InputStream inputStream, boolean segmentSentences) {
        LOGGER.debug(methodLogIn());
        String retVal = null;
        Response response = null;
        File originFile = null;
        try {
            originFile = ArticleUtilities.writeInputFile(inputStream, ".tei.xml");
            if (originFile == null) {
                LOGGER.error("The input file cannot be written.");
                throw new DatastetServiceException(
                        "The input file cannot be written. ", Response.Status.INTERNAL_SERVER_ERROR);
            }

            // starts conversion process
            retVal = this.dataseerClassifier.processTEI(originFile.getAbsolutePath(), segmentSentences, false);

            if (!isResultOK(retVal)) {
                response = Response.status(Response.Status.NO_CONTENT).build();
            } else {
                response = Response.status(Response.Status.OK)
                        .entity(retVal)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML + "; charset=UTF-8")
                        .header("Access-Control-Allow-Origin", "*")
                        .header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT")
                        .build();
            }
        } catch (Exception exp) {
            LOGGER.error("An unexpected exception occurs. ", exp);
            response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(exp.getMessage()).build();
        } finally {
            if (originFile != null)
                IOUtilities.removeTempFile(originFile);
        }

        LOGGER.debug(methodLogOut());
        return response;
    }

    /**
     * Uploads a JATS document, identify dataset introductory section, segment and classify sentences.
     *
     * @param inputStream the data of origin JATS document
     * @return a response object which contains an enriched TEI representation of the document
     */
    public Response processJATS(final InputStream inputStream) {
        LOGGER.debug(methodLogIn());
        String retVal = null;
        Response response = null;
        File originFile = null;
        try {
            originFile = ArticleUtilities.writeInputFile(inputStream, ".xml");
            if (originFile == null) {
                LOGGER.error("The input file cannot be written.");
                throw new DatastetServiceException(
                        "The input file cannot be written. ", Response.Status.INTERNAL_SERVER_ERROR);
            }

            // starts conversion process
            retVal = dataseerClassifier.processJATS(originFile.getAbsolutePath());

            if (!isResultOK(retVal)) {
                response = Response.status(Response.Status.NO_CONTENT).build();
            } else {
                response = Response.status(Response.Status.OK)
                        .entity(retVal)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML + "; charset=UTF-8")
                        .header("Access-Control-Allow-Origin", "*")
                        .header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT")
                        .build();
            }
        } catch (Exception exp) {
            LOGGER.error("An unexpected exception occurs. ", exp);
            response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(exp.getMessage()).build();
        } finally {
            if (originFile != null)
                IOUtilities.removeTempFile(originFile);
        }

        LOGGER.debug(methodLogOut());
        return response;
    }

    /**
     * Uploads a PDF document, extract and structured content with GROBID, convert it into TEI,
     * identify dataset introductory section, segment and classify sentences.
     *
     * @param inputStream the data of origin PDF document
     * @return a response object which contains an enriched TEI representation of the document
     */
    public Response processPDF(final InputStream inputStream) {
        LOGGER.debug(methodLogIn());
        String retVal = null;
        Response response = null;
        File originFile = null;
        try {
            originFile = IOUtilities.writeInputFile(inputStream);
            if (originFile == null) {
                LOGGER.error("The input file cannot be written.");
                throw new DatastetServiceException(
                        "The input file cannot be written. ", Response.Status.INTERNAL_SERVER_ERROR);
            }

            // starts conversion process
            retVal = dataseerClassifier.processPDF(originFile.getAbsolutePath());

            if (!isResultOK(retVal)) {
                response = Response.status(Response.Status.NO_CONTENT).build();
            } else {
                response = Response.status(Response.Status.OK)
                        .entity(retVal)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML + "; charset=UTF-8")
                        .header("Access-Control-Allow-Origin", "*")
                        .header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT")
                        .build();
            }
        } catch (Exception exp) {
            LOGGER.error("An unexpected exception occurs. ", exp);
            response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(exp.getMessage()).build();
        } finally {
            if (originFile != null)
                IOUtilities.removeTempFile(originFile);
        }

        LOGGER.debug(methodLogOut());
        return response;
    }

    /**
     * Uploads a PDF document, extract and structured content with GROBID, identify datasets and
     * associated information, return JSON response as layer annotations.
     *
     * @param inputStream the data of origin PDF document
     * @return a response object which contains JSON annotation enrichments
     */
    public Response processDatasetPDF(final InputStream inputStream,
                                      boolean disambiguate) {
        LOGGER.debug(methodLogIn());
        String retVal = null;
        Response response = null;
        File originFile = null;

        try {
            ObjectMapper mapper = new ObjectMapper();

            MessageDigest md = MessageDigest.getInstance("MD5");
            DigestInputStream dis = new DigestInputStream(inputStream, md);

            originFile = IOUtilities.writeInputFile(dis);
            byte[] digest = md.digest();

            if (originFile == null) {
                LOGGER.error("The input file cannot be written.");
                throw new DatastetServiceException(
                        "The input file cannot be written. ", Response.Status.INTERNAL_SERVER_ERROR);
            }

            long start = System.currentTimeMillis();
            // starts conversion process
            Pair<List<List<Dataset>>, Document> extractedResults = this.datasetParser.processPDF(originFile, disambiguate);

            StringBuilder json = new StringBuilder();
            json.append("{ ");
            json.append(DatastetServiceUtils.applicationDetails(this.datastetConfiguration.getVersion()));

            String md5Str = DatatypeConverter.printHexBinary(digest).toUpperCase();
            json.append(", \"md5\": \"" + md5Str + "\"");

            // page height and width
            json.append(", \"pages\":[");
            Document doc = extractedResults.getRight();
            List<Page> pages = doc.getPages();
            boolean first = true;
            for (Page page : pages) {
                if (first)
                    first = false;
                else
                    json.append(", ");
                json.append("{\"page_height\":" + page.getHeight());
                json.append(", \"page_width\":" + page.getWidth() + "}");
            }

            json.append("], \"mentions\":[");
            boolean startList = true;
            for (List<Dataset> results : extractedResults.getLeft()) {
                for (Dataset dataset : results) {
                    if (startList)
                        startList = false;
                    else
                        json.append(", ");
                    json.append(dataset.toJson());
                }
            }

            json.append("], \"references\":[");

            List<BibDataSet> bibDataSet = doc.getBibDataSets();
            if (bibDataSet != null && bibDataSet.size() > 0) {
                DatastetServiceUtils.serializeReferences(json, bibDataSet, extractedResults.getLeft());
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
        } catch (Exception exp) {
            LOGGER.error("An unexpected exception occurs. ", exp);
            response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(exp.getMessage()).build();
        } finally {
            if (originFile != null)
                IOUtilities.removeTempFile(originFile);
        }

        LOGGER.debug(methodLogOut());
        return response;
    }

    /**
     * Uploads the origin XML, process it and return the extracted dataset mention objects in JSON.
     *
     * @param inputStream the data of origin XML
     * @return a response object containing the JSON annotations
     */
    public Response processDatasetJATS(final InputStream inputStream, Boolean disambiguate) {
        LOGGER.debug(methodLogIn());
        Response response = null;
        File originFile = null;
        try {
            ObjectMapper mapper = new ObjectMapper();

            MessageDigest md = MessageDigest.getInstance("MD5");
            DigestInputStream dis = new DigestInputStream(inputStream, md);

            originFile = IOUtilities.writeInputFile(dis);
            byte[] digest = md.digest();

            if (originFile == null) {
                response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            } else {
                long start = System.currentTimeMillis();

                Pair<List<List<Dataset>>, List<BibDataSet>> extractionResult = this.datasetParser.processXML(originFile, false, disambiguate);
                long end = System.currentTimeMillis();

                List<List<Dataset>> extractedEntities = null;
                if (extractionResult != null) {
                    extractedEntities = extractionResult.getLeft();
                }

                StringBuilder json = new StringBuilder();
                json.append("{ ");
                json.append(DatastetServiceUtils.applicationDetails(GrobidProperties.getVersion()));

                String md5Str = DatatypeConverter.printHexBinary(digest).toUpperCase();
                json.append(", \"md5\": \"" + md5Str + "\"");
                json.append(", \"mentions\":[");

                if (CollectionUtils.isNotEmpty(extractedEntities)) {
                    boolean startList = true;
                    for (List<Dataset> results : extractedEntities) {
                        for (Dataset dataset : results) {
                            if (startList)
                                startList = false;
                            else
                                json.append(", ");
                            json.append(dataset.toJson());
                        }
                    }
                }

                json.append("], \"references\":[");

                if (CollectionUtils.isNotEmpty(extractedEntities)) {
                    List<BibDataSet> bibDataSet = extractionResult.getRight();
                    if (CollectionUtils.isNotEmpty(bibDataSet)) {
                        DatastetServiceUtils.serializeReferences(json, bibDataSet, extractedEntities);
                    }
                }

                json.append("]");

                float runtime = ((float) (end - start) / 1000);
                json.append(", \"runtime\": " + runtime);

                json.append("}");

                Object finalJsonObject = mapper.readValue(json.toString(), Object.class);
                String retValString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(finalJsonObject);

                if (!isResultOK(retValString)) {
                    response = Response.status(Response.Status.NO_CONTENT).build();
                } else {
                    response = Response.status(Response.Status.OK).entity(retValString).type(MediaType.TEXT_PLAIN).build();
                    /*response = Response
                            .ok()
                            .type("application/json")
                            .entity(retValString)
                            .build();*/
                }
            }

        } catch (NoSuchElementException nseExp) {
            LOGGER.error("Could not get an instance of DatastetParser. Sending service unavailable.");
            response = Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
        } catch (Exception exp) {
            LOGGER.error("An unexpected exception occurs. ", exp);
            response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(exp.getMessage()).build();
        } finally {
            IOUtilities.removeTempFile(originFile);
        }
        LOGGER.debug(methodLogOut());
        return response;
    }

    /**
     * Uploads the origin TEI XML, process it and return the extracted dataset mention objects in JSON.
     *
     * @param inputStream      the data of origin TEI
     * @param segmentSentences add sentence segmentation if the TEI was not already segmented
     * @return a response object containing the JSON annotations
     */
    public Response processDatasetTEI(
            final InputStream inputStream,
            boolean segmentSentences,
            boolean disambiguate
    ) {
        LOGGER.debug(methodLogIn());
        Response response = null;
        File originFile = null;


        try {
            ObjectMapper mapper = new ObjectMapper();

            MessageDigest md = MessageDigest.getInstance("MD5");
            DigestInputStream dis = new DigestInputStream(inputStream, md);

            originFile = IOUtilities.writeInputFile(dis);
            byte[] digest = md.digest();

            if (originFile == null) {
                response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            } else {
                long start = System.currentTimeMillis();
                Pair<List<List<Dataset>>, List<BibDataSet>> extractionResult = this.datasetParser.processTEI(originFile, segmentSentences, disambiguate);
                long end = System.currentTimeMillis();

                List<List<Dataset>> extractedEntities = null;
                if (extractionResult != null) {
                    extractedEntities = extractionResult.getLeft();
                }

                StringBuilder json = new StringBuilder();
                json.append("{ ");
                json.append(DatastetServiceUtils.applicationDetails(GrobidProperties.getVersion()));

                String md5Str = DatatypeConverter.printHexBinary(digest).toUpperCase();
                json.append(", \"md5\": \"" + md5Str + "\"");
                json.append(", \"mentions\":[");
                if (CollectionUtils.isNotEmpty(extractedEntities)) {
                    boolean startList = true;
                    for (List<Dataset> results : extractedEntities) {
                        for (Dataset dataset : results) {
                            if (startList)
                                startList = false;
                            else
                                json.append(", ");
                            json.append(dataset.toJson());
                        }
                    }
                }
                json.append("], \"references\":[");

                if (CollectionUtils.isNotEmpty(extractedEntities)) {
                    List<BibDataSet> bibDataSet = extractionResult.getRight();
                    if (CollectionUtils.isNotEmpty(bibDataSet)) {
                        DatastetServiceUtils.serializeReferences(json, bibDataSet, extractedEntities);
                    }
                }
                json.append("]");

                float runtime = ((float) (end - start) / 1000);
                json.append(", \"runtime\": " + runtime);

                json.append("}");

                Object finalJsonObject = mapper.readValue(json.toString(), Object.class);
                String retValString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(finalJsonObject);

                if (!isResultOK(retValString)) {
                    response = Response.status(Response.Status.NO_CONTENT).build();
                } else {
                    response = Response.status(Response.Status.OK).entity(retValString).type(MediaType.TEXT_PLAIN).build();
                }
            }

        } catch (NoSuchElementException nseExp) {
            LOGGER.error("Could not get an instance of DatastetParser. Sending service unavailable.");
            response = Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
        } catch (Exception exp) {
            LOGGER.error("An unexpected exception occurs. ", exp);
            response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(exp.getMessage()).build();
        } finally {
            IOUtilities.removeTempFile(originFile);
        }
        LOGGER.debug(methodLogOut());
        return response;
    }

    public static String methodLogIn() {
        return ">> " + DatastetProcessFile.class.getName() + "." + Thread.currentThread().getStackTrace()[1].getMethodName();
    }

    public static String methodLogOut() {
        return "<< " + DatastetProcessFile.class.getName() + "." + Thread.currentThread().getStackTrace()[1].getMethodName();
    }

}
