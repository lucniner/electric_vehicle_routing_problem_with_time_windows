package at.ac.tuwien.otl.ss18.pwlk.exceptions;

public class BatteryViolationException extends Exception {
  public BatteryViolationException() {

  }

  public BatteryViolationException(String message) {
    super(message);
  }
}
