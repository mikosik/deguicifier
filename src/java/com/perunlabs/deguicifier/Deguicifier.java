package com.perunlabs.deguicifier;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableSet;

import java.io.IOException;
import java.io.StringWriter;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import javax.lang.model.element.Modifier;

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
import com.squareup.javawriter.JavaWriter;

public class Deguicifier {
  public static final String FACTORY_CLASS_NAME = "GeneratedFactory";
  private static final Set<Key<?>> IGNORED_KEYS = immutableSet(Key.get(Injector.class), Key
      .get(Logger.class), Key.get(Stage.class));

  public String deguicify(Module module) {
    Injector injector = Guice.createInjector(module);
    for (Binding<?> binding : injector.getAllBindings().values()) {
      if (!IGNORED_KEYS.contains(binding.getKey())) {
        binding.acceptTargetVisitor(createBindingTargetVisitor());
      }
    }

    try {
      StringWriter stringWriter = new StringWriter();
      JavaWriter javaWriter = new JavaWriter(stringWriter);
      javaWriter.emitPackage("");
      javaWriter.beginType(FACTORY_CLASS_NAME, "class", EnumSet.of(Modifier.PUBLIC));
      javaWriter.endType();
      javaWriter.close();
      return stringWriter.toString();
    } catch (IOException e) {
      throw new Error(e);
    }
  }

  private BindingTargetVisitor<Object, Void> createBindingTargetVisitor() {
    return new BindingTargetVisitor<Object, Void>() {
      @Override
      public Void visit(InstanceBinding<? extends Object> binding) {
        throw new DeguicifierException();
      }

      @Override
      public Void visit(ProviderInstanceBinding<? extends Object> binding) {
        throw new DeguicifierException();
      }

      @Override
      public Void visit(ProviderKeyBinding<? extends Object> binding) {
        throw new DeguicifierException();
      }

      @Override
      public Void visit(LinkedKeyBinding<? extends Object> binding) {
        throw new DeguicifierException();
      }

      @Override
      public Void visit(ExposedBinding<? extends Object> binding) {
        throw new RuntimeException();
      }

      @Override
      public Void visit(UntargettedBinding<? extends Object> binding) {
        throw new RuntimeException();
      }

      @Override
      public Void visit(ConstructorBinding<? extends Object> binding) {
        throw new DeguicifierException();
      }

      @Override
      public Void visit(ConvertedConstantBinding<? extends Object> binding) {
        throw new RuntimeException();
      }

      @Override
      public Void visit(ProviderBinding<? extends Object> binding) {
        throw new RuntimeException();
      }
    };
  }

  @SafeVarargs
  private static <E> Set<E> immutableSet(E... elements) {
    return unmodifiableSet(new HashSet<E>(asList(elements)));
  }
}
