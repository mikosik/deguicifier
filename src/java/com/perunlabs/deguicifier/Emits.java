package com.perunlabs.deguicifier;

import com.google.inject.TypeLiteral;

public class Emits {
  public static String emitNewInstanceFactory(TypeLiteral<?> typeLiteral) {
    StringBuilder builder = new StringBuilder();
    builder.append(emitGetInstance(typeLiteral, emitNewInstance(typeLiteral)));
    builder.append(emitGetProvider(typeLiteral, "getInstance()"));
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

  public static String emitGetProvider(TypeLiteral<?> type, String instanceStatement) {
    String typeCode = print(type);
    StringBuilder builder = new StringBuilder();
    builder.append("public static Provider<" + typeCode + "> getProvider() {\n");
    builder.append("  return new Provider<" + typeCode + ">() {\n");
    builder.append("    public " + typeCode + " get() {\n");
    builder.append("      return " + instanceStatement + ";\n");
    builder.append("    }\n");
    builder.append("  };\n");
    builder.append("}\n");
    builder.append("\n");
    return builder.toString();
  }

  private static String print(TypeLiteral<?> typeLiteral) {
    return typeLiteral.toString().replace('$', '.');
  }
}
