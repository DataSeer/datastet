package org.grobid.trainer;

import org.grobid.core.GrobidModels;
import org.grobid.core.engines.DataseerClassifier;
import org.grobid.core.engines.DataseerParser;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.layout.LayoutToken;
import org.grobid.core.main.GrobidHomeFinder;
import org.grobid.core.utilities.GrobidProperties;
import org.grobid.core.utilities.Pair;
import org.grobid.service.configuration.DatastetConfiguration;
import org.grobid.trainer.evaluation.EvaluationUtilities;
import org.grobid.trainer.sax.DataseerAnnotationSaxHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.grobid.trainer.AnnotatedCorpusGeneratorCSV.readConfiguration;

/**
 * Training of the dataseer section labeling model
 *
 * @author Patrice
 */
public class DataseerTrainer extends AbstractTrainer {

    private final DataseerClassifier classifier;
    private DatastetConfiguration datastetConfiguration;

    public DataseerTrainer(DatastetConfiguration datastetConfiguration) {
        this(0.00001, 20, 0, datastetConfiguration);
    }

    public DataseerTrainer(double epsilon, int window, int nbMaxIterations, DatastetConfiguration datastetConfiguration) {
        super(GrobidModels.DATASEER);
        this.classifier = DataseerClassifier.getInstance(datastetConfiguration);

        // adjusting CRF training parameters for this model
        this.epsilon = epsilon;
        this.window = window;
        //this.nbMaxIterations = nbMaxIterations;
        this.nbMaxIterations = 2000;
    }

    /**
     * Add the selected features to the model training for dataseer.
     */
    public int createCRFPPData(File sourcePathLabel,
                               File outputPath) {
        return createCRFPPData(sourcePathLabel, outputPath, null, 1.0);
    }

    /**
     * Add the selected features to the model training for dataseer. Split
     * automatically all available labeled data into training and evaluation data
     * according to a given split ratio.
     */
    public int createCRFPPData(final File corpusDir,
                               final File trainingOutputPath,
                               final File evalOutputPath,
                               double splitRatio) {
        return createCRFPPData(corpusDir, trainingOutputPath, evalOutputPath, splitRatio, true);
    }


