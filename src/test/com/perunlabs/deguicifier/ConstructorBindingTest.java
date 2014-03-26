package com.perunlabs.deguicifier;

import static com.perunlabs.deguicifier.testing.SimpleJavaCompiler.compileProvider;
import static org.hamcrest.Matchers.instanceOf;
import static org.testory.Testory.given;
import static org.testory.Testory.thenReturned;
import static org.testory.Testory.when;

import javax.inject.Inject;
import javax.inject.Provider;

import org.junit.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Module;

public class ConstructorBindingTest {
  private Deguicifier deguicifier;
  private Module module;
  private Provider<?> provider;

  @Test
  public void implicit_default_constructor_binding() {
    given(deguicifier = new Deguicifier());
    given(module = new AbstractModule() {
      @Override
      protected void configure() {
        bind(Implementation.class);
      }
    });
    given(provider = compileProvider(deguicifier.deguicify(module, Implementation.class)));
    when(provider.get());
    thenReturned(instanceOf(Implementation.class));
  }

  public static class Implementation {}

  @Test
  public void explicit_default_constructor_binding() {
    given(deguicifier = new Deguicifier());
    given(module = new AbstractModule() {
      @Override
      protected void configure() {
        bind(ImplementationWithInject.class);
      }
    });
    given(provider = compileProvider(deguicifier.deguicify(module, ImplementationWithInject.class)));
    when(provider.get());
    thenReturned(instanceOf(ImplementationWithInject.class));
  }

  public static class ImplementationWithInject {
    @Inject
    public ImplementationWithInject() {}
  }

  @Test
  public void injected_constructor_binding() {
    given(deguicifier = new Deguicifier());
    given(module = new AbstractModule() {
      @Override
      protected void configure() {
        bind(Value.class);
        bind(ImplementationWithInjectedValue.class);
      }
    });
    given(provider =
        compileProvider(deguicifier.deguicify(module, ImplementationWithInjectedValue.class)));
    when(provider.get());
    thenReturned(instanceOf(ImplementationWithInjectedValue.class));
  }

  public static class ImplementationWithInjectedValue {
    @Inject
    public ImplementationWithInjectedValue(Value value) {}
  }

  public static class Value {}
}
