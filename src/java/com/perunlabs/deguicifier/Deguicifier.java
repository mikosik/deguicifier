package com.perunlabs.deguicifier;

import static com.perunlabs.deguicifier.Generators.providerField;
import static com.perunlabs.deguicifier.Generators.scopeField;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableSet;

import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.inject.Provider;

import com.google.inject.AbstractModule;
import com.google.inject.Binding;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.Scope;
import com.google.inject.Stage;
import com.google.inject.spi.DefaultBindingScopingVisitor;

public class Deguicifier {
  private static final Set<Key<?>> IGNORED_KEYS = immutableSet(Key.get(Injector.class), Key
      .get(Logger.class), Key.get(Stage.class));

  public String deguicify(Module module, final Class<?> mainClass,
      String generatedClassCanonicalName) {
    Injector injector = Guice.createInjector(module, new AbstractModule() {
      @Override
      protected void configure() {
        requireBinding(mainClass);
      }
    });

    StringBuilder builder = new StringBuilder();

    String packageName = packageName(generatedClassCanonicalName);
    if (!packageName.isEmpty()) {
      builder.append("package " + packageName + ";\n");
    }
    builder.append("import " + Provider.class.getName() + ";\n");
    builder.append("@SuppressWarnings(\"all\")\n");
    builder.append("public class " + className(generatedClassCanonicalName)
        + " implements javax.inject.Provider<" + mainClass.getCanonicalName() + "> {\n");

    builder.append(Generators.mainGetter(mainClass));

    final Map<Scope, Void> scopes = new IdentityHashMap<Scope, Void>();
    for (Binding<?> entry : injector.getAllBindings().values()) {
      entry.acceptScopingVisitor(new DefaultBindingScopingVisitor<Void>() {
        @Override
        public Void visitScope(Scope scope) {
          scopes.put(scope, null);
          return null;
        }
      });
    }

    for (Scope scope : scopes.keySet()) {
      builder.append(scopeField(scope));
    }

    for (Binding<?> binding : injector.getAllBindings().values()) {
      if (!IGNORED_KEYS.contains(binding.getKey())) {
        builder.append(providerField(binding));
      }
    }

    builder.append("}\n");
    return builder.toString();
  }

  private String packageName(String classCanonicalName) {
    int index = classCanonicalName.lastIndexOf('.');
    if (index == -1) {
      return "";
    } else {
      return classCanonicalName.substring(0, index);
    }
  }

  private String className(String classCanonicalName) {
    int index = classCanonicalName.lastIndexOf('.');
    if (index == -1) {
      return classCanonicalName;
    } else {
      return classCanonicalName.substring(index + 1);
    }
  }

  @SafeVarargs
  private static <E> Set<E> immutableSet(E... elements) {
    return unmodifiableSet(new HashSet<E>(asList(elements)));
  }
}
