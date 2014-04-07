package com.perunlabs.deguicifier;

import com.google.inject.Module;

public class Main {
  public static void main(String... args) throws Exception {
    if (args.length != 3) {
      throw new RuntimeException(
          "Two arguments needed: <module class name> <main class name> <class to generate>");
    }
    Module module = (Module) Class.forName(args[0]).newInstance();
    Class<?> mainClass = Class.forName(args[1]);
    System.out.print(new Deguicifier().deguicify(module, mainClass, "MyFactory"));
  }
}
