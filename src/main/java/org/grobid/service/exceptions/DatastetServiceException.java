package org.grobid.service.exceptions;

import jakarta.ws.rs.core.Response;
import org.grobid.core.exceptions.GrobidException;

public class DatastetServiceException extends GrobidException {

    private Response.Status responseCode;

    public DatastetServiceException(Response.Status responseCode) {
        super();
        this.responseCode = responseCode;
    }

    public DatastetServiceException(String msg, Response.Status responseCode) {
        super(msg);
        this.responseCode = responseCode;
    }

    public DatastetServiceException(String msg, Throwable cause, Response.Status responseCode) {
        super(msg, cause);
        this.responseCode = responseCode;
    }

    public Response.Status getResponseCode() {
        return responseCode;
    }
}