    /**
     * CRF training data here are produced from the unique training TEI file
     */
    public int createCRFPPData(final File corpusDir,
                               final File trainingOutputPath,
                               final File evalOutputPath,
                               double splitRatio,
                               boolean splitRandom) {

        int totalExamples = 0;
        Writer writerTraining = null;
        Writer writerEvaluation = null;
        try {
            System.out.println("labeled corpus path: " + corpusDir.getPath());
            System.out.println("training data path: " + trainingOutputPath.getPath());
            if (evalOutputPath != null)
                System.out.println("evaluation data path: " + evalOutputPath.getPath());

            // we need first to generate the labeled files from the TEI annotated files
            // we process all tei files in the output directory
            File input = new File(corpusDir.getAbsolutePath());
            File[] refFiles = input.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.endsWith(".tei.xml") || name.endsWith(".tei");
                }
            });
            System.out.println(refFiles.length + " tei files");

            if (refFiles == null) {
                return 0;
            }

            // the file for writing the training data
            writerTraining = new OutputStreamWriter(new FileOutputStream(trainingOutputPath), "UTF8");

            // the file for writing the evaluation data
            if (evalOutputPath != null)
                writerEvaluation = new OutputStreamWriter(new FileOutputStream(evalOutputPath), "UTF8");

            // the active writer
            Writer writer = null;

            // this ratio this the minimum proportion of token with non default label, it is used to
            // decide to keep or not a paragraph without any entities in the training data
            //double ratioNegativeSample = 0.01;

            // get a factory for SAX parser
            SAXParserFactory spf = SAXParserFactory.newInstance();
            for (int n = 0; n < refFiles.length; n++) {
                //if (n > 10)
                //    break;
                File tf = refFiles[n];
                String name = tf.getName();
                System.out.println("Processing: " + name);

                DataseerAnnotationSaxHandler handler = new DataseerAnnotationSaxHandler(classifier);

                //get a new instance of parser
                SAXParser p = spf.newSAXParser();
                p.parse(tf, handler);

                //List<List<Pair<String, String>>> allLabeled = handler.getLabeledResult();
                //labeled = subSample(labeled, ratioNegativeSample);
                List<List<LayoutToken>> segments = handler.getSegments();
                List<String> sectionTypes = handler.getSectionTypes();
                List<Integer> nbDatasets = handler.getNbDatasets();
                List<String> datasetTypes = handler.getDatasetTypes();
                List<String> labels = handler.getLabels();

                // segmentation into training/evaluation is done file by file
                if (splitRandom) {
                    if (Math.random() <= splitRatio)
                        writer = writerTraining;
                    else
                        writer = writerEvaluation;
                } else {
                    if ((double) n / refFiles.length <= splitRatio)
                        writer = writerTraining;
                    else
                        writer = writerEvaluation;
                }

                String featured = DataseerParser.getFeatureVectorsAsString(segments, sectionTypes, nbDatasets, datasetTypes);

                // put the labels
                String[] lines = featured.split("\n");
                for (int i = 0; i < lines.length; i++) {
                    String line = lines[i];
                    writer.write(line);
                    writer.write(" ");
                    writer.write(labels.get(i));
                    writer.write("\n");
                }
                writer.write("\n");
            }
        } catch (Exception e) {
            throw new GrobidException("An exception occured while training GROBID.", e);
        } finally {
            try {
                if (writerTraining != null)
                    writerTraining.close();
                if (writerEvaluation != null)
                    writerEvaluation.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return totalExamples;
    }

    /**
     * Ensure a ratio between positive and negative examples and shuffle
     */
    private List<Pair<String, String>> subSample(List<Pair<String, String>> labeled, double targetRatio) {
        int nbPositionTokens = 0;
        int nbNegativeTokens = 0;

        List<Pair<String, String>> reSampled = new ArrayList<Pair<String, String>>();
        List<Pair<String, String>> newSampled = new ArrayList<Pair<String, String>>();

        boolean hasLabels = false;
        for (Pair<String, String> tagPair : labeled) {
            if (tagPair.getB() == null) {
                // new sequence
                if (hasLabels) {
                    reSampled.addAll(newSampled);
                    reSampled.add(tagPair);
                }
                newSampled = new ArrayList<Pair<String, String>>();
                hasLabels = false;
            } else {
                newSampled.add(tagPair);
                if (!tagPair.getB().equals("<other>") && !tagPair.getB().equals("other") && !tagPair.getB().equals("O"))
                    hasLabels = true;
            }
        }
        return reSampled;
    }


    /**
     * Standard evaluation via the the usual Grobid evaluation framework.
     */
    public String evaluate() {
        File evalDataF = GrobidProperties.getInstance().getEvalCorpusPath(
                new File(new File("resources").getAbsolutePath()), model);

        File tmpEvalPath = getTempEvaluationDataPath();
        createCRFPPData(evalDataF, tmpEvalPath);

        return EvaluationUtilities.evaluateStandard(tmpEvalPath.getAbsolutePath(), getTagger()).toString();
    }

    public String splitTrainEvaluate(Double split, boolean random) {
        System.out.println("Paths :\n" + getCorpusPath() + "\n" + GrobidProperties.getModelPath(model).getAbsolutePath() + "\n" + getTempTrainingDataPath().getAbsolutePath() + "\n" + getTempEvaluationDataPath().getAbsolutePath() + " \nrand " + random);

        File trainDataPath = getTempTrainingDataPath();
        File evalDataPath = getTempEvaluationDataPath();

        final File dataPath = trainDataPath;
        createCRFPPData(getCorpusPath(), dataPath, evalDataPath, split);
        GenericTrainer trainer = TrainerFactory.getTrainer(model);

        if (epsilon != 0.0)
            trainer.setEpsilon(epsilon);
        if (window != 0)
            trainer.setWindow(window);
        if (nbMaxIterations != 0)
            trainer.setNbMaxIterations(nbMaxIterations);

        final File tempModelPath = new File(GrobidProperties.getModelPath(model).getAbsolutePath() + NEW_MODEL_EXT);
        final File oldModelPath = GrobidProperties.getModelPath(model);

        trainer.train(getTemplatePath(), dataPath, tempModelPath, GrobidProperties.getWapitiNbThreads(), model);

        // if we are here, that means that training succeeded
        renameModels(oldModelPath, tempModelPath);

        return EvaluationUtilities.evaluateStandard(evalDataPath.getAbsolutePath(), getTagger()).toString();
    }

    protected final File getCorpusPath() {
        return new File(datastetConfiguration.getCorpusPath());
    }

    protected final File getTemplatePath() {
        return new File(datastetConfiguration.getTemplatePath());
    }

    /**
     * Command line execution.
     *
     * @param args Command line arguments.
     */
    public static void main(String[] args) {
        DatastetConfiguration datastetConfiguration = readConfiguration(args[0]).getDatastetConfiguration();

        if (datastetConfiguration == null) {
            throw new IllegalStateException("Dataseer configuration file not found or not valid.");
        }

        try {
            String pGrobidHome = datastetConfiguration.getGrobidHome();

            GrobidHomeFinder grobidHomeFinder = new GrobidHomeFinder(Collections.singletonList(pGrobidHome));
            GrobidProperties.getInstance(grobidHomeFinder);

            System.out.println(">>>>>>>> GROBID_HOME=" + GrobidProperties.get_GROBID_HOME_PATH());
        } catch (final Exception exp) {
            System.err.println("GROBID dataseer initialisation failed: " + exp);
            exp.printStackTrace();
        }

        Trainer trainer = new DataseerTrainer(datastetConfiguration);
        AbstractTrainer.runTraining(trainer);
        System.out.println(AbstractTrainer.runEvaluation(trainer));
        System.exit(0);
    }

    public void setDatastetConfiguration(DatastetConfiguration config) {
        this.datastetConfiguration = config;
    }
}