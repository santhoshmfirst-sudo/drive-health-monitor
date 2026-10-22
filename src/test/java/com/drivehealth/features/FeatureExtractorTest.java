package com.drivehealth.features;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.drivehealth.simulator.SignalSimulator;
import com.drivehealth.simulator.SignalSimulator.FaultType;
import org.junit.jupiter.api.Test;

class FeatureExtractorTest {

    @Test
    void pureSineHasExpectedFeatures() {
        double[] s = new double[1024];
        for (int i = 0; i < s.length; i++) {
            s[i] = Math.sin(2 * Math.PI * 50 * i / 6400.0);
        }
        double[] f = new FeatureExtractor().extract(s);

        assertEquals(0.707, f[0], 0.01);  // RMS of a sine
        assertEquals(0.0, f[1], 0.01);    // THD of a pure sine
        assertEquals(1.414, f[2], 0.02);  // crest factor of a sine
    }

    @Test
    void printFeaturesForEachFault() {
        SignalSimulator sim = new SignalSimulator();
        FeatureExtractor fx = new FeatureExtractor();

        System.out.printf("%-22s %7s %7s %7s %7s %9s%n",
                "FAULT", "rms", "thd", "crest", "zcr", "sideband");
        for (FaultType type : FaultType.values()) {
            double[] f = fx.extract(sim.generate(type));
            System.out.printf("%-22s %7.3f %7.3f %7.3f %7.3f %9.3f%n",
                    type, f[0], f[1], f[2], f[3], f[4]);
        }
    }
}