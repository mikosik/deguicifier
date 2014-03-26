package com.perunlabs.deguicifier;

import com.google.inject.TypeLiteral;

public class Emits {
  public static String emitNewInstanceFactory(TypeLiteral<?> typeLiteral) {
    StringBuilder builder = new StringBuilder();
    builder.append(emitGetInstance(typeLiteral, emitNewInstance(typeLiteral)));
    return builder.toString();
  }

  private static String emitNewInstance(TypeLiteral<?> typeLiteral) {
    return "new " + print(typeLiteral) + "()";
  }

  public static String emitGetInstance(TypeLiteral<?> type, String instanceStatement) {
    String typeCode = print(type);
    StringBuilder builder = new StringBuilder();
    builder.append("public static " + typeCode + " getInstance() {\n");
    builder.append("  return " + instanceStatement + ";\n");
    builder.append("}\n");
    builder.append("\n");
    return builder.toString();
  }

  private static String print(TypeLiteral<?> typeLiteral) {
    return typeLiteral.toString().replace('$', '.');
  }
}
