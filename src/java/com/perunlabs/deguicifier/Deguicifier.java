package com.perunlabs.deguicifier;

import static com.perunlabs.deguicifier.Generators.generateGetter;
import static com.perunlabs.deguicifier.Generators.getterSignature;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableSet;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import javax.inject.Provider;

import com.google.inject.AbstractModule;
import com.google.inject.Binding;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.Stage;
import com.google.inject.spi.BindingTargetVisitor;
import com.google.inject.spi.ConstructorBinding;
import com.google.inject.spi.ConvertedConstantBinding;
import com.google.inject.spi.ExposedBinding;
import com.google.inject.spi.InstanceBinding;
import com.google.inject.spi.LinkedKeyBinding;
import com.google.inject.spi.ProviderBinding;
import com.google.inject.spi.ProviderInstanceBinding;
import com.google.inject.spi.ProviderKeyBinding;
import com.google.inject.spi.UntargettedBinding;

public class Deguicifier {
  public static final String FACTORY_CLASS_NAME = "GeneratedFactory";
  private static final Set<Key<?>> IGNORED_KEYS = immutableSet(Key.get(Injector.class), Key
      .get(Logger.class), Key.get(Stage.class));

  public String deguicify(Module module, final Class<?> mainClass) {
    Injector injector = Guice.createInjector(module, new AbstractModule() {
      @Override
      protected void configure() {
        requireBinding(mainClass);
      }
    });

    StringBuilder builder = new StringBuilder();

    builder.append("import " + Provider.class.getName() + ";\n");
    builder.append("public class " + FACTORY_CLASS_NAME + " implements javax.inject.Provider<"
        + mainClass.getCanonicalName() + "> {\n");

    builder.append("public " + mainClass.getCanonicalName() + " get" + "() {\n");
    builder.append("  return " + " " + getterSignature(Key.get(mainClass)) + ";\n");
    builder.append("}\n");
    builder.append("\n");

    for (Binding<?> binding : injector.getAllBindings().values()) {
      if (!IGNORED_KEYS.contains(binding.getKey())) {
        builder.append(emit(binding));
      }
    }

    builder.append("}\n");
    return builder.toString();
  }

  private static String emit(Binding<?> binding) {
    return binding.acceptTargetVisitor(createBindingTargetVisitor());
  }

  private static BindingTargetVisitor<Object, String> createBindingTargetVisitor() {
    return new BindingTargetVisitor<Object, String>() {
      @Override
      public String visit(InstanceBinding<?> binding) {
        return generateGetter(binding);
      }

      @Override
      public String visit(ProviderInstanceBinding<?> binding) {
        return generateGetter(binding);
      }

      @Override
      public String visit(ProviderKeyBinding<?> binding) {
        return generateGetter(binding);
      }

      @Override
      public String visit(LinkedKeyBinding<?> binding) {
        return generateGetter(binding);
      }

      @Override
      public String visit(ExposedBinding<?> binding) {
        throw new RuntimeException();
      }

      @Override
      public String visit(UntargettedBinding<?> binding) {
        throw new RuntimeException();
      }

      @Override
      public String visit(ConstructorBinding<?> binding) {
        return generateGetter(binding);
      }

      @Override
      public String visit(ConvertedConstantBinding<?> binding) {
        throw new RuntimeException();
      }

      @Override
      public String visit(ProviderBinding<?> binding) {
        return generateGetter(binding);
      }
    };
  }

  @SafeVarargs
  private static <E> Set<E> immutableSet(E... elements) {
    return unmodifiableSet(new HashSet<E>(asList(elements)));
  }
}
