package org.grobid.service.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.grobid.core.data.BibDataSet;
import org.grobid.core.data.BiblioComponent;
import org.grobid.core.data.Dataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

/**
 * Utility methods for DataStet service.
 *
 * @author Patrice
 */
public class DatastetServiceUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(DatastetServiceUtils.class);

    /**
     * Give application information to be added in a JSON result
     */
    public static String applicationDetails(String version) {
        StringBuilder sb = new StringBuilder();

        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ");
        df.setTimeZone(tz);
        String dateISOString = df.format(new java.util.Date());

        sb.append("\"application\": \"datastet\", ");
        if (version != null)
            sb.append("\"version\": \"" + version + "\", ");
        sb.append("\"date\": \"" + dateISOString + "\"");

        return sb.toString();
    }

    /**
     * Convert REST boolean parameter value provided as string
     */
    public static boolean validateBooleanRawParam(String raw) {
        boolean result = false;
        if ((raw != null) && (raw.equals("1") || raw.toLowerCase().equals("true"))) {
            result = true;
        }
        return result;
    }

    /**
     * Serialize the bibliographical references present in a list of entities
     */
    public static void serializeReferences(StringBuilder json,
                                           List<BibDataSet> bibDataSet,
                                           List<List<Dataset>> entities) {
        ObjectMapper mapper = new ObjectMapper();
        List<Integer> serializedKeys = new ArrayList<Integer>();
        for (List<Dataset> datasets : entities) {
            for (Dataset entity : datasets) {
                List<BiblioComponent> bibRefs = entity.getBibRefs();
                if (bibRefs != null) {
                    for (BiblioComponent bibComponent : bibRefs) {
                        int refKey = bibComponent.getRefKey();
                        if (!serializedKeys.contains(refKey)) {
                            if (serializedKeys.size() > 0)
                                json.append(", ");
                            if (bibComponent.getBiblio() != null) {
                                json.append("{ \"refKey\": " + refKey);
                                try {
                                    json.append(", \"tei\": " + mapper.writeValueAsString(bibComponent.getBiblio().toTEI(refKey)));
                                } catch (JsonProcessingException e) {
                                    LOGGER.warn("tei for biblio cannot be encoded", e);
                                }
                                json.append("}");
                            }
                            serializedKeys.add(Integer.valueOf(refKey));
                        }
                    }
                }
            }
        }
    }

    public static boolean isResultOK(String result) {
        return !StringUtils.isBlank(result);
    }

    private static boolean validateTrueFalseParam(String param) {
        return (param != null) && (param.equals("1") || param.equalsIgnoreCase("true"));
    }
}