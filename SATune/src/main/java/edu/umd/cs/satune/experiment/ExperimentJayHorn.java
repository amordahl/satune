package edu.umd.cs.satune.experiment;

import edu.umd.cs.satune.Utils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.HashSet;

public class ExperimentJayHorn extends Experiment {
	public ExperimentJayHorn(String search, boolean pcaOn, String neighStrat, double threshhold, int seed, boolean improve)
			throws IOException {
		super(search, pcaOn, neighStrat, threshhold, seed, improve);
		domFile = Utils.PROJECT_DIR + "/tools/jayhorn/Config-space-12opts.txt";
		defConfigFile = Utils.PROJECT_DIR + "/tools/jayhorn/Config-competition.txt";
		trainSetPtrn = Utils.PROJECT_DIR + "/tools/jayhorn/data/train-XYZ.arff";
		testSetPtrn = Utils.PROJECT_DIR + "/tools/jayhorn/data/test-XYZ.arff";
		testSplitsFile = Utils.PROJECT_DIR + "/data/splits/java-testset-XYZ.txt";
		defConfigCacheFile = Utils.PROJECT_DIR + "/tools/jayhorn/data/def_completes.txt";

		configMap = Utils.readConfigFile(domFile);
		defConfig = Utils.readDefaultConfig(defConfigFile);
		completedSet = new HashSet<String>();
		if (improveOn)
			completedSet.addAll(Files.readAllLines(new File(defConfigCacheFile).toPath(), Charset.defaultCharset()));

		classIndex = 32 + 12;
		Utils.BENCH_DIR = Utils.PROJECT_DIR + "/sv-comp/benchmarks/java";
		Utils.featureFile = Utils.PROJECT_DIR + "/data/features/java-program-features.txt";
		Utils.TOOL = Utils.PROJECT_DIR + "/tools/jayhorn/jayhorn";
		Utils.cmdPattern = "--propertyfile " + Utils.BENCH_DIR + "/properties/assert.prp " + Utils.BENCH_DIR + "/common "
				+ Utils.BENCH_DIR + "/TARGETFILE CONFIG";
		Utils.saveFile = "jayhorn-newdata-" + System.currentTimeMillis() + ".txt";
		// forbidden options
		Utils.forbiddenOpts = new HashSet<>();
		if (neighStrat.equalsIgnoreCase("conservative")) { //forbid anything that increase Fs
			Utils.forbiddenOpts.add("9=_"); // --solver = spacer	
			Utils.forbiddenOpts.add("3=1"); // -heap-limit:1
			Utils.forbiddenOpts.add("4=1"); // -heap-mode:bounded
			Utils.forbiddenOpts.add("8=4"); // --mem-prec=5
		} else if (neighStrat.equalsIgnoreCase("greedy")) {//forbid anything that increase only Fs
			Utils.forbiddenOpts.add("3=1"); // -heap-limit:1
			Utils.forbiddenOpts.add("4=1"); // -heap-mode:bounded
		} //base forbid nothing
	}

	@Override
	public boolean isTrue(String str) {
		return str.contains(",True");
	}

	@Override
	public String getCategory(String file) {
		return "assert";
	}
}