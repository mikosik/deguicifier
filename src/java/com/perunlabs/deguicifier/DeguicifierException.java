package com.perunlabs.deguicifier;

@SuppressWarnings("serial")
public class DeguicifierException extends RuntimeException {

  public DeguicifierException() {}

  public DeguicifierException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

  public DeguicifierException(String message, Throwable cause) {
    super(message, cause);
  }

  public DeguicifierException(String message) {
    super(message);
  }

  public DeguicifierException(Throwable cause) {
    super(cause);
  }
}
