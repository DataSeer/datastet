package org.grobid.service.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.core.Configuration;
import org.grobid.core.utilities.GrobidConfig;

import java.util.ArrayList;
import java.util.List;

public class DatastetServiceConfiguration extends Configuration {

    private String grobidHome;
    private DatastetConfiguration datastetConfiguration;
    private int maxParallelRequests;

    public String corpusPath;
    public String templatePath;
    public String tmpPath;
    public String pub2teiPath;
    public String gluttonHost;
    public String gluttonPort;
    private String version;
    private Boolean useBinaryContextClassifiers;
    private String entityFishingHost;
    private String entityFishingPort;

    //models (sequence labeling and text classifiers)
    private List<GrobidConfig.ModelParameters> models = new ArrayList<>();

    @JsonProperty
    private String corsAllowedOrigins = "*";
    @JsonProperty
    private String corsAllowedMethods = "OPTIONS,GET,PUT,POST,DELETE,HEAD";
    @JsonProperty
    private String corsAllowedHeaders = "X-Requested-With,Content-Type,Accept,Origin";

    public String getGrobidHome() {
        return grobidHome;
    }

    public void setGrobidHome(String grobidHome) {
        this.grobidHome = grobidHome;
    }

    public DatastetConfiguration getDatastetConfiguration() {
        return this.datastetConfiguration;
    }

    public void setDatastetConfiguration(DatastetConfiguration conf) {
        this.datastetConfiguration = conf;
    }

    public int getMaxParallelRequests() {
        if (this.maxParallelRequests == 0) {
            this.maxParallelRequests = Runtime.getRuntime().availableProcessors();
        }
        return this.maxParallelRequests;
    }

    public String getCorsAllowedOrigins() {
        return corsAllowedOrigins;
    }

    public void setCorsAllowedOrigins(String corsAllowedOrigins) {
        this.corsAllowedOrigins = corsAllowedOrigins;
    }

    public String getCorsAllowedMethods() {
        return corsAllowedMethods;
    }

    public void setCorsAllowedMethods(String corsAllowedMethods) {
        this.corsAllowedMethods = corsAllowedMethods;
    }

    public String getCorsAllowedHeaders() {
        return corsAllowedHeaders;
    }

    public void setCorsAllowedHeaders(String corsAllowedHeaders) {
        this.corsAllowedHeaders = corsAllowedHeaders;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getCorpusPath() {
        return this.corpusPath;
    }

    public void setCorpusPath(String corpusPath) {
        this.corpusPath = corpusPath;
    }

    public String getTemplatePath() {
        return this.templatePath;
    }

    public void setTemplatePath(String templatePath) {
        this.templatePath = templatePath;
    }

    public String getTmpPath() {
        return this.tmpPath;
    }

    public void setTmpPath(String tmpPath) {
        this.tmpPath = tmpPath;
    }

    public String getPub2TEIPath() {
        return this.pub2teiPath;
    }

    public void setPub2teiPath(String pub2teiPath) {
        this.pub2teiPath = pub2teiPath;
    }

    public List<GrobidConfig.ModelParameters> getModels() {
        return models;
    }

    public GrobidConfig.ModelParameters getModel() {
        // by default return the dataseer sequence labeling model
        return getModel("dataseer");
    }

    public GrobidConfig.ModelParameters getModel(String modelName) {
        for (GrobidConfig.ModelParameters parameters : models) {
            if (parameters.name.equals(modelName)) {
                return parameters;
            }
        }
        return null;
    }

    public void setModels(List<GrobidConfig.ModelParameters> models) {
        this.models = models;
    }

    public String getGluttonHost() {
        return this.gluttonHost;
    }

    public void setGluttonHost(String host) {
        this.gluttonHost = host;
    }

    public String getGluttonPort() {
        return this.gluttonPort;
    }

    public void setGluttonPort(String port) {
        this.gluttonPort = port;
    }

    public Boolean getUseBinaryContextClassifiers() {
        return this.useBinaryContextClassifiers;
    }

    public void setUseBinaryContextClassifiers(Boolean binary) {
        this.useBinaryContextClassifiers = binary;
    }

    public String getEntityFishingHost() {
        return entityFishingHost;
    }

    public void setEntityFishingHost(String entityFishingHost) {
        this.entityFishingHost = entityFishingHost;
    }

    public String getEntityFishingPort() {
        return entityFishingPort;
    }

    public void setEntityFishingPort(String entityFishingPort) {
        this.entityFishingPort = entityFishingPort;
    }
}
