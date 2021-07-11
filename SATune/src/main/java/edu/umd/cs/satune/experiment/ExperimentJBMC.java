package edu.umd.cs.satune.experiment;

import edu.umd.cs.satune.Utils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.HashSet;

public class ExperimentJBMC extends Experiment {
	public ExperimentJBMC(String search, boolean pcaOn, String neighStrat, double threshhold, int seed, boolean improve)
			throws IOException {
		super(search, pcaOn, neighStrat, threshhold, seed, improve);
		domFile = Utils.PROJECT_DIR + "/tools/jbmc/Config-space-27opts.txt";
		defConfigFile = Utils.PROJECT_DIR + "/tools/jbmc/Config-default.txt";
		trainSetPtrn = Utils.PROJECT_DIR + "/tools/jbmc/data/train-XYZ.arff";
		testSetPtrn = Utils.PROJECT_DIR + "/tools/jbmc/data/test-XYZ.arff";
		testSplitsFile = Utils.PROJECT_DIR + "/data/splits/java-testset-XYZ.txt";
		defConfigCacheFile = Utils.PROJECT_DIR + "/tools/jbmc/data/def_completes.txt";

		configMap = Utils.readConfigFile(domFile);
		defConfig = Utils.readDefaultConfig(defConfigFile);
		completedSet = new HashSet<String>();
		if (improveOn)
			completedSet.addAll(Files.readAllLines(new File(defConfigCacheFile).toPath(), Charset.defaultCharset()));

		classIndex = 32 + 27;
		Utils.BENCH_DIR = Utils.PROJECT_DIR + "/sv-comp/benchmarks/java";
		Utils.featureFile = Utils.PROJECT_DIR + "/data/features/java-program-features.txt";
		Utils.TOOL = Utils.PROJECT_DIR + "/tools/jbmc/jbmc";
		Utils.cmdPattern = "--propertyfile " + Utils.BENCH_DIR + "/properties/assert.prp " + Utils.BENCH_DIR + "/common "
				+ Utils.BENCH_DIR + "/TARGETFILE CONFIG";
		Utils.saveFile = "jbmc-newdata-" + System.currentTimeMillis() + ".txt";
		// forbidden options
		Utils.forbiddenOpts = new HashSet<>();
		if (neighStrat.equalsIgnoreCase("conservative")) { //forbid anything that increase Fs
			Utils.forbiddenOpts.add("2=_"); // full slice 1
			Utils.forbiddenOpts.add("6=_"); // --localize-faults 0
			Utils.forbiddenOpts.add("16=_"); // --symex-driven-lazy-loading:1
			Utils.forbiddenOpts.add("17=1"); // --depth 100
			Utils.forbiddenOpts.add("17=2"); // --depth 1000
			Utils.forbiddenOpts.add("23=0"); // path 0
			Utils.forbiddenOpts.add("25=0"); // reachability-slice 0
		} else if (neighStrat.equalsIgnoreCase("greedy")) {//forbid anything that increase only Fs
			Utils.forbiddenOpts.add("2=_"); // full slice 0
			Utils.forbiddenOpts.add("6=_"); // --localize-faults 0
			Utils.forbiddenOpts.add("23=0"); // path 0
			Utils.forbiddenOpts.add("25=0"); // reachability-slice 0
		} //base forbid nothing
	}

	@Override
	public String getCategory(String file) {
		return "assert";
	}

	@Override
	public boolean isTrue(String str) {
		return str.contains(",True");
	}
}