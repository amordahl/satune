package edu.umd.cs.satune;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.PriorityQueue;

public class AStar extends AbstractSearchAlgorithm {

	private int CAPACITY = 1000;
	private String category;
	private String[] features;

	class ConfigComparator implements Comparator<String[]> {

		@Override
		public int compare(String[] o1, String[] o2) {
			try {
				return (int) (getCost(category, o1, features) - getCost(category, o2, features));
			} catch (Exception e) {
				e.printStackTrace();
			}
			return -99;
		}
	}

	class ConfigIterator implements Iterator<String[]> {
		int optIndex = 0;
		int valIndex = 0;
		String[] config;
		String[] curr;
		Iterator<Entry<String, String[]>> iterator;

		public ConfigIterator(String[] config) {
			this.config = config;
			iterator = optionMap.entrySet().iterator();
			curr = iterator.next().getValue();
		}

		@Override
		public boolean hasNext() {
			return optIndex < optionMap.size();
		}

		@Override
		public String[] next() { // TODO implement screening restrictions
			String[] neighbor = Arrays.copyOf(config, config.length);
			neighbor[optIndex] = curr[valIndex++];
			config = neighbor;

			if (curr.length <= valIndex && ++optIndex < optionMap.size()) {
				valIndex = 0;
				curr = iterator.next().getValue();
			}
			return config;
		}
	}

	public AStar(AsbractModel fitnessF, AsbractModel filterF, double threshold, int seed,
			LinkedHashMap<String, String[]> optionMap) {
		super(fitnessF, filterF, threshold, seed, optionMap);
		Utils.log("A* starts");
	}

	@Override
	public int search(String task, String category, String[] configDef, boolean verdict) throws Exception {
		long start = System.currentTimeMillis();
		int runResult = -9;
		int configCounter = 0, acceptCounter = 0, saRunCounter = 0, bestCounter = 0; // debugging params
		this.category = category;

		String[] neighbor, config = configDef;
		features = Utils.getFeatures(task);
		double cost = 0, gScore = 0; //getCost(category, config, features), getScore(category, config, features);

		PriorityQueue<String[]> open = new PriorityQueue<>(CAPACITY, new ConfigComparator());
		open.add(config);
		Utils.reportSearchStart(task, features, gScore, cost, threshold);
		while (!open.isEmpty()) {
			config = open.poll();
			gScore = getScore(category, config, features);
			if (gScore <= threshold) {
				runResult = Utils.runVerifier(optionMap, task, category, config);
				Utils.reportVerificationResult(task, category, config, runResult, features, gScore, cost, verdict);
				saRunCounter++;
				if (runResult == 10 || runResult == 0) break;
			}
			ConfigIterator it = new ConfigIterator(config);
			while (it.hasNext()) {
				neighbor = (String[]) it.next();
				if (testedConfigs.contains(Utils.arr2String(neighbor))) continue;
				if (!open.contains(neighbor)) {
					open.add(neighbor);
					configCounter++;
				}
			}
			testedConfigs.add(Utils.arr2String(config));
		}
		double timeSc = (double) (System.currentTimeMillis() - start) / 1000;
		Utils.reportSearchEnd(task, config, runResult, timeSc, gScore, cost, configCounter, configCounter, acceptCounter,
				saRunCounter, bestCounter, verdict);
		return runResult;
	}

	private double getCost(String category, String[] config, String[] features) throws Exception {
		return fitnessF.getName().contains("Classifier")
				? (double) 1 - fitnessF.query(features, config, correctClasses, category) // classifier use
				: (double) fitnessF.query(features, config, null, category); // regressor use
	}

	private double getScore(String category, String[] config, String[] features) throws Exception {
		return filter.getName().contains("Classifier") ? filter.query(features, config, incorrectClasses, category)
				: filter.query(features, config, null, category);
	}
}