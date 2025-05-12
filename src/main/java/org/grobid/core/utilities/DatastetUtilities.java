package org.grobid.core.utilities;

import org.grobid.core.layout.LayoutToken;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class for holding static methods for Dataseer processing.
 *
 * @author Patrice Lopez
 */
public class DatastetUtilities {

    // a regular expression for identifying "materials and method" pattern in text
    static public final Pattern matAndMetPattern = Pattern
            .compile("(?i)material(s?)\\s*(and|&)\\s*method");

    static public boolean detectMaterialsAndMethod(List<LayoutToken> tokens) {
        if (tokens == null || tokens.size() == 0)
            return false;
        String localText = LayoutTokensUtil.toText(tokens);
        if (localText.trim().length() < 15)
            return false;
        Matcher matAndMetMatcher = DatastetUtilities.matAndMetPattern.matcher(localText);
        return matAndMetMatcher.find();
    }

    static public String getISO8601Date() {
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat sdf;
        sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(date);
    }

}