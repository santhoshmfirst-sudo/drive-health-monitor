# Real-Time Drive Health Monitoring System

A Java system that monitors an elevator drive (motor/traction unit) by analysing simulated voltage waveforms and predicting faults before breakdown.

## How it works
1. Simulator: normal waveform plus 5 faults (overvoltage, voltage sag, harmonic distortion, bearing fault, transient spikes)
2. Feature extraction (FFT, Apache Commons Math): RMS, THD, crest factor, zero-crossing rate, sideband ratio
3. Machine learning: Weka J48 decision tree classifies each waveform
4. Dashboard (JavaFX): live waveform, Green/Yellow/Red status, predicted fault

## Tech stack
Java 17, JavaFX, Apache Commons Math, Weka, Maven

## Run it
Train the model:

    mvn compile exec:java -Dexec.mainClass=com.drivehealth.ml.ModelTrainer

Start the dashboard:

    mvn javafx:run
