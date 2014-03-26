package com.perunlabs.deguicifier;

import static com.perunlabs.deguicifier.testing.SimpleJavaCompiler.compileProvider;
import static org.hamcrest.Matchers.instanceOf;
import static org.testory.Testory.given;
import static org.testory.Testory.thenReturned;
import static org.testory.Testory.when;

import javax.inject.Provider;

import org.junit.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Module;

public class ProviderKeyBindingTest {
  private Deguicifier deguicifier;
  private Module module;
  private Provider<?> provider;

  @Test
  public void implicit_default_constructor_binding() {
    given(deguicifier = new Deguicifier());
    given(module = new AbstractModule() {
      @Override
      protected void configure() {
        bind(Interface.class).toProvider(InterfaceProvider.class);
      }
    });
    given(provider = compileProvider(deguicifier.deguicify(module, Interface.class)));
    when(provider.get());
    thenReturned(instanceOf(Interface.class));
  }

  public static interface Interface {}

  public static class InterfaceProvider implements Provider<Interface> {

    @Override
    public Interface get() {
      return new Interface() {};
    }
  }
}
