package com.perunlabs.deguicifier;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.google.inject.Binding;
import com.google.inject.Key;
import com.google.inject.Scope;
import com.google.inject.Scopes;
import com.google.inject.TypeLiteral;
import com.google.inject.internal.ProviderMethod;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.spi.BindingScopingVisitor;
import com.google.inject.spi.ConstructorBinding;
import com.google.inject.spi.Dependency;
import com.google.inject.spi.HasDependencies;
import com.google.inject.spi.InstanceBinding;
import com.google.inject.spi.LinkedKeyBinding;
import com.google.inject.spi.ProviderBinding;
import com.google.inject.spi.ProviderInstanceBinding;
import com.google.inject.spi.ProviderKeyBinding;

public class Generators {
  public static String mainGetter(final Class<?> mainClass) {
    StringBuilder builder = new StringBuilder();
    builder.append("public " + mainClass.getCanonicalName() + " get" + "() {\n");
    builder.append("  return " + " " + providerCall(Key.get(mainClass)) + ";\n");
    builder.append("}\n");
    builder.append("\n");
    return builder.toString();
  }

  public static String scopeField(Scope scope) {
    return "private final com.google.inject.Scope " + scopeFieldName(scope) + " = "
        + scopeInstance(scope) + ";\n";
  }

  private static String scopeInstance(Scope scope) {
    if (scope == Scopes.SINGLETON) {
      return Scopes.class.getCanonicalName() + ".SINGLETON";
    } else {
      return "new " + scope.getClass().getCanonicalName() + "()";
    }
  }

  public static String providerField(Binding<?> binding) {
    return providerField(binding, instantiation(binding));
  }

  private static String instantiation(Binding<?> binding) {
    if (binding instanceof InstanceBinding<?>) {
      return instanceLiteral((InstanceBinding<?>) binding);
    } else if (binding instanceof ProviderKeyBinding<?>) {
      return providerCall(((ProviderKeyBinding<?>) binding).getProviderKey()) + ".get()";
    } else if (binding instanceof LinkedKeyBinding<?>) {
      return providerCall(((LinkedKeyBinding<?>) binding).getLinkedKey());
    } else if (binding instanceof ConstructorBinding<?>) {
      return constructorCall((ConstructorBinding<?>) binding);
    } else if (binding instanceof ProviderBinding<?>) {
      return provider((ProviderBinding<?>) binding);
    } else if (binding instanceof ProviderInstanceBinding<?>) {
      return instantiation((ProviderInstanceBinding<?>) binding);
    } else {
      throw new DeguicifierException();
    }
  }

  private static String instantiation(ProviderInstanceBinding<?> binding) {
    if (binding.getProviderInstance() instanceof ProviderMethod<?>) {
      ProviderMethod<?> provider = (ProviderMethod<?>) binding.getProviderInstance();
      return providesMethodCall(binding, provider);
    } else if (binding.getProviderInstance() instanceof Multibinder<?>) {
      return createSetCall(binding);
    } else {
      throw new DeguicifierException();
    }
  }

  private static String providerField(Binding<?> binding, String statement) {
    Key<?> key = binding.getKey();
    TypeLiteral<?> type = key.getTypeLiteral();
    return "private final Provider<" + canonicalName(type) + "> " + providerFieldName(key) + " = "
        + scoped(binding, provider(type, statement)) + ";\n";
  }

  private static String constructorCall(ConstructorBinding<?> binding) {
    StringBuilder builder = new StringBuilder();
    builder.append("new " + canonicalName(binding.getKey().getTypeLiteral()) + "(\n");
    builder.append(argumentList(binding));
    builder.append(")");
    return builder.toString();
  }

