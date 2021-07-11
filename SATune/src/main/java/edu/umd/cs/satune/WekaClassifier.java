package edu.umd.cs.satune;

import weka.attributeSelection.PrincipalComponents;
import weka.classifiers.evaluation.Evaluation;
import weka.classifiers.meta.RandomCommittee;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.RandomForest;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SerializationHelper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.util.Arrays;
import java.util.Set;

public class WekaClassifier extends AsbractModel {
	private Instances header;
	private Instances headerPCA;
	private Attribute label;
	private weka.classifiers.Classifier classifier;
	private boolean filter = false;
	static final double UNK_VAL = 0.0;
	private PrincipalComponents pca = null;

	public WekaClassifier(int classIndex, String algorithm, int seed) {
		super("WekaClassifier-" + algorithm, seed);
		this.classIndex = classIndex;
		switch (algorithm) {
			case "RandomForest":
				classifier = new RandomForest();
				break;
			case "J48":
				classifier = new J48();
				break;
			case "RandomCommittee":
				classifier = new RandomCommittee();
				break;
			default:
				throw new IllegalStateException("Unexpected value: " + algorithm);
		}
	}

	public WekaClassifier(int classIndex, String algorithm, boolean filter, int seed) {
		super("WekaClassifier" + algorithm, seed);
		this.classIndex = classIndex;
		this.filter = filter;
		switch (algorithm) {
			case "RandomForest":
				classifier = new RandomForest();
				break;
			case "J48":
				classifier = new J48();
				break;
			case "RandomCommittee":
				classifier = new RandomCommittee();
				break;
			default:
				throw new IllegalStateException("Unexpected value: " + algorithm);
		}
	}

	@Override
	public void loadModelFromFile(String modelFile) throws Exception {
		classifier = (weka.classifiers.Classifier) SerializationHelper.read(new FileInputStream(modelFile));
	}

	@Override
	public void retrain(String[] features, String[] config, String category) {
		// TODO nothing
	}

	@Override
	public double query(String[] features, String[] config, Set<String> classes, String category) throws Exception {
		Instance inst = createInstance(features, config, category);
		inst.setMissing(classIndex - 1);
		inst.setDataset(header);
		if (pca != null) {
			inst = pca.convertInstance(inst);
			inst.setDataset(headerPCA);
		}
		double[] votes = classifier.distributionForInstance((weka.core.Instance) inst);
		double prop = 0;
		for (String clazz : classes)
			prop += votes[label.indexOfValue(clazz)];
		return prop / Arrays.stream(votes).sum();
	}

	@Override
	public void trainModel(String arffFile) throws Exception {
		Utils.log("Training on " + arffFile);
		String postFix = "-" + name + "-" + (filter ? "filter" : "fitness") + (pca != null ? "-pca" : "") + "-" + seed
				+ ".mdl";
		String saveFile = arffFile.replace(".arff", postFix);

		Instances trainSet = new Instances(new BufferedReader(new FileReader(arffFile)));
		trainSet.setClassIndex(classIndex);
		header = trainSet.firstInstance().dataset();
		label = trainSet.classAttribute();
		
		if (new File(saveFile).exists()) {
			Utils.log("Loading model file:" + saveFile);
			loadModelFromFile(saveFile);
		} else {
			if (filter) trainSet.removeIf(dp -> dp.classValue() == UNK_VAL);

			if (pca != null) {
				pca.buildEvaluator(trainSet);
				trainSet = pca.transformedData(trainSet);
				headerPCA = trainSet.firstInstance().dataset();
			}
			classifier.buildClassifier(trainSet);
			Utils.log("Trained on " + trainSet.size() + " many instances");
			saveModeltoFile(saveFile);
		}
	}

	@Override
	public double testModel(String arffFile) throws Exception {
		Utils.log("Testing on " + arffFile);
		int numCorrect = 0;
		Instances testSet = new Instances(new BufferedReader(new FileReader(arffFile)));
		testSet.setClassIndex(classIndex);
		if (filter) testSet.removeIf(dp -> dp.classValue() == UNK_VAL);
		if (pca != null) testSet = pca.transformedData(testSet);

		Evaluation evaluation = new Evaluation(testSet);
		evaluation.useNoPriors();
		double[] predictions = new double[testSet.numInstances()];
		for (int j = 0; j < testSet.numInstances(); j++) {
			Instance instance = testSet.instance(j);
			predictions[j] = classifier.classifyInstance(instance);//TODO double check
			if (predictions[j] == instance.classValue()) {
				numCorrect++;
			}
		}
		double acc = (double) numCorrect / testSet.size();
		Utils.log("Correctly classified " + numCorrect + " instances out of " + testSet.size() + ": acc: " + acc);
		Utils.log(arffFile + "," + seed + "," + filter + "," + acc);
		return acc;
	}

	private Instance createInstance(String[] features, String[] config, String category) {
		Instance inst = new DenseInstance(features.length + config.length + 2);
		for (int i = 0; i < config.length; i++) {
			Attribute att = header.attribute(i);
			double val = att.isNumeric() ? Double.parseDouble(config[i]) : (double) att.indexOfValue(config[i]);
			inst.setValue(i, val);
		}
		for (int i = 0; i < features.length; i++) {
			int indx = config.length + i;
			Attribute att = header.attribute(indx);
			double val = att.isNumeric() ? Double.parseDouble(features[i]) : (double) att.indexOfValue(features[i]);
			inst.setValue(indx, val);
		}

		inst.setValue(classIndex, header.attribute("category").indexOfValue(category));
		return inst;
	}

	@Override
	public void initializePCA() {
		Utils.log("Initializing PCA");
		pca = new PrincipalComponents();
	}

	@Override
	public void saveModeltoFile(String modelFile) throws Exception {
		Utils.log("Saving model file:" + modelFile);
		weka.core.SerializationHelper.write(modelFile, classifier);
	}
}