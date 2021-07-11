package edu.umd.cs.satune;

import weka.attributeSelection.PrincipalComponents;
import weka.classifiers.Classifier;
import weka.classifiers.evaluation.Evaluation;
import weka.classifiers.functions.LinearRegression;
import weka.classifiers.functions.SMOreg;
import weka.classifiers.rules.ZeroR;
import weka.classifiers.trees.REPTree;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SerializationHelper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.util.Set;

public class WekaRegressor extends AsbractModel {
	private Instances header;
	private Instances headerPCA;
	private int classIndex;
	private Classifier classifier = null;
	boolean filter = false;
	static final double UNK_SCORE = 50.0;
	private PrincipalComponents pca = null;

	public WekaRegressor(int classIndex, String algorithm, int seed) {
		super("WekaRegressor-" + algorithm, seed);
		this.classIndex = classIndex;
		if (algorithm.equals("LinearRegression")) {
			classifier = new LinearRegression();
		} else if (algorithm.equals("SMOreg")) {
			classifier = new SMOreg();
		} else if (algorithm.equals("REPTree")) {
			classifier = new REPTree();
			((REPTree) classifier).setSeed(seed);
		} else if (algorithm.equals("ZeroR")) {
			classifier = new ZeroR();
		}
	}

	public WekaRegressor(int classIndex, String algorithm, boolean filter, int seed) {
		super("WekaRegressor-" + algorithm, seed);
		this.classIndex = classIndex;
		this.filter = filter;
		if (algorithm.equals("LinearRegression")) {
			classifier = new LinearRegression();
		} else if (algorithm.equals("SMOreg")) {
			classifier = new SMOreg();
		} else if (algorithm.equals("REPTree")) {
			classifier = new REPTree();
			((REPTree) classifier).setSeed(seed);
		} else if (algorithm.equals("ZeroR")) {
			classifier = new ZeroR();
		}
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
		return classifier.classifyInstance(inst);
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
		
		if (new File(saveFile).exists()) {
			Utils.log("Loading model file:" + saveFile);
			loadModelFromFile(saveFile);
		} else {
			if (filter) trainSet.removeIf(dp -> dp.classValue() == UNK_SCORE);

			if (pca != null) {
				pca.buildEvaluator(trainSet);
				trainSet = pca.transformedData(trainSet);
				headerPCA = trainSet.firstInstance().dataset();
			}

			classifier.buildClassifier(trainSet);
			Utils.log("Trained on " + trainSet.size() + " instances");
			saveModeltoFile(saveFile);
		}
	}

	@Override
	public double testModel(String arffFile) throws Exception {
		Utils.log("Testing on " + arffFile);
		Instances testSet = new Instances(new BufferedReader(new FileReader(arffFile)));
		testSet.setClassIndex(classIndex);

		if (filter) testSet.removeIf(dp -> dp.classValue() == UNK_SCORE);
		if (pca != null) testSet = pca.transformedData(testSet);

		Evaluation svmregeval = new Evaluation(testSet);
		svmregeval.evaluateModel(classifier, testSet);
		String errorStr = arffFile + "," + seed + "," + filter + "," + svmregeval.meanAbsoluteError() + ","
				+ svmregeval.rootMeanSquaredError() + "," + svmregeval.relativeAbsoluteError() + ","
				+ svmregeval.rootRelativeSquaredError();
		Utils.log(svmregeval.toSummaryString());
		Utils.log(errorStr);
		return svmregeval.errorRate();
	}

	private Instance createInstance(String[] features, String[] config, String category) {
		Instance inst = new DenseInstance(features.length + config.length + 2);
		for (int i = 0; i < config.length; i++) {
			double val = header.attribute(i).isNumeric() ? Double.parseDouble(config[i])
					: (double) header.attribute(i).indexOfValue(config[i]);
			inst.setValue(i, val);
		}
		for (int i = 0; i < features.length; i++) {
			int indx = config.length + i;
			double val = header.attribute(indx).isNumeric() ? Double.parseDouble(features[i])
					: (double) header.attribute(indx).indexOfValue(features[i]);
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
	public void loadModelFromFile(String modelFile) throws Exception {
		classifier = (Classifier) SerializationHelper.read(new FileInputStream(modelFile));
	}

	@Override
	public void retrain(String[] features, String[] config, String category) {
	}

	@Override
	public void saveModeltoFile(String modelFile) throws Exception {
		Utils.log("Saving model file:" + modelFile);
		weka.core.SerializationHelper.write(modelFile, classifier);
	}
}