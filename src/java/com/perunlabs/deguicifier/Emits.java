package com.perunlabs.deguicifier;

import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.google.inject.Binding;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.ConstructorBinding;
import com.google.inject.spi.Dependency;

public class Emits {
  public static String generateGetter(ConstructorBinding<?> binding) {
    return generateGetter(binding, generateConstructorInvocation(binding));
  }

  private static String generateConstructorInvocation(ConstructorBinding<?> binding) {
    StringBuilder builder = new StringBuilder();

    builder.append("new " + canonicalName(binding.getKey().getTypeLiteral()) + "(\n");
    for (Dependency<?> dependency : binding.getDependencies()) {
      TypeLiteral<?> typeLiteral = dependency.getKey().getTypeLiteral();
      builder.append(getterSignature(typeLiteral) + ",");
    }
    if (0 < binding.getDependencies().size()) {
      builder.deleteCharAt(builder.length() - 1);
    }
    builder.append(")");

    return builder.toString();
  }

  private static String generateGetter(Binding<?> binding, String statement) {
    TypeLiteral<?> type = binding.getKey().getTypeLiteral();
    StringBuilder builder = new StringBuilder();
    builder.append("private " + canonicalName(type) + " " + getterSignature(type) + " {\n");
    builder.append("  return " + statement + ";\n");
    builder.append("}\n");
    builder.append("\n");
    return builder.toString();
  }

  private static String canonicalName(TypeLiteral<?> typeLiteral) {
    return typeLiteral.toString().replace('$', '.');
  }

  public static String getterSignature(TypeLiteral<?> type) {
    return "get" + uniqueNameFor(type) + "()";
  }

  private static String uniqueNameFor(TypeLiteral<?> typeLiteral) {
    try {
      byte[] stringBytes = typeLiteral.toString().getBytes(Charset.forName("UTF-8"));
      byte[] hash = MessageDigest.getInstance("SHA-1").digest(stringBytes);
      return new BigInteger(1, hash).toString(16);
    } catch (NoSuchAlgorithmException e) {
      throw new DeguicifierException(e);
    }
  }
}
