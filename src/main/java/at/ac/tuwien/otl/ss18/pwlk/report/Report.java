package at.ac.tuwien.otl.ss18.pwlk.report;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;
import java.util.OptionalInt;

public class Report {
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
    StringBuilder sb = new StringBuilder();
    sb.append("\n\n|               | Construction algorithm                 |");
    if (isOptmizied) {
      sb.append(" Optimization algorithm                 |");
    }
    sb.append("\n|               | " + formatAlgorithmName(constructAlgorithmName) + "|");
    if (isOptmizied) {
      sb.append(" " + formatAlgorithmName(optimizeAlgorithmName) + "|");
    }
    sb.append("\n| Instance name | Min [s] | Max [s] | Avg [s] | #SuccRun |");
    if (isOptmizied) {
      sb.append(" Min [s] | Max [s] | Avg [s] | #SuccRun |");
    }
    if (!isOptmizied) {
     sb.append("\n|--------------------------------------------------------|" );
    } else {
      sb.append("\n|-------------------------------------------------------------------------------------------------|");
    }

    for (InstanceReport instanceReport: instanceReports) {
      sb.append("\n| "
              + String.format("%1$-" + 14 + "s", instanceReport.getInstanceName())
              + "|"
              + formatNumber(instanceReport.getMinRunTimeConstruct())
              + "|"
              + formatNumber(instanceReport.getMaxRunTimeConstruct())
              + "|"
              + formatNumber(instanceReport.getAverageTimeConstruct())
              + "|"
              + formatNumberOfRuns(instanceReport.getNumberOfSuccessfulRunsConstruct(), instanceReport.getNumberOfRunsConstruct()));

      if(isOptmizied) {
        sb.append("|"
                + formatNumber(instanceReport.getMinRunTimeOptimize())
                + "|"
                + formatNumber(instanceReport.getMaxRunTimeOptimize())
                + "|"
                + formatNumber(instanceReport.getAverageTimeOptimize())
                + "|"
                + formatNumberOfRuns(instanceReport.getNumberOfSuccessfulRunsOptimize(), instanceReport.getNumberOfRunsOptimize())
                + "|");
      }
    }

    return sb.toString();
  }

  private String formatNumber(OptionalInt number) {
    return String.format("%1$" + 9 + "s", (number.isPresent() ? number.getAsInt() : "N"));
  }

  private String formatNumber(OptionalDouble number) {
    DecimalFormat formatter = new DecimalFormat("#0.00");
    return String.format("%1$" + 9 + "s", (number.isPresent() ? formatter.format(number.getAsDouble()) : "N"));
  }

  private String formatAlgorithmName(String name) {
    return String.format("%1$-" + 39 + "s", "(" + name + ")");
  }

  private String formatNumberOfRuns(int success, int total) {
    return String.format("%1$" + 10 + "s", success + "/" + total);
  }
}
