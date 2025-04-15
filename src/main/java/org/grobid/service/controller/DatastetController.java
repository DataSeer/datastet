package org.grobid.service.controller;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.grobid.service.configuration.DatastetConfiguration;
import org.grobid.service.configuration.DatastetServiceConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

/**
 * RESTful service for GROBID dataseer extension.
 *
 * @author Patrice
 */
@Singleton
@Path(DatastetPaths.PATH_DATASEER)
public class DatastetController implements DatastetPaths {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatastetController.class);

    private static final String TEXT = "text";
    private static final String TEXTS = "texts";
    private static final String XML = "xml";
    private static final String TEI = "tei";
    private static final String PDF = "pdf";
    private static final String INPUT = "input";
    private static final String JSON = "json";
    private static final String DISAMBIGUATE = "disambiguate";
    private static final String SEGMENT_SENTENCES = "segmentSentences";

    private DatastetConfiguration configuration;
    private final DatastetProcessFile datastetProcessFile;
    private final DatastetProcessString datastetProcessString;

    @Inject
    public DatastetController(
            DatastetServiceConfiguration serviceConfiguration,
            DatastetProcessFile datastetProcessFile,
            DatastetProcessString datastetProcessString) {
        this.configuration = serviceConfiguration.getDatastetConfiguration();
        this.datastetProcessFile = datastetProcessFile;
        this.datastetProcessString = datastetProcessString;
    }

    @GET
    @Path(PATH_IS_ALIVE)
    @Produces(MediaType.TEXT_PLAIN)
    public Response isAlive() {
        return DatastetRestProcessGeneric.isAlive();
    }

    @Path(PATH_DATASEER_SENTENCE)
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    @POST
    public Response processText_post(@FormParam(TEXT) String text) {
        LOGGER.info(text);
        return this.datastetProcessString.processDataseerSentence(text);
    }

    @Path(PATH_OLD_DATASEER_SENTENCE)
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    @POST
    public Response processTextOld_post(@FormParam(TEXT) String text) {
        LOGGER.info(text);
        return this.datastetProcessString.processDataseerSentence(text);
    }

    @Path(PATH_DATASEER_SENTENCE)
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    @GET
    public Response processText_get(@QueryParam(TEXT) String text) {
        LOGGER.info(text);
        return this.datastetProcessString.processDataseerSentence(text);
    }

    @Path(PATH_DATASEER_SENTENCES)
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @POST
    public Response processTexts_post(@FormDataParam(TEXTS) String texts) {
        LOGGER.info("Received multiple sentences as JSON list");
        return this.datastetProcessString.processDataseerSentences(texts);
    }

    @Path(PATH_OLD_DATASEER_SENTENCES)
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @POST
    public Response processTextsOld_post(@FormDataParam(TEXTS) String texts) {
        LOGGER.info("Received multiple sentences as JSON list");
        return this.datastetProcessString.processDataseerSentences(texts);
    }

    @Path(PATH_DATASET_SENTENCE)
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    @POST
    public Response processDatasetText_post(@FormParam(TEXT) String text) {
        LOGGER.info(text);
        return this.datastetProcessString.processDatasetSentence(text);
    }

    @Path(PATH_DATASET_SENTENCE)
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    @GET
    public Response processDatasetText_get(@QueryParam(TEXT) String text) {
        LOGGER.info(text);
        return this.datastetProcessString.processDatasetSentence(text);
    }

    @Path(PATH_DATASEER_PDF)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_XML)
    @POST
    public Response processPDF(@FormDataParam(INPUT) InputStream inputStream) {
        return this.datastetProcessFile.processPDF(inputStream);
    }

    @Path(PATH_DATASET_PDF)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_XML)
    @POST
    public Response processDatasetPDF(@FormDataParam(INPUT) InputStream inputStream,
                                      @DefaultValue("0") @FormDataParam(DISAMBIGUATE) String disambiguate) {
        boolean disambiguateBoolean = DatastetServiceUtils.validateBooleanRawParam(disambiguate);
        return this.datastetProcessFile.processDatasetPDF(inputStream, disambiguateBoolean);
    }

    @Path(PATH_DATASET_TEI)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @POST
    public Response processDatasetTEI(
            @FormDataParam(INPUT) InputStream inputStream,
            @DefaultValue("0") @FormDataParam(SEGMENT_SENTENCES) String segmentSentences,
            @DefaultValue("0") @FormDataParam(DISAMBIGUATE) String disambiguate
    ) {
        boolean disambiguateBoolean = DatastetServiceUtils.validateBooleanRawParam(disambiguate);
        boolean segmentSentencesBoolean = DatastetServiceUtils.validateBooleanRawParam(segmentSentences);
        return this.datastetProcessFile.processDatasetTEI(inputStream, segmentSentencesBoolean, disambiguateBoolean);
    }

    @Path(PATH_DATASET_JATS)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @POST
    public Response processJATS(@FormDataParam(INPUT) InputStream inputStream,
                                @DefaultValue("0") @FormDataParam(DISAMBIGUATE) String disambiguate) {
        boolean disambiguateBoolean = DatastetServiceUtils.validateBooleanRawParam(disambiguate);
        return this.datastetProcessFile.processDatasetJATS(inputStream, disambiguateBoolean);
    }

    @Path(PATH_DATASEER_TEI)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_XML)
    @POST
    public Response processTEI(
            @FormDataParam(INPUT) InputStream inputStream,
            @FormDataParam("segmentSentences") String segmentSentences) {
        boolean segmentSentencesBoolean = DatastetServiceUtils.validateBooleanRawParam(segmentSentences);
        return this.datastetProcessFile.processTEI(inputStream, segmentSentencesBoolean);
    }

    @Path(PATH_DATASEER_JATS)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_XML)
    @POST
    public Response processJATS(@FormDataParam(INPUT) InputStream inputStream) {
        return this.datastetProcessFile.processJATS(inputStream);
    }

    @Path(PATH_DATATYPE_JSON)
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    @GET
    public Response getJsonDataTypes() {
        return DatastetDataTypeService.getInstance().getJsonDataTypes();
    }

    @Path(PATH_RESYNC_DATATYPE_JSON)
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    @GET
    public Response getResyncJsonDataTypes() {
        return DatastetDataTypeService.getInstance().getResyncJsonDataTypes();
    }

    public DatastetConfiguration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(DatastetConfiguration configuration) {
        this.configuration = configuration;
    }
}
