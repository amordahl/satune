package edu.umd.cs.satune.experiment;

import edu.umd.cs.satune.AStar;
import edu.umd.cs.satune.AbstractSearchAlgorithm;
import edu.umd.cs.satune.AsbractModel;
import edu.umd.cs.satune.SimultedAnnealing;
import edu.umd.cs.satune.Utils;
import edu.umd.cs.satune.WekaClassifier;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;

public abstract class Experiment {
    static String domFile;
    static String defConfigFile;
    static String trainSetPtrn;
    static String testSetPtrn;
    static String testSplitsFile;
    static String defConfigCacheFile;
    static Set<String> completedSet;
    static Random random;
    static int classIndex;
    LinkedHashMap<String, String[]> configMap;
    int seed;
    String[] defConfig;
    String search;
    String neighStrat;
    double threshold;
    boolean pcaOn;
    boolean improveOn;
    AbstractSearchAlgorithm sa;

    public Experiment(String search, boolean pcaOn, String neighStrat, double threshold, int seed, boolean improveOn) {
        this.search = search;
        this.pcaOn = pcaOn;
        this.seed = seed;
        this.neighStrat = neighStrat;
        this.threshold = threshold;
        this.improveOn = improveOn;
        random = new Random();
        random.setSeed(seed);
    }

    public void run(String algorithm) throws Exception {
        AsbractModel fitnessFunc, filter;
        if (new File(Utils.logFile).exists()) {
            Files.readAllLines(new File(Utils.logFile).toPath(), Charset.defaultCharset()).stream()
                .filter(line -> line.startsWith("Final result:")).forEach(l -> completedSet.add(l.split(",")[1]));
        }
        for (int i = 1; i <= 5; i++) {
            String testFilesF = testSplitsFile.replace("XYZ", "" + i);
            String trainFileI = trainSetPtrn.replace("XYZ", "" + i);
            String testFileI = testSetPtrn.replace("XYZ", "" + i);

            fitnessFunc = new WekaClassifier(classIndex, algorithm, seed);
            if (pcaOn) fitnessFunc.initializePCA();
            fitnessFunc.trainModel(trainFileI);
            fitnessFunc.testModel(testFileI);

            filter = new WekaClassifier(classIndex, algorithm, true, seed);
            if (pcaOn) filter.initializePCA();
            filter.trainModel(trainFileI);
            filter.testModel(testFileI);

            sa = getSearchAlgorithm(search, fitnessFunc, filter);
            List<String> files = Files.readAllLines(new File(testFilesF).toPath(), Charset.defaultCharset());
            for (String line : files) {
                String[] fileVerdict = line.split(",");
                if (!completedSet.contains(fileVerdict[0]))
                    sa.search(fileVerdict[0], getCategory(fileVerdict[0]), defConfig, isTrue(line));
                else Utils.log("Skipping " + fileVerdict[0] + ", already in completed set.");
            }
        }
    }

    private AbstractSearchAlgorithm getSearchAlgorithm(String search, AsbractModel ch, AsbractModel filter) {
        return search.equals("astar") ? new AStar(ch, filter, threshold, random.nextInt(), configMap) :
            new SimultedAnnealing(ch, filter, threshold, random.nextInt(), configMap);
    }

    public abstract String getCategory(String file);

    public abstract boolean isTrue(String str);

}
