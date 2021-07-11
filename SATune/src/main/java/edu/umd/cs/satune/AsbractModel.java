package edu.umd.cs.satune;

import java.util.Set;

public abstract class AsbractModel {

	int classIndex;
	String name;
	int seed;

	public AsbractModel(String name, int seed) {
		this.name = name;
		this.seed = seed;
		Utils.log("Initializing: " + name + ", random seed: " + seed);
	}

	public abstract void loadModelFromFile(String modelFile) throws Exception;

	public abstract void saveModeltoFile(String modelFile) throws Exception;

	public abstract void retrain(String[] features, String[] config, String category);

	public abstract double query(String[] features, String[] config, Set<String> classes, String category)
			throws Exception;

	public abstract void initializePCA();

	public abstract void trainModel(String arffFile) throws Exception;

	public abstract double testModel(String arffFile) throws Exception;

	public String getName() {
		return name;
	}
}
