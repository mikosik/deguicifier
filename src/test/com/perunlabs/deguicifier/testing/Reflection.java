package com.perunlabs.deguicifier.testing;

import java.lang.reflect.InvocationTargetException;

public class Reflection {
  public static Object getInstance(Object factory) {
    return invoke(factory, "getInstance");
  }

  private static Object invoke(Object instance, String methodName) {
    try {
      return instance.getClass().getMethod(methodName).invoke(instance);
    } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
  }
}
