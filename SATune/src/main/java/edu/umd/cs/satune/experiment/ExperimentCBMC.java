package edu.umd.cs.satune.experiment;

import edu.umd.cs.satune.Utils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.HashSet;

public class ExperimentCBMC extends Experiment {

	static String reachSafety = "ReachSafety", memSafety = "MemSafety", noOverflow = "NoOverflows",
			termination = "Termination", concurrencySafety = "ConcurrencySafety";

	public ExperimentCBMC(String search, boolean pcaOn, String neighStrat, double threshold, int seed, boolean improve)
			throws IOException {
		super(search, pcaOn, neighStrat, threshold, seed, improve);
		domFile = Utils.PROJECT_DIR + "/tools/cbmc/Config-space-21opts.txt";
		defConfigFile = Utils.PROJECT_DIR + "/tools/cbmc/Config-default.txt";
		trainSetPtrn = Utils.PROJECT_DIR + "/tools/cbmc/data/train-XYZ.arff";
		testSetPtrn = Utils.PROJECT_DIR + "/tools/cbmc/data/test-XYZ.arff";
		defConfigCacheFile = Utils.PROJECT_DIR + "/tools/cbmc/data/def_completes.txt";
		testSplitsFile = Utils.PROJECT_DIR + "/data/splits/c-testset-XYZ.txt";

		configMap = Utils.readConfigFile(domFile);
		defConfig = Utils.readDefaultConfig(defConfigFile);
		completedSet = new HashSet<String>();
		if (improveOn)
			completedSet.addAll(Files.readAllLines(new File(defConfigCacheFile).toPath(), Charset.defaultCharset()));

		classIndex = 34 + 45;

		Utils.BENCH_DIR = Utils.PROJECT_DIR + "/sv-comp/benchmarks/c";
		Utils.featureFile = Utils.PROJECT_DIR + "/data/features/c-program-features-1000-sample.txt";
		Utils.TOOL = Utils.PROJECT_DIR + "/tools/cbmc/cbmc";
		Utils.cmdPattern = "--propertyfile " + Utils.BENCH_DIR + "/CATEGORY.prp " + Utils.BENCH_DIR
				+ "/TARGETFILE CONFIG";
		Utils.saveFile = "cbmc-newdata-" + System.currentTimeMillis() + ".txt";

		// forbidden options
		Utils.forbiddenOpts = new HashSet<>();
		if (neighStrat.equalsIgnoreCase("conservative")) { //forbid anything that increase Fs
			Utils.forbiddenOpts.add("1=_"); // --full-slice 1 increases all 4			
			Utils.forbiddenOpts.add("2=_"); // --no-assumptions 1 increase TP and FP
			Utils.forbiddenOpts.add("7=_"); // --partial-loops 0 increasing TN and FN and --partial-loops 1 increase TP and FP
			Utils.forbiddenOpts.add("8=_"); // --refine 1 increase FP and TN
			Utils.forbiddenOpts.add("9=_"); // --refine-strings 0 increase FN FP and TN
			Utils.forbiddenOpts.add("11=1"); // --depth=100
			Utils.forbiddenOpts.add("12=1"); // --unwind=5
			Utils.forbiddenOpts.add("15=0"); // --paths!=0		
			Utils.forbiddenOpts.add("17=_"); // arrays-uf 0 increase TN TP		
			Utils.forbiddenOpts.add("19=3"); // --round-to-zero		
		} else if (neighStrat.equalsIgnoreCase("greedy")) {//forbid anything that increase only Fs
			//Utils.forbiddenOpts.add("9=_"); // --refine-strings 0 increase FN FP and TN
			Utils.forbiddenOpts.add("19=3"); // --round-to-zero increase FP FN and TP decrease TN
			Utils.forbiddenOpts.add("12=1"); // --unwind!=5 increase FN and TN
		}  //base forbid nothing
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