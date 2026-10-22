package com.drivehealth.ui;

import com.drivehealth.ml.DriveClassifier;
import com.drivehealth.simulator.SignalSimulator;
import com.drivehealth.simulator.SignalSimulator.FaultType;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

public class App extends Application {

    private final SignalSimulator sim = new SignalSimulator();
    private final Random rnd = new Random();
    private DriveClassifier classifier;

    private final XYChart.Series<Number, Number> series = new XYChart.Series<>();
    private final Label status = new Label("STARTING");
    private final Label faultLabel = new Label("Predicted fault: -");
    private final Label confLabel = new Label("Confidence: -");

    @Override
    public void start(Stage stage) throws Exception {
        classifier = new DriveClassifier();

        NumberAxis x = new NumberAxis();
        x.setLabel("Sample");
        NumberAxis y = new NumberAxis(-3, 3, 1);
        y.setAutoRanging(false);
        y.setLabel("Amplitude");

        LineChart<Number, Number> chart = new LineChart<>(x, y);
        chart.setTitle("Live motor voltage waveform");
        chart.setCreateSymbols(false);
        chart.setAnimated(false);
        chart.setLegendVisible(false);
        chart.getData().add(series);

        status.setStyle("-fx-font-size: 28px; -fx-text-fill: white; -fx-padding: 10 40; "
                + "-fx-background-color: gray; -fx-font-weight: bold;");
        faultLabel.setStyle("-fx-font-size: 20px;");
        confLabel.setStyle("-fx-font-size: 16px;");

        VBox root = new VBox(12, status, faultLabel, confLabel, chart);
        root.setPadding(new Insets(15));
        root.setAlignment(Pos.CENTER);

        stage.setTitle("Drive Health Monitor");
        stage.setScene(new Scene(root, 900, 600));
        stage.show();

        // Every 1 second: simulate a new waveform, classify it, update the screen
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> update()));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
        update();
    }

    private void update() {
        try {
            FaultType[] all = FaultType.values();
            // About half the time the drive is healthy, otherwise a random fault
            FaultType actual = rnd.nextBoolean() ? FaultType.NORMAL : all[rnd.nextInt(all.length)];
            double[] signal = sim.generate(actual);

            DriveClassifier.Result r = classifier.classify(signal);

            List<XYChart.Data<Number, Number>> points = new ArrayList<>();
            for (int i = 0; i < 256; i++) {
                points.add(new XYChart.Data<>(i, signal[i]));
            }
            series.getData().setAll(points);

            String color;
            String text;
            if (r.confidence() < 0.90) {
                color = "#e6b800";
                text = "WARNING";
            } else if (r.type() == FaultType.NORMAL) {
                color = "#2e9e44";
                text = "HEALTHY";
            } else {
                color = "#d63030";
                text = "FAULT";
            }
            status.setText(text);
            status.setStyle("-fx-font-size: 28px; -fx-text-fill: white; -fx-padding: 10 40; "
                    + "-fx-font-weight: bold; -fx-background-color: " + color + ";");
            faultLabel.setText("Predicted fault: " + r.type());
            confLabel.setText(String.format("Confidence: %.0f%%   (simulated: %s)",
                    r.confidence() * 100, actual));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
