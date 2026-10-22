package com.drivehealth.ml;

import com.drivehealth.features.FeatureExtractor;
import com.drivehealth.simulator.SignalSimulator.FaultType;
import weka.classifiers.Classifier;
import weka.core.DenseInstance;
import weka.core.Instances;
import weka.core.SerializationHelper;

public class DriveClassifier {

    public record Result(FaultType type, double confidence) {}

    private final Classifier model;
    private final Instances header;
    private final FeatureExtractor fx = new FeatureExtractor();

    public DriveClassifier() throws Exception {
        model = (Classifier) SerializationHelper.read(ModelTrainer.MODEL_PATH);
        header = new Instances(ModelTrainer.buildDataset(), 0);
    }

    public Result classify(double[] signal) throws Exception {
        double[] f = fx.extract(signal);
        DenseInstance inst = new DenseInstance(header.numAttributes());
        inst.setDataset(header);
        for (int k = 0; k < f.length; k++) {
            inst.setValue(k, f[k]);
        }
        double[] dist = model.distributionForInstance(inst);
        int best = 0;
        for (int i = 1; i < dist.length; i++) {
            if (dist[i] > dist[best]) {
                best = i;
            }
        }
        return new Result(FaultType.values()[best], dist[best]);
    }
}
