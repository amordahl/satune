package edu.umd.cs.satune.experiment;

import edu.umd.cs.satune.Utils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.HashSet;

public class ExperimentSymbiotic extends Experiment {

	static String reachSafety = "ReachSafety", memSafety = "MemSafety", noOverflow = "NoOverflows",
			termination = "Termination", concurrencySafety = "ConcurrencySafety";

	public ExperimentSymbiotic(String search, boolean pcaOn, String neighStrat, double threshold, int seed,
			boolean improve) throws IOException {
		super(search, pcaOn, neighStrat, threshold, seed, improve);

		domFile = Utils.PROJECT_DIR + "/tools/symbiotic/Config-space-16opts.txt";
		defConfigFile = Utils.PROJECT_DIR + "/tools/symbiotic/Config-default.txt";
		trainSetPtrn = Utils.PROJECT_DIR + "/tools/symbiotic/data/train-XYZ.arff";
		testSetPtrn = Utils.PROJECT_DIR + "/tools/symbiotic/data/test-XYZ.arff";
		defConfigCacheFile = Utils.PROJECT_DIR + "/tools/symbiotic/data/def_completes.txt";
		testSplitsFile = Utils.PROJECT_DIR + "/data/splits/c-testset-XYZ.txt";
		String skipsFile = Utils.PROJECT_DIR + "/tools/symbiotic/data/concurrency_skips.txt";
		classIndex = 34 + 40;

		configMap = Utils.readConfigFile(domFile);
		defConfig = Utils.readDefaultConfig(defConfigFile);
		completedSet = new HashSet<String>();
		if (improveOn) {
			completedSet.addAll(Files.readAllLines(new File(defConfigCacheFile).toPath(), Charset.defaultCharset()));
		}
		if (new File(skipsFile).exists()) {
			completedSet.addAll(Files.readAllLines(new File(skipsFile).toPath(), Charset.defaultCharset()));
		}
		Utils.BENCH_DIR = Utils.PROJECT_DIR + "/sv-comp/benchmarks/c";
		Utils.featureFile = Utils.PROJECT_DIR + "/data/features/c-program-features-1000-sample.txt";
		Utils.TOOL = Utils.PROJECT_DIR + "/tools/symbiotic/symbiotic/bin/symbiotic";
		String fixPart = "--timeout=60 --witness witness.graphml --no-integrity-check";
		Utils.cmdPattern = "--prp=" + Utils.BENCH_DIR + "/CATEGORY.prp " + fixPart + " CONFIG " + Utils.BENCH_DIR
				+ "/TARGETFILE";
		Utils.saveFile = "symbiotic-newdata-" + System.currentTimeMillis() + ".txt";

		// forbidden options
		Utils.forbiddenOpts = new HashSet<>();
		if (neighStrat.equalsIgnoreCase("conservative")) { //forbid anything that increase Fs
			Utils.forbiddenOpts.add("3=_"); // --no-slice:off Increases FN
			Utils.forbiddenOpts.add("8=_"); // --overflow-with-clang:off Increases FN
			Utils.forbiddenOpts.add("14=2"); // optimize-before:before-O2 Increases FN
		} else if (neighStrat.equalsIgnoreCase("greedy")) {//forbid anything that increase only Fs
			Utils.forbiddenOpts.add("0=_"); // --explicit-symbolic off decreases TN by 8 
			Utils.forbiddenOpts.add("3=_"); // --no-slice:off Increases FN
			Utils.forbiddenOpts.add("8=_"); // --overflow-with-clang:off decreases FN
			Utils.forbiddenOpts.add("11=_"); //--undefined-retval-nosym off decreases TP
			Utils.forbiddenOpts.add("13=1"); // --repeat-slicing 1 decreases TP
			Utils.forbiddenOpts.add("14=2"); // optimize-before:before-O2 Increases FN decreases TN
		} //base forbid nothing
	}

	public String getCategory(String file) {
		if (file.contains("overflow"))
			return noOverflow;
		else if (file.contains("valid-"))
			return memSafety;
		else if (file.contains("termination"))
			return termination;
		else if (file.contains("pthread"))
			return concurrencySafety;
		else return reachSafety;
	}

	@Override
	public boolean isTrue(String str) {
		return str.contains("_true-");
	}
}