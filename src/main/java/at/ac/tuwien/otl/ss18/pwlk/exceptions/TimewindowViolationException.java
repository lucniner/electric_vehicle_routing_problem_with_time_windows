package at.ac.tuwien.otl.ss18.pwlk.exceptions;

public class TimewindowViolationException extends Exception {
  public TimewindowViolationException() {

  }

  public TimewindowViolationException(String message) {
    super(message);
  }
}
