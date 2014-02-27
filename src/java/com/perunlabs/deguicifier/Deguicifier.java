package com.perunlabs.deguicifier;

import com.google.inject.Module;

public class Deguicifier {
  public static final String FACTORY_CLASS_NAME = "GeneratedFactory";

  public String deguicify(Module module) {
    return "public class " + FACTORY_CLASS_NAME + " {}";
  }
}
