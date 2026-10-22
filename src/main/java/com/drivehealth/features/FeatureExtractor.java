package com.drivehealth.features;

import com.drivehealth.simulator.SignalSimulator;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;

public class FeatureExtractor {

    public static final String[] NAMES = {
        "rms", "thd", "crestFactor", "zeroCrossingRate", "sidebandRatio"
    };

    public double[] extract(double[] s) {
        int n = s.length;

        // RMS and peak
        double sumSq = 0;
        double peak = 0;
        for (double v : s) {
            sumSq += v * v;
            peak = Math.max(peak, Math.abs(v));
        }
        double rms = Math.sqrt(sumSq / n);
        double crestFactor = peak / rms;

        // Zero-crossing rate
        int crossings = 0;
        for (int i = 1; i < n; i++) {
            if ((s[i - 1] < 0 && s[i] >= 0) || (s[i - 1] >= 0 && s[i] < 0)) {
                crossings++;
            }
        }
        double zcr = crossings / (double) (n - 1);

        // FFT
        FastFourierTransformer fft = new FastFourierTransformer(DftNormalization.STANDARD);
        Complex[] spec = fft.transform(s, TransformType.FORWARD);
        double binHz = SignalSimulator.SAMPLE_RATE / (double) n;
        int f = (int) Math.round(SignalSimulator.FREQUENCY / binHz); // bin of 50 Hz
        double fundamental = spec[f].abs();

        // THD = sqrt(sum of harmonic powers) / fundamental
        double harmonicPower = 0;
        for (int k = 2 * f; k < n / 2; k += f) {
            double m = spec[k].abs();
            harmonicPower += m * m;
        }
        double thd = Math.sqrt(harmonicPower) / fundamental;

        // Sidebands around 50 Hz (shows up in bearing faults)
        int offset = (int) Math.round(18.75 / binHz);
        double sidebandRatio = (spec[f - offset].abs() + spec[f + offset].abs()) / fundamental;

        return new double[] { rms, thd, crestFactor, zcr, sidebandRatio };
    }
}