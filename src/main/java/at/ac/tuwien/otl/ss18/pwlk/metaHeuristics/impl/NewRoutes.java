package at.ac.tuwien.otl.ss18.pwlk.metaHeuristics.impl;

import at.ac.tuwien.otl.ss18.pwlk.valueobjects.Route;

public class NewRoutes {
  private double saving;
  private Route route1;
  private Route route2;

  public NewRoutes(double saving, Route route1, Route route2) {
    this.saving = saving;
    this.route1 = route1;
    this.route2 = route2;
  }

  public double getSaving() {
    return saving;
  }

  public void setSaving(double saving) {
    this.saving = saving;
  }

  public Route getRoute1() {
    return route1;
  }

  public void setRoute1(Route route1) {
    this.route1 = route1;
  }

  public Route getRoute2() {
    return route2;
  }

  public void setRoute2(Route route2) {
    this.route2 = route2;
  }
}

