package sample;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Font;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.time.LocalDate;

public class Controller {
    @FXML
    private AnchorPane panel;
    @FXML
    private DatePicker startDate;
    @FXML
    private DatePicker endDate;
    @FXML
    private Label maxPLN;
    @FXML
    private Label minPLN;
    @FXML
    private Label maxValue;
    @FXML
    private Label minValue;
    @FXML
    private ComboBox baseComboBox;
    @FXML
    private ComboBox secondComboBox;

    private LineChart lineChart;
    private CategoryAxis xAxis;
    private NumberAxis yAxis;
    private XYChart.Series series;

    @FXML
    private void initialize() {
        maxPLN.setVisible(false);
        minPLN.setVisible(false);
        series = new XYChart.Series();
        xAxis = new CategoryAxis();
        yAxis = new NumberAxis();
        yAxis.setForceZeroInRange(false);
        xAxis.setLabel("Data");
        yAxis.tickLabelFontProperty().set(Font.font(10));
        yAxis.setLabel("Kurs wymiany");
        lineChart = new LineChart(xAxis, yAxis);
        lineChart.setTitle("Kursy walut");
        lineChart.setLayoutX(10);
        lineChart.setLayoutY(90);
        lineChart.setPrefSize(1050, 710);
        lineChart.setLegendVisible(false);
        lineChart.getData().add(series);
        baseComboBox.getItems().addAll("EUR", "AUD", "BGN", "CAD", "CHF", "CNY", "CZK", "DKK", "EFK", "GBP", "HDK", "HUF", "ISK", "JPY", "KRW", "LTL", "MTL", "NOK", "NZD", "PHP", "PLN", "ROL", "RUB", "SEK", "SGD", "SIT", "SKK", "TRL", "USD", "ZAR");
        secondComboBox.getItems().addAll(baseComboBox.getItems());
        baseComboBox.setValue("PLN");
        secondComboBox.setValue("EUR");

        panel.getChildren().add(lineChart);
    }

    @FXML
    private void handleReadCoursesButton() {
        if (startDate.getValue() != null && endDate.getValue() != null) {
            LocalDate start = startDate.getValue();
            LocalDate end = endDate.getValue();
            minPLN.setVisible(true);
            maxPLN.setVisible(true);
            series.getData().clear();
            lineChart.setTitle("Kurs walut " + baseComboBox.getValue().toString() + " na " + secondComboBox.getValue().toString());
            maxValue.setText("Największa wartość " + baseComboBox.getValue().toString() + " w dniu:");
            minValue.setText("Najmniejsza wartość " + baseComboBox.getValue().toString() + " w dniu:");

            if (baseComboBox.getValue().toString().equals(secondComboBox.getValue().toString())) {
                minPLN.setText(startDate.toString());
                maxPLN.setText(startDate.toString());
            }

            Thread t = new Thread(() -> {
                double min = 50000000, max = 0;
                LocalDate forMin = startDate.getValue(), forMax = startDate.getValue();

                for (LocalDate date = start; date.isBefore(end); date = date.plusDays(1)) {
                    if (baseComboBox.getValue().toString().equals(secondComboBox.getValue().toString())) {
                        LocalDate finalDate = date;
                        Platform.runLater(() -> {
                            series.getData().add(new XYChart.Data(finalDate.toString(), 1));
                        });
                    } else {
                        try {
                            URL url = new URL("http://api.fixer.io/" + date + "?base=" + baseComboBox.getValue().toString());
                            URLConnection urlConnection = url.openConnection();
                            InputStreamReader in = new InputStreamReader(urlConnection.getInputStream());
                            ObjectMapper mapper = new ObjectMapper();
                            ExchangeRate er = mapper.readValue(in, ExchangeRate.class);
                            if (er.getRates().get(secondComboBox.getValue().toString()) < min) {
                                min = er.getRates().get(secondComboBox.getValue().toString());
                                forMin = date;
                            }
                            if (er.getRates().get(secondComboBox.getValue().toString()) > max) {
                                max = er.getRates().get(secondComboBox.getValue().toString());
                                forMax = date;
                            }

                            LocalDate finalForMin = forMin;
                            LocalDate finalForMax = forMax;
                            final LocalDate d = date;
                            Platform.runLater(() -> {
                                series.getData().add(new XYChart.Data(d.toString(), er.getRates().get(secondComboBox.getValue().toString())));
                                minPLN.setText(finalForMin.toString());
                                maxPLN.setText(finalForMax.toString());
                            });
                            in.close();
                            Thread.sleep(180);
                        } catch (IOException | InterruptedException e) {
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Błąd!");
                            alert.setHeaderText("Błady przy odczycie danych!");
                            alert.showAndWait();
                        }
                    }
                }
            });
            t.setDaemon(true);
            t.start();
        }
    }
}
