package edu.umd.cs.satune;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;

public class SimultedAnnealing extends AbstractSearchAlgorithm {
	double Ts = 0.00001, T0 = 1, R = 0.0001;
	int revisitCount = 0;

	public SimultedAnnealing(AsbractModel fitnessF, AsbractModel filterF, double threshold, int seed,
			LinkedHashMap<String, String[]> optionMap) {
		super(fitnessF, filterF, threshold, seed, optionMap);
		Utils.log("SimultedAnnealing:" + Ts + ":" + T0 + ":" + R);
	}

	@Override
	public int search(String task, String category, String[] configDef, boolean verdict) throws Exception {
		long start = System.currentTimeMillis();
		int runResult = -9;
		double T = T0;

		int configCounter = 0, acceptCounter = 0, saRunCounter = 0, bestCounter = 0;
		revisitCount = 0;

		String[] neighbor, config = configDef, features = Utils.getFeatures(task);
		double neighCost, cost = getCost(category, config, features);
		double delta, score = getScore(category, config, features);
		double bestCost = cost;

		Utils.reportSearchStart(task, features, score, cost, threshold);
		while (T > Ts && !(runResult == 10 || runResult == 0)) {
			do {
				neighbor = generateNeigboringConfig(config);
				neighCost = getCost(category, neighbor, features);
				delta = neighCost - cost;
				T -= R * T;
				configCounter++;
			} while (delta >= 0 && random.nextDouble() > Math.exp(-delta / T) && T > Ts);
			config = neighbor;
			cost = neighCost;
			acceptCounter++;
			//Utils.log("Accepted:" + Utils.arr2String(config));
			if (bestCost > cost) {
				bestCost = cost;
				score = getScore(category, config, features);
				bestCounter++;
				if (score <= threshold) {
					runResult = Utils.runVerifier(optionMap, task, category, config);
					saRunCounter++;
					Utils.reportVerificationResult(task, category, config, runResult, features, score, cost, verdict);
				}
			}
		}

		double timeSc = (double) (System.currentTimeMillis() - start) / 1000;
		Utils.reportSearchEnd(task, config, runResult, timeSc, score, cost, configCounter, revisitCount, acceptCounter,
				saRunCounter, bestCounter, verdict);
		return runResult;
	}

	private double getCost(String category, String[] config, String[] features) throws Exception {
		return fitnessF.getName().contains("Classifier")
				? (double) 1 - fitnessF.query(features, config, correctClasses, category) // classifier use
				: fitnessF.query(features, config, null, category); // regressor use
	}

	private double getScore(String category, String[] config, String[] features) throws Exception {
		return filter.getName().contains("Classifier") ? filter.query(features, config, incorrectClasses, category)
				: filter.query(features, config, null, category);
	}

	String[] generateNeigboringConfig(String[] config) {
		String[] neighbor = Arrays.copyOf(config, config.length);
		int optIndx, valueIndx, i = -1;
		Set<Integer> options2Change = new HashSet<>();

		int numberOfChanges = 1;// just change one thing
		while (numberOfChanges > options2Change.size()) {
			optIndx = random.nextInt(config.length);
			if (!Utils.forbiddenOpts.contains(optIndx + "=_")) // screening condition: wild-card  
				options2Change.add(optIndx);
		}

		for (String[] values : optionMap.values()) {
			if (!options2Change.contains(++i)) continue;
			do {
				valueIndx = random.nextInt(values.length);
			} while (neighbor[i].equals(values[valueIndx]) //assure a different value 
					|| Utils.forbiddenOpts.contains(i + "=" + valueIndx)); // screening condition: certain value 
			neighbor[i] = values[valueIndx];
		}
		String nStr = Utils.arr2String(neighbor);
		if (!testedConfigs.add(nStr)) {
			revisitCount++;
			Utils.log("Re-evaluating config:" + nStr + "\nNumber of visited configs:" + testedConfigs.size());
		}
		return neighbor;
	}
}