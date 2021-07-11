package edu.umd.cs.satune;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Random;
import java.util.Set;

public abstract class AbstractSearchAlgorithm {
	double threshold;
	protected Random random;
	protected Set<String> correctClasses, incorrectClasses;
	protected AsbractModel fitnessF, filter;
	protected LinkedHashMap<String, String[]> optionMap;
	protected HashSet<String> testedConfigs;

	public AbstractSearchAlgorithm(AsbractModel fitnessF, AsbractModel filterF, double threshold, int seed,
			LinkedHashMap<String, String[]> optionMap) {
		this.optionMap = optionMap;
		this.threshold = threshold;
		this.fitnessF = fitnessF;
		this.filter = filterF;

		random = new Random();
		random.setSeed(seed);
		testedConfigs = new HashSet<>();
		correctClasses = new HashSet<>();
		correctClasses.add("TP");
		correctClasses.add("TN");

		incorrectClasses = new HashSet<>();
		incorrectClasses.add("FP");
		incorrectClasses.add("FN");
	}

	public abstract int search(String task, String category, String[] configDef, boolean verdict) throws Exception;

}