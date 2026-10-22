package com.drivehealth.ml;

import com.drivehealth.features.FeatureExtractor;
import com.drivehealth.simulator.SignalSimulator;
import com.drivehealth.simulator.SignalSimulator.FaultType;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import weka.classifiers.Evaluation;
import weka.classifiers.trees.J48;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;
import weka.core.SerializationHelper;

public class ModelTrainer {

    public static final int SAMPLES_PER_CLASS = 300;
    public static final String MODEL_PATH = "model/j48.model";

    public static Instances buildDataset() {
        ArrayList<Attribute> attrs = new ArrayList<>();
        for (String name : FeatureExtractor.NAMES) {
            attrs.add(new Attribute(name));
        }
        List<String> classes = new ArrayList<>();
        for (FaultType t : FaultType.values()) {
            classes.add(t.name());
        }
        attrs.add(new Attribute("class", classes));

        Instances data = new Instances("drive", attrs, 0);
        data.setClassIndex(attrs.size() - 1);

        SignalSimulator sim = new SignalSimulator();
        FeatureExtractor fx = new FeatureExtractor();

        for (FaultType type : FaultType.values()) {
            for (int i = 0; i < SAMPLES_PER_CLASS; i++) {
                double[] f = fx.extract(sim.generate(type));
                DenseInstance inst = new DenseInstance(attrs.size());
                inst.setDataset(data);
                for (int k = 0; k < f.length; k++) {
                    inst.setValue(k, f[k]);
                }
                inst.setClassValue(type.name());
                data.add(inst);
            }
        }
        return data;
    }

    public static void main(String[] args) throws Exception {
        Instances data = buildDataset();

        // 10-fold cross-validation
        Evaluation eval = new Evaluation(data);
        eval.crossValidateModel(new J48(), data, 10, new Random(1));
        System.out.println(eval.toSummaryString("\n=== Cross-validation ===\n", false));
        System.out.println(eval.toMatrixString("=== Confusion matrix ==="));

        // Train on all data and save
        J48 tree = new J48();
        tree.buildClassifier(data);
        SerializationHelper.write(MODEL_PATH, tree);
        System.out.println("Model saved to " + MODEL_PATH);
        System.out.println(tree);
    }
}
