package com.perunlabs.deguicifier.testing;

import java.lang.reflect.InvocationTargetException;

import com.google.inject.Provider;

public class Reflection {
  public static Object getInstance(Object factory) {
    return invoke(factory, "getInstance");
  }

  public static Provider<?> getProvider(Object factory) {
    return (Provider<?>) invoke(factory, "getProvider");
  }

  private static Object invoke(Object instance, String methodName) {
    try {
      return instance.getClass().getMethod(methodName).invoke(instance);
    } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
  }
}
