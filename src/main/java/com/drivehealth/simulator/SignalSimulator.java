package com.drivehealth.simulator;

import java.util.Random;

public class SignalSimulator {

    public enum FaultType {
        NORMAL,
        OVERVOLTAGE,
        VOLTAGE_SAG,
        HARMONIC_DISTORTION,
        BEARING_FAULT,
        TRANSIENT_SPIKES
    }

    public static final int SAMPLE_RATE = 6400;   // samples per second
    public static final int NUM_SAMPLES = 1024;   // power of 2, needed for FFT
    public static final double FREQUENCY = 50.0;  // supply frequency in Hz

    private final Random random = new Random();

    public double[] generate(FaultType type) {
        double[] signal = new double[NUM_SAMPLES];

        // Small random variation so no two signals are identical
        double amplitude = 1.0 + (random.nextDouble() - 0.5) * 0.1;
        double noise = 0.02 + random.nextDouble() * 0.02;

        // Fault settings
        double h5 = 0.08 + random.nextDouble() * 0.17;
        double h7 = 0.05 + random.nextDouble() * 0.10;
        double modIndex = 0.08 + random.nextDouble() * 0.12;
        double modFreq = 18.75;
        int sagStart = 200 + random.nextInt(300);
        int sagLength = 200 + random.nextInt(200);
        double sagFactor = 0.4 + random.nextDouble() * 0.4;

        if (type == FaultType.OVERVOLTAGE) {
            amplitude *= 1.15 + random.nextDouble() * 0.25;
        }

        for (int i = 0; i < NUM_SAMPLES; i++) {
            double t = i / (double) SAMPLE_RATE;
            double w = 2 * Math.PI * FREQUENCY * t;
            double value = amplitude * Math.sin(w);

            switch (type) {
                case HARMONIC_DISTORTION:
                    value += amplitude * (h5 * Math.sin(5 * w) + h7 * Math.sin(7 * w));
                    break;
                case BEARING_FAULT:
                    value = amplitude * (1 + modIndex * Math.sin(2 * Math.PI * modFreq * t)) * Math.sin(w);
                    break;
                case VOLTAGE_SAG:
                    if (i >= sagStart && i < sagStart + sagLength) {
                        value *= sagFactor;
                    }
                    break;
                default:
                    break;
            }

            value += random.nextGaussian() * noise;
            signal[i] = value;
        }

        if (type == FaultType.TRANSIENT_SPIKES) {
            int spikes = 3 + random.nextInt(6);
            for (int s = 0; s < spikes; s++) {
                double size = 1.0 + random.nextDouble() * 1.5;
                signal[random.nextInt(NUM_SAMPLES)] += random.nextBoolean() ? size : -size;
            }
        }

        return signal;
    }
}