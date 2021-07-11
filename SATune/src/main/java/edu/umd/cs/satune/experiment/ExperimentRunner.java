package edu.umd.cs.satune.experiment;

import edu.umd.cs.satune.Utils;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class ExperimentRunner {
    public static void main(String[] args) throws Exception {

        CommandLineParser parser = new DefaultParser();
        Options options = Utils.prepareExperimentOptions();
        CommandLine cmd;
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("java -jar autotune.jar [options]", options);
            return;
        }

        String search = cmd.getOptionValue("search");
        String neighStrat = cmd.hasOption("anneal-neighbor-strat") ? cmd.getOptionValue("anneal-neighbor-strat") : "base";
        String tool = cmd.getOptionValue("tool");
        boolean pcaOn = cmd.hasOption("pca");
        double threshold = Double.parseDouble(cmd.getOptionValue("threshold"));
        int seed = Integer.parseInt(cmd.getOptionValue("seed"));
        boolean improve = cmd.hasOption("improve");

        Experiment exp;

        Utils.debugStr = search + "," + tool + "," + pcaOn + "," + threshold + "," + seed + "," + neighStrat + "," + improve;
        Utils.logFile = Utils.PROJECT_DIR + "/tools/log_" + Utils.debugStr.replaceAll(",", "_") + ".txt";
        Utils.log("Initializing subject: " + tool + ", search:" + search + ", PCA :" + pcaOn + ", threshold:" + threshold
            + ", seed:" + seed + ", neighbor generation strategy:" + neighStrat);
        if (tool.equalsIgnoreCase("cbmc"))
            exp = new ExperimentCBMC(search, pcaOn, neighStrat, threshold, seed, improve);
        else if (tool.equalsIgnoreCase("jbmc"))
            exp = new ExperimentJBMC(search, pcaOn, neighStrat, threshold, seed, improve);
        else if (tool.equalsIgnoreCase("jayhorn"))
            exp = new ExperimentJayHorn(search, pcaOn, neighStrat, threshold, seed, improve);
        else if (tool.equalsIgnoreCase("symbiotic"))
            exp = new ExperimentSymbiotic(search, pcaOn, neighStrat, threshold, seed, improve);
        else
            throw new IllegalStateException("Unexpected value: " + tool);
        exp.run("RandomForest");
    }
}