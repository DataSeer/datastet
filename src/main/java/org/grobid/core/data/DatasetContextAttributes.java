package org.grobid.core.data;

/**
 * This class represents characteristics of mention context(s) for a dataset with the following attributes:
 * - used: dataset usage by the research work disclosed in the document
 * - created: dataset creation/contribution of the research work disclosed in the document (creation, extension, etc.)
 * - shared: dataset is claimed shared via a sharing statement
 * Scores in [0,1] and binary class values are stored for each attribute.
 */
public class DatasetContextAttributes {

    private Boolean used = null;
    private Double usedScore = null;
    private Boolean created = null;
    private Double createdScore = null;
    private Boolean shared = null;
    private Double sharedScore = null;

    public DatasetContextAttributes() {
    }

    public Boolean getUsed() {
        return this.used;
    }

    public void setUsed(Boolean used) {
        this.used = used;
    }

    public Double getUsedScore() {
        return this.usedScore;
    }

    public void setUsedScore(Double usedScore) {
        this.usedScore = usedScore;
    }

    public Boolean getCreated() {
        return this.created;
    }

    public void setCreated(Boolean created) {
        this.created = created;
    }

    public Double getCreatedScore() {
        return this.createdScore;
    }

    public void setCreatedScore(Double createdScore) {
        this.createdScore = createdScore;
    }

    public Boolean getShared() {
        return this.shared;
    }

    public void setShared(Boolean shared) {
        this.shared = shared;
    }

    public Double getSharedScore() {
        return this.sharedScore;
    }

    public void setSharedScore(Double sharedScore) {
        this.sharedScore = sharedScore;
    }

    /**
     * Set all values to default
     **/
    public void init() {
        used = false;
        usedScore = 0.0;
        created = false;
        createdScore = 0.0;
        shared = false;
        sharedScore = 0.0;
    }

    public String toJson() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("{\"used\" : { \"value\": " + this.used + ", \"score\": " + this.usedScore + "}");
        buffer.append(", \"created\" : { \"value\": " + this.created + ", \"score\": " + this.createdScore + "}");
        buffer.append(", \"shared\" : { \"value\": " + this.shared + ", \"score\": " + this.sharedScore + "}}");
        return buffer.toString();
    }

}