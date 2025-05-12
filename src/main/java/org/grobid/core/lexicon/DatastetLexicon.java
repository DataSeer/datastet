package org.grobid.core.lexicon;

import org.apache.commons.lang3.StringUtils;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.exceptions.GrobidResourceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

/**
 * Class for managing the lexical resources for datastet
 *
 * @author Patrice
 */
public class DatastetLexicon {

    private static Logger LOGGER = LoggerFactory.getLogger(DatastetLexicon.class);

    private Set<String> Datasetocabulary = null;
    private FastMatcher DatasetPattern = null;

    private Map<String, Double> termIDF = null;

    // the list of P31 and P279 values of the Wikidata dataset entities
    private List<String> propertyValues = null;

    private List<String> englishStopwords = null;
    private List<String> blackListBioMed = null;

    private Set<String> doiPrefixes = null;
    private Set<String> urlDomains = null;

    private static volatile DatastetLexicon instance;

    // to use the url pattern in grobid-core after merging branch update_header
    static public final Pattern urlPattern = Pattern
            .compile("(?i)(https?|ftp)\\s?:\\s?//\\s?[-A-Z0-9+&@#/%=~_:.]*[-A-Z0-9+&@#/%=~_]");

    public static DatastetLexicon getInstance() {
        if (instance == null) {
            synchronized (DatastetLexicon.class) {
                instance = new DatastetLexicon();
            }
        }
        return instance;
    }

    private DatastetLexicon() {
        Lexicon.getInstance();
        // init the lexicon
        LOGGER.info("Init Datastet lexicon");

        // term idf
        File file = getFileFromPath("resources/lexicon/idf.label.en.txt.gz");

        BufferedReader dis = null;
        // read the idf file
        try {
            termIDF = new TreeMap<String, Double>();

            dis = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(file)), "UTF-8"));
            //dis = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
            String l = null;
            while ((l = dis.readLine()) != null) {
                if (l.length() == 0) continue;

                String[] pieces = l.split("\t");
                if (pieces.length != 2) {
                    LOGGER.warn("Invalid term/idf line format: " + l);
                    continue;
                }

                String term = pieces[0];
                String idfString = pieces[1];
                double idf = 0.0;
                try {
                    idf = Double.parseDouble(idfString);
                } catch (Exception e) {
                    LOGGER.warn("Invalid idf format: " + idfString);
                    continue;
                }

                termIDF.put(term, Double.valueOf(idf));
            }
        } catch (FileNotFoundException e) {
            throw new GrobidException("Datastet Lexicon file not found.", e);
        } catch (IOException e) {
            throw new GrobidException("Cannot read Datastet Lexicon file.", e);
        } finally {
            try {
                if (dis != null)
                    dis.close();
            } catch (Exception e) {
                throw new GrobidResourceException("Cannot close IO stream.", e);
            }
        }

        // read the datacite DOI prefixes
        file = getFileFromPath("resources/lexicon/doiPrefixes.txt");

        dis = null;
        try {
            doiPrefixes = new HashSet<>();

            dis = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
            String l = null;
            while ((l = dis.readLine()) != null) {
                l = l.trim();
                if (l.length() == 0)
                    continue;
                doiPrefixes.add(l);
            }
        } catch (FileNotFoundException e) {
            throw new GrobidException("DatasetLexicon DOI prefix file not found.", e);
        } catch (IOException e) {
            throw new GrobidException("Cannot read DatasetLexicon DOI prefix file.", e);
        } finally {
            try {
                if (dis != null)
                    dis.close();
            } catch (Exception e) {
                throw new GrobidResourceException("DatasetLexicon DOI prefix file: cannot close IO stream.", e);
            }
        }

        // read the data source url domains 
        file = getFileFromPath("resources/lexicon/domains.txt");

        dis = null;
        try {
            urlDomains = new HashSet<>();

            dis = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
            String l = null;
            while ((l = dis.readLine()) != null) {
                l = l.trim();
                if (l.length() == 0)
                    continue;
                urlDomains.add(l);
            }
        } catch (FileNotFoundException e) {
            throw new GrobidException("DatasetLexicon url domain file not found.", e);
        } catch (IOException e) {
            throw new GrobidException("Cannot read DatasetLexicon url domain file.", e);
        } finally {
            try {
                if (dis != null)
                    dis.close();
            } catch (Exception e) {
                throw new GrobidResourceException("DatasetLexicon url domain file: cannot close IO stream.", e);
            }
        }

        // a list of stopwords for English for conservative checks with dataset names
        englishStopwords = new ArrayList<>();

        file = getFileFromPath("resources/lexicon/stopwords_en.txt");

        // read the file
        try {
            dis = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
            String l = null;
            while ((l = dis.readLine()) != null) {
                if (l.length() == 0) continue;
                englishStopwords.add(l.trim());
            }
        } catch (FileNotFoundException e) {
            throw new GrobidException("English stopwords file not found.", e);
        } catch (IOException e) {
            throw new GrobidException("Cannot read English stopwords file.", e);
        } finally {
            try {
                if (dis != null)
                    dis.close();
            } catch (Exception e) {
                throw new GrobidResourceException("Cannot close IO stream.", e);
            }
        }

        // a black list of for English in biomed domain
        blackListBioMed = new ArrayList<>();
        file = getFileFromPath("resources/lexicon/covid_blacklist.txt");
        // read the file
        try {
            dis = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
            String l = null;
            while ((l = dis.readLine()) != null) {
                if (StringUtils.isBlank(l) || l.startsWith("#")) {
                    continue;
                }
                blackListBioMed.add(l.trim().toLowerCase());
            }
        } catch (FileNotFoundException e) {
            throw new GrobidException("covid blacklist file not found.", e);
        } catch (IOException e) {
            throw new GrobidException("Cannot read covid blacklist file.", e);
        } finally {
            try {
                if (dis != null)
                    dis.close();
            } catch (Exception e) {
                throw new GrobidResourceException("Cannot close IO stream.", e);
            }
        }
    }

    private File getFileFromPath(String filePath) {
        Path path = Paths.get(filePath);
        File file = path.toFile();

        if (!file.exists()) {
            throw new GrobidResourceException("Cannot initialize dataset lexicon because file '" +
                    file.getAbsolutePath() + "' does not exists.");
        }
        if (!file.canRead()) {
            throw new GrobidResourceException("Cannot initialize dataset lexicon because cannot read file '" +
                    file.getAbsolutePath() + "'.");
        }

        return file;
    }

    public double getTermIDF(String term) {
        Double idf = termIDF.get(term);
        if (idf != null)
            return idf.doubleValue();
        else
            return 0.0;
    }

    /*public boolean inSoftwarePropertyValues(String value) {
        return propertyValues.contains(value.toLowerCase());
    }

    public boolean inSoftwareCategories(String value) {
        return wikipediaCategories.contains(value.toLowerCase());
    }  */

    public boolean isEnglishStopword(String value) {
        if (this.englishStopwords == null || value == null)
            return false;
        if (value.length() == 1)
            value = value.toLowerCase();
        return this.englishStopwords.contains(value);
    }

    public String removeLeadingEnglishStopwords(String string) {
        if (string == null || string.trim().length() == 0) {
            return string;
        }

        string = string.trim();
        while (string.length() > 0) {
            int startSize = string.length();
            // note: create a fast matcher...
            for (String stopword : this.englishStopwords) {
                if (string.startsWith(stopword + " ")) {
                    string = string.substring(stopword.length(), string.length());
                    string = string.trim();
                    break;
                }
            }
            if (startSize - string.length() == 0)
                break;
        }

        return string;
    }

    /**
     * Return a boolean value indicating if an URL or DOI is data DOI (referenced by datacite)
     * or a known dataset URL.
     * <p>
     * To determine this, we use a list of DOI prefix collected from a datacite dump and a list
     * of known domains of data repository.
     */
    public boolean isDatasetURLorDOI(String url) {
        if (StringUtils.isBlank(url)) {
            return false;
        }
        return (isDatasetURL(url) || isDatasetDOI(url));
    }

    /**
     * Return a boolean value indicating if an URL data source as a known dataset URL.
     * <p>
     * To determine this, we use a list of known domains of data repository.
     */
    public boolean isDatasetURL(String url) {
        if (StringUtils.isBlank(url)) {
            return false;
        }

        // strip protocol prefix
        if (url.startsWith("https://"))
            url = url.substring(8);
        if (url.startsWith("http://"))
            url = url.substring(7);
        if (url.startsWith("www."))
            url = url.substring(4);

        // strip url path
        int ind = url.indexOf("/");
        if (ind != -1)
            url = url.substring(0, ind);

        if (urlDomains != null && urlDomains.contains(url))
            return true;
        return false;
    }

    /**
     * Return a boolean value indicating if a DOI is data DOI (referenced by datacite).
     * <p>
     * To determine this, we use a list of DOI prefix collected from a datacite dump.
     */
    public boolean isDatasetDOI(String doi) {
        if (doi == null || doi.length() == 0)
            return false;

        // strip protocol prefix
        doi = doi.replace("https://doi.org/", "");
        doi = doi.replace("http://doi.org/", "");

        // strip url path
        int ind = doi.indexOf("/");
        if (ind != -1)
            doi = doi.substring(0, ind);

        if (doiPrefixes != null && doiPrefixes.contains(doi))
            return true;
        return false;
    }

    // basic black list (it should be built semi-automatically in future version and to be put in a file), not enough content
    // for a full named dataset
    private List<String> blackListNamedDataset =
            Arrays.asList("data", "dataset", "datasets", "data set", "data sets", "cell", "cells", "file", "files", "model", "models",
                    "record", "records", "column", "columns", "line", "lines", "tnbc", "pam", "patient", "patients", "uhrf", "normal",
                    "discovery", "manuscript", "draft", "database", "data base", "databases", "data bases", "base", "bases", "square",
                    "mission", "missions", "subject", "subjects");

    public boolean isBlackListedNamedDataset(String term) {
        if (term == null || term.length() == 0)
            return false;

        if (blackListNamedDataset.contains(term.toLowerCase()))
            return true;

        if (blackListBioMed.contains(term.toLowerCase()))
            return true;

        // temporary force filtering all the models, waiting for more training data and negative examples 
        if (term.toLowerCase().endsWith("model") || term.toLowerCase().endsWith("models"))
            return true;

        if (term.startsWith("ð"))
            return true;

        return false;
    }


}
