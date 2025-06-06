package org.grobid.service.configuration;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.grobid.core.utilities.GrobidConfig;
import org.grobid.core.utilities.GrobidConfig.ModelParameters;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DatastetConfiguration {

    public String corpusPath;
    public String templatePath;
    public String grobidHome;
    public String tmpPath;
    public String pub2teiPath;
    public String gluttonHost;
    public String gluttonPort;
    public String version;
    private Boolean useBinaryContextClassifiers;
    private String entityFishingHost;
    private String entityFishingPort;

    //models (sequence labeling and text classifiers)
    private List<GrobidConfig.ModelParameters> models = new ArrayList<>();


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

    public String getGrobidHome() {
        return this.grobidHome;
    }

    public void setGrobidHome(String grobidHome) {
        this.grobidHome = grobidHome;
    }

    public List<ModelParameters> getModels() {
        return models;
    }

    public ModelParameters getModel() {
        // by default return the dataseer sequence labeling model
        return getModel("dataseer");
    }

    public ModelParameters getModel(String modelName) {
        for (ModelParameters parameters : models) {
            if (parameters.name.equals(modelName)) {
                return parameters;
            }
        }
        return null;
    }

    public void setModels(List<ModelParameters> models) {
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

    public String getVersion() {
        return this.version;
    }

    public void setVersion(String version) {
        this.version = version;
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
 