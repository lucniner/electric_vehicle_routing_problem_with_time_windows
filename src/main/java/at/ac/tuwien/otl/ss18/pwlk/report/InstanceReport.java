package at.ac.tuwien.otl.ss18.pwlk.report;

import at.ac.tuwien.otl.ss18.pwlk.valueobjects.SolutionInstance;

import java.util.*;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.lang.Math.toIntExact;

public class InstanceReport {
  private String instanceName;

  private List<Optional<SolutionInstance>> solutionInstances = new ArrayList<>();
  private List<Optional<SolutionInstance>> optimizedInstances = new ArrayList<>();

  private List<OptionalInt> runTimeConstruct = new ArrayList<>();
  private List<OptionalInt> runTimeOptimize = new ArrayList<>();


  public InstanceReport(String instanceName) {
    this.instanceName = instanceName;
  }

  public String getInstanceName() {
    return instanceName;
  }

  public void setInstanceName(String instanceName) {
    this.instanceName = instanceName;
  }

  public List<OptionalInt> getRunTimeConstruct() {
    return runTimeConstruct;
  }

  public void addRunTimeConstruct(OptionalInt runTimeConstruct) {
    this.runTimeConstruct.add(runTimeConstruct);
  }

  public List<OptionalInt> getRunTimeOptimize() {
    return runTimeOptimize;
  }

  public void addRunTimeOptimize(OptionalInt runTimeOptimize) {
    this.runTimeOptimize.add(runTimeOptimize);
  }

  public OptionalInt getMinRunTimeConstruct() {
    return getRunTimeStream(runTimeConstruct).min();
  }

  public OptionalInt getMaxRunTimeConstruct() {
    return getRunTimeStream(runTimeConstruct).max();
  }

  public OptionalDouble getAverageTimeConstruct() {
    return getRunTimeStream(runTimeConstruct).average();
  }

  public OptionalInt getMinRunTimeOptimize() {
    return getRunTimeStream(runTimeOptimize).min();
  }

  public OptionalInt getMaxRunTimeOptimize() {
    return getRunTimeStream(runTimeOptimize).max();
  }

  public OptionalDouble getAverageTimeOptimize() {
    return getRunTimeStream(runTimeOptimize).average();
  }

  public int getNumberOfRunsConstruct() {
    return runTimeConstruct.size();
  }

  public int getNumberOfSuccessfulRunsConstruct() {
    return toIntExact(getRunTimeStream(runTimeConstruct).count());
  }

  public int getNumberOfRunsOptimize() {
    return runTimeOptimize.size();
  }

  public int getNumberOfSuccessfulRunsOptimize() {
    return toIntExact(getRunTimeStream(runTimeOptimize).count());
  }

  public void addSolutionInstances(Optional<SolutionInstance> solutionInstance) {
    this.solutionInstances.add(solutionInstance);
  }

  public void addOptimizedInstances(Optional<SolutionInstance> optimizizedInstance) {
    this.optimizedInstances.add(optimizizedInstance);
  }

  public OptionalDouble getMinDistanceConstruct() {
    return getDistanceStream(solutionInstances).min();
  }

  public OptionalDouble getMaxDistanceConstruct() {
    return getDistanceStream(solutionInstances).max();
  }

  public OptionalDouble getAverageDistanceConstruct() {
    return getDistanceStream(solutionInstances).average();
  }

  public OptionalDouble getMinDistanceOptimize() {
    return getDistanceStream(optimizedInstances).min();
  }

  public OptionalDouble getMaxDistanceOptimize() {
    return getDistanceStream(optimizedInstances).max();
  }

  public OptionalDouble getAverageDistanceOptimize() {
    return getDistanceStream(optimizedInstances).average();
  }

  private IntStream getRunTimeStream(List<OptionalInt> runTime) {
    return runTime
            .stream()
            .flatMap(o -> o.isPresent() ? Stream.of(o.getAsInt()) : Stream.empty())
            .mapToInt(i -> i);
  }

  private DoubleStream getDistanceStream(List<Optional<SolutionInstance>> solutionInstances) {
    return solutionInstances
            .stream()
            .flatMap(o -> o.isPresent() ? Stream.of(o.get().getDistanceSum()) :Stream.empty())
            .mapToDouble(d -> d);
  }
}
