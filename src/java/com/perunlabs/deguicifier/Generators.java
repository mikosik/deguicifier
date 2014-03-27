package com.perunlabs.deguicifier;

import java.lang.reflect.Method;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.google.inject.Binding;
import com.google.inject.TypeLiteral;
import com.google.inject.internal.ProviderMethod;
import com.google.inject.spi.ConstructorBinding;
import com.google.inject.spi.Dependency;
import com.google.inject.spi.LinkedKeyBinding;
import com.google.inject.spi.ProviderBinding;
import com.google.inject.spi.ProviderInstanceBinding;
import com.google.inject.spi.ProviderKeyBinding;

public class Generators {
  public static String generateGetter(ConstructorBinding<?> binding) {
    String statement = generateConstructorInvocation(binding);
    return generateGetter(binding, statement);
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

  public static String generateGetter(LinkedKeyBinding<?> binding) {
    String statement = getterSignature(binding.getLinkedKey().getTypeLiteral());
    return generateGetter(binding, statement);
  }

  public static String generateGetter(ProviderKeyBinding<?> binding) {
    String statement = getterSignature(binding.getProviderKey().getTypeLiteral()) + ".get()";
    return generateGetter(binding, statement);
  }

  public static String generateGetter(ProviderBinding<?> binding) {
    TypeLiteral<?> provider = binding.getKey().getTypeLiteral();
    TypeLiteral<?> provided = binding.getProvidedKey().getTypeLiteral();

    StringBuilder builder = new StringBuilder();
    builder.append("new " + canonicalName(provider) + "() {\n");
    builder.append("  public " + canonicalName(provided) + " get() {\n");
    builder.append("    return " + getterSignature(provided) + "\n;");
    builder.append("  }\n");
    builder.append("}");
    String statement = builder.toString();

    return generateGetter(binding, statement);
  }

  public static String generateGetter(ProviderInstanceBinding<?> binding) {
    if (binding.getProviderInstance() instanceof ProviderMethod<?>) {
      Method method = ((ProviderMethod<?>) binding.getProviderInstance()).getMethod();
      String statement =
          "new " + method.getDeclaringClass().getCanonicalName() + "()." + method.getName() + "()";
      return generateGetter(binding, statement);
    }
    throw new DeguicifierException();
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

  public static String getterSignature(TypeLiteral<?> type) {
    return "get" + uniqueNameFor(type) + "()";
  }

  private static String uniqueNameFor(TypeLiteral<?> typeLiteral) {
    try {
      byte[] stringBytes = canonicalName(typeLiteral).getBytes(Charset.forName("UTF-8"));
      byte[] hash = MessageDigest.getInstance("SHA-1").digest(stringBytes);
      return new BigInteger(1, hash).toString(16);
    } catch (NoSuchAlgorithmException e) {
      throw new DeguicifierException(e);
    }
  }

  private static String canonicalName(TypeLiteral<?> typeLiteral) {
    String result = typeLiteral.toString().replace('$', '.');
    if (typeLiteral.getRawType().equals(com.google.inject.Provider.class)) {
      String googleProvider = com.google.inject.Provider.class.getCanonicalName();
      String javaxProvider = javax.inject.Provider.class.getCanonicalName();
      return javaxProvider + result.substring(googleProvider.length());
    } else {
      return result;
    }
  }
}