  private static String instanceLiteral(InstanceBinding<?> binding) {
    Object instance = binding.getInstance();
    if (instance instanceof Boolean) {
      return instance.toString();
    } else if (instance instanceof Character) {
      return "'" + instance.toString() + "'";
    } else if (instance instanceof Byte) {
      return instance.toString();
    } else if (instance instanceof Short) {
      return instance.toString();
    } else if (instance instanceof Integer) {
      return instance.toString();
    } else if (instance instanceof Long) {
      return instance.toString() + "L";
    } else if (instance instanceof Float) {
      return instance.toString() + "f";
    } else if (instance instanceof Double) {
      return instance.toString() + "d";
    } else if (instance instanceof String) {
      return "\"" + escape(instance.toString()) + "\"";
    } else {
      throw new DeguicifierException();
    }
  }

  private static String provider(ProviderBinding<?> binding) {
    Key<?> key = binding.getProvidedKey();
    TypeLiteral<?> type = key.getTypeLiteral();
    return provider(type, providerCall(key));
  }

  private static String createSetCall(ProviderInstanceBinding<?> binding) {
    return "(" + canonicalName(binding.getKey().getTypeLiteral())
        + ") new java.util.HashSet(java.util.Arrays.asList(new Object[] {" + argumentList(binding)
        + "}))";
  }

  private static String providesMethodCall(ProviderInstanceBinding<?> binding,
      ProviderMethod<?> provider) {
    Method providesMethod = provider.getMethod();
    Class<?> moduleClass = providesMethod.getDeclaringClass();
    if (moduleClass.isLocalClass()) {
      throw new DeguicifierException();
    }
    try {
      moduleClass.getConstructor();
    } catch (NoSuchMethodException e) {
      throw new DeguicifierException(e);
    }
    String statement =
        "new " + moduleClass.getCanonicalName() + "()." + providesMethod.getName() + "("
            + argumentList(binding) + ")";
    return statement;
  }

  private static String provider(TypeLiteral<?> type, String statement) {
    String canonical = canonicalName(type);

    StringBuilder builder = new StringBuilder();
    builder.append("new " + "Provider<" + canonical + ">" + "() {\n");
    builder.append("  public " + canonical + " get() {\n");
    builder.append("    return " + statement + ";\n");
    builder.append("  }\n");
    builder.append("}");
    return builder.toString();
  }

  private static String argumentList(HasDependencies binding) {
    StringBuilder builder = new StringBuilder();
    for (Dependency<?> dependency : binding.getDependencies()) {
      builder.append(providerCall(dependency.getKey()) + ",");
    }
    if (0 < binding.getDependencies().size()) {
      builder.deleteCharAt(builder.length() - 1);
    }
    return builder.toString();
  }

  private static String escape(String string) {
    return string.replace("\\", "\\\\").replace("\"", "\\\"").replace("\t", "\\t").replace("\r",
        "\\r").replace("\n", "\\n").replace("\b", "\\b").replace("\f", "\\f");
  }

  private static String scoped(final Binding<?> binding, final String statement) {
    return binding.acceptScopingVisitor(new BindingScopingVisitor<String>() {
      @Override
      public String visitEagerSingleton() {
        if (binding instanceof InstanceBinding<?>) {
          return statement;
        }
        throw new DeguicifierException();
      }

      @Override
      public String visitScope(Scope scope) {
        return scopeFieldName(scope) + ".scope(null, " + guicify(statement) + ")";
      }

      @Override
      public String visitScopeAnnotation(Class<? extends Annotation> scopeAnnotation) {
        throw new DeguicifierException();
      }

      @Override
      public String visitNoScoping() {
        return statement;
      }
    });
  }

  private static String guicify(String statement) {
    return "com.google.inject.util.Providers.guicify(" + statement + ")";
  }

  private static String scopeFieldName(Scope scope) {
    return "scope_" + System.identityHashCode(scope);
  }

  private static String providerCall(Key<?> key) {
    return providerFieldName(key) + ".get()";
  }

  private static String providerFieldName(Key<?> key) {
    return "provider_" + uniqueNameFor(key);
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
