package com.perunlabs.deguicifier;

import java.lang.reflect.Method;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.google.inject.Binding;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.internal.ProviderMethod;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.spi.ConstructorBinding;
import com.google.inject.spi.Dependency;
import com.google.inject.spi.HasDependencies;
import com.google.inject.spi.InstanceBinding;
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
    builder.append(generateArgumentList(binding));
    builder.append(")");
    return builder.toString();
  }

  private static String generateArgumentList(HasDependencies binding) {
    StringBuilder builder = new StringBuilder();
    for (Dependency<?> dependency : binding.getDependencies()) {
      builder.append(getterSignature(dependency.getKey()) + ",");
    }
    if (0 < binding.getDependencies().size()) {
      builder.deleteCharAt(builder.length() - 1);
    }
    return builder.toString();
  }

  public static String generateGetter(LinkedKeyBinding<?> binding) {
    return generateGetter(binding, getterSignature(binding.getLinkedKey()));
  }

  public static String generateGetter(ProviderKeyBinding<?> binding) {
    return generateGetter(binding, getterSignature(binding.getProviderKey()) + ".get()");
  }

  public static String generateGetter(ProviderBinding<?> binding) {
    Key<?> key = binding.getProvidedKey();
    TypeLiteral<?> type = key.getTypeLiteral();
    return generateGetter(binding, generateProvider(type, getterSignature(key)));
  }

  public static String generateGetter(ProviderInstanceBinding<?> binding) {
    if (binding.getProviderInstance() instanceof ProviderMethod<?>) {
      Method method = ((ProviderMethod<?>) binding.getProviderInstance()).getMethod();
      Class<?> declaringClass = method.getDeclaringClass();
      if (declaringClass.isLocalClass()) {
        throw new DeguicifierException();
      }
      try {
        declaringClass.getConstructor();
      } catch (NoSuchMethodException e) {
        throw new DeguicifierException(e);
      }
      String statement =
          "new " + declaringClass.getCanonicalName() + "()." + method.getName() + "("
              + generateArgumentList(binding) + ")";
      return generateGetter(binding, statement);
    }
    if (binding.getProviderInstance() instanceof Multibinder<?>) {
      String statement =
          "(" + canonicalName(binding.getKey().getTypeLiteral())
              + ") new java.util.HashSet(java.util.Arrays.asList(new Object[] {"
              + generateArgumentList(binding) + "}))";
      return "@SuppressWarnings({ \"unchecked\", \"rawtypes\" })\n"
          + generateGetter(binding, statement);
    }
    throw new DeguicifierException();
  }

  private static String generateProvider(TypeLiteral<?> type, String statement) {
    String canonical = canonicalName(type);

    StringBuilder builder = new StringBuilder();
    builder.append("new " + "Provider<" + canonical + ">" + "() {\n");
    builder.append("  public " + canonical + " get() {\n");
    builder.append("    return " + statement + "\n;");
    builder.append("  }\n");
    builder.append("}");
    return builder.toString();
  }

  public static String generateGetter(InstanceBinding<?> binding) {
    Object instance = binding.getInstance();
    if (instance instanceof Boolean) {
      return generateGetter(binding, instance.toString());
    } else if (instance instanceof Character) {
      return generateGetter(binding, "'" + instance.toString() + "'");
    } else if (instance instanceof Byte) {
      return generateGetter(binding, instance.toString());
    } else if (instance instanceof Short) {
      return generateGetter(binding, instance.toString());
    } else if (instance instanceof Integer) {
      return generateGetter(binding, instance.toString());
    } else if (instance instanceof Long) {
      return generateGetter(binding, instance.toString() + "L");
    } else if (instance instanceof Float) {
      return generateGetter(binding, instance.toString() + "f");
    } else if (instance instanceof Double) {
      return generateGetter(binding, instance.toString() + "d");
    } else if (instance instanceof String) {
      return generateGetter(binding, "\"" + escape(instance.toString()) + "\"");
    } else {
      throw new DeguicifierException();
    }
  }

  private static String escape(String string) {
    return string.replace("\\", "\\\\").replace("\"", "\\\"").replace("\t", "\\t").replace("\r",
        "\\r").replace("\n", "\\n").replace("\b", "\\b").replace("\f", "\\f");
  }

  private static String generateGetter(Binding<?> binding, String statement) {
    Key<?> key = binding.getKey();
    TypeLiteral<?> type = key.getTypeLiteral();
    StringBuilder builder = new StringBuilder();
    builder.append("private " + canonicalName(type) + " " + getterSignature(key) + " {\n");
    builder.append("  return " + statement + ";\n");
    builder.append("}\n");
    builder.append("\n");
    return builder.toString();
  }

  public static String getterSignature(Key<?> key) {
    return "get" + uniqueNameFor(key) + "()";
  }

  private static String uniqueNameFor(Key<?> key) {
    try {
      Object annotationObject = key.hasAttributes() ? key.getAnnotation() : key.getAnnotationType();
      String string = canonicalName(key.getTypeLiteral()) + "#" + String.valueOf(annotationObject);
      byte[] stringBytes = string.getBytes(Charset.forName("UTF-8"));
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
