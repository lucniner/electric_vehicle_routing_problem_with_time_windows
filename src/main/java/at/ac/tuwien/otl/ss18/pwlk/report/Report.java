package at.ac.tuwien.otl.ss18.pwlk.report;

import java.text.DecimalFormat;
import java.util.*;

public class Report {
  private final Map<String, Double> bestSolution = new HashMap<>();
  private boolean isOptmizied;

  private String constructAlgorithmName;
  private int numberOfRunsConstruct;

  private String optimizeAlgorithmName;
  private int numberOfRunsOptimize;

  private List<InstanceReport> instanceReports = new ArrayList<>();

  public Report(boolean isOptimized) {
    this.isOptmizied = isOptimized;
  }

  public String getConstructAlgorithmName() {
    return constructAlgorithmName;
  }

  public void setConstructAlgorithmName(String constructAlgorithmName) {
    this.constructAlgorithmName = constructAlgorithmName;
  }

  public int getNumberOfRunsConstruct() {
    return numberOfRunsConstruct;
  }

  public void setNumberOfRunsConstruct(int numberOfRunsConstruct) {
    this.numberOfRunsConstruct = numberOfRunsConstruct;
  }

  public String getOptimizeAlgorithmName() {
    return optimizeAlgorithmName;
  }

  public void setOptimizeAlgorithmName(String optimizeAlgorithmName) {
    this.optimizeAlgorithmName = optimizeAlgorithmName;
  }

  public int getNumberOfRunsOptimize() {
    return numberOfRunsOptimize;
  }

  public void setNumberOfRunsOptimize(int numberOfRunsOptimize) {
    this.numberOfRunsOptimize = numberOfRunsOptimize;
  }

  public List<InstanceReport> getInstanceReports() {
    return instanceReports;
  }

  public void setInstanceReport(InstanceReport instanceReport) {
    this.instanceReports.add(instanceReport);
  }

  @Override
  public String toString() {
    Collections.sort(instanceReports, Comparator.comparing(InstanceReport::getInstanceName));
    StringBuilder sb = new StringBuilder();
    sb.append("\n");
    sb.append(
            "\n                | Construction algorithm "
                    + formatAlgorithmName(constructAlgorithmName)
                    + "|");
    sb.append(
            "\n                |----------------------------------------------------------------------|");
    sb.append(
            "\n                | Runtime [seconds]           | Distance                    |          |");
    sb.append(
            "\n|---------------|-----------------------------|-----------------------------|----------|");
    sb.append(
            "\n| Instance name | Min     | Max     | Avg     | Min     | Max     | Avg     | #SuccRun |");
    sb.append(
            "\n|--------------------------------------------------------------------------------------|");

    for (InstanceReport instanceReport : instanceReports) {
      sb.append(
              "\n| "
              + String.format("%1$-" + 14 + "s", instanceReport.getInstanceName())
              + "|"
              + formatNumber(instanceReport.getMinRunTimeConstruct())
              + "|"
              + formatNumber(instanceReport.getMaxRunTimeConstruct())
              + "|"
              + formatNumber(instanceReport.getAverageTimeConstruct())
              + "|"
              + formatNumber(instanceReport.getMinDistanceConstruct())
              + "|"
              + formatNumber(instanceReport.getMaxDistanceConstruct())
              + "|"
              + formatNumber(instanceReport.getAverageDistanceConstruct())
              + "|"
                      + formatNumberOfRuns(
                      instanceReport.getNumberOfSuccessfulRunsConstruct(),
                      instanceReport.getNumberOfRunsConstruct())
              + "|");
    }
    sb.append(
            "\n|--------------------------------------------------------------------------------------|");

    if (isOptmizied) {
      sb.append("\n");
      sb.append("\n");
      sb.append(
              "\n                | Optimization algorithm "
                      + formatAlgorithmName(optimizeAlgorithmName)
                      + "|");
      sb.append(
              "\n                |----------------------------------------------------------------------|");
      sb.append(
              "\n                | Runtime [seconds]           | Distance                    |          |");
      sb.append(
              "\n|---------------|-----------------------------|-----------------------------|----------|");
      sb.append(
              "\n| Instance name | Min     | Max     | Avg     | Min     | Max     | Avg     | #SuccRun |");
      sb.append(
              "\n|--------------------------------------------------------------------------------------|");

      for (InstanceReport instanceReport : instanceReports) {
        sb.append(
                "\n| "
                        + String.format("%1$-" + 14 + "s", instanceReport.getInstanceName())
                        + "|"
                        + formatNumber(instanceReport.getMinRunTimeOptimize())
                        + "|"
                        + formatNumber(instanceReport.getMaxRunTimeOptimize())
                        + "|"
                        + formatNumber(instanceReport.getAverageTimeOptimize())
                        + "|"
                        + formatNumber(instanceReport.getMinDistanceOptimize())
                        + "|"
                        + formatNumber(instanceReport.getMaxDistanceOptimize())
                        + "|"
                        + formatNumber(instanceReport.getAverageDistanceOptimize())
                        + "|"
                        + formatNumberOfRuns(
                        instanceReport.getNumberOfSuccessfulRunsOptimize(),
                        instanceReport.getNumberOfRunsOptimize())
                        + "|");
      }

      sb.append(
              "\n|--------------------------------------------------------------------------------------|");
    }
    initBestSolution();

    for (InstanceReport instanceReport : instanceReports) {
      sb.append(
              "\n| "
                      + String.format("%1$-" + 14 + "s", instanceReport.getInstanceName())
                      + "|"
                      + formatNumber(instanceReport.getMinDistanceOptimize())
                      + "|"
                      + formatNumber(OptionalDouble.of(bestSolution.get(instanceReport.getInstanceName())))
                      + "|"
                      + formatBoolean(Double.parseDouble(formatNumber(instanceReport.getMinDistanceOptimize()))
                      <= bestSolution.get(instanceReport.getInstanceName()))
                      + "|");
    }
    sb.append(
            "\n|--------------------------------------------------------------------------------------|");

    return sb.toString();
  }

  private void initBestSolution() {
    bestSolution.put("c103_21", 1012.32);
    bestSolution.put("c105_21", 1033.98);
    bestSolution.put("c204_21", 637.13);
    bestSolution.put("r102_21", 1481.81);
    bestSolution.put("r107_21", 1175.6);
    bestSolution.put("r205_21", 964.92);
    bestSolution.put("r211_21", 765.78);
    bestSolution.put("rc101_21", 1770.97);
    bestSolution.put("rc106_21", 1439.59);
    bestSolution.put("rc203_21", 964.2);
  }

  private String formatNumber(OptionalInt number) {
    return String.format("%1$" + 9 + "s", (number.isPresent() ? number.getAsInt() : "N"));
  }

  private String formatNumber(OptionalDouble number) {
    DecimalFormat formatter = new DecimalFormat("#0.00");
    return String.format(
            "%1$" + 9 + "s", (number.isPresent() ? formatter.format(number.getAsDouble()) : "N"));
  }

  private String formatBoolean(boolean better) {
    return String.format("%1$" + 9 + "s", better);
  }


  private String formatAlgorithmName(String name) {
    return String.format("%1$-" + 46 + "s", "(" + name + ")");
  }

  private String formatNumberOfRuns(int success, int total) {
    return String.format("%1$" + 10 + "s", success + "/" + total);
  }
}
