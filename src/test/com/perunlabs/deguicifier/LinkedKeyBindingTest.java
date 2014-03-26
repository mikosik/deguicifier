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

public class LinkedKeyBindingTest {
  private Deguicifier deguicifier;
  private Module module;
  private Provider<?> provider;

  @Test
  public void implicit_default_constructor_binding() {
    given(deguicifier = new Deguicifier());
    given(module = new AbstractModule() {
      @Override
      protected void configure() {
        bind(Interface.class).to(Implementation.class);
      }
    });
    given(provider = compileProvider(deguicifier.deguicify(module, Interface.class)));
    when(provider.get());
    thenReturned(instanceOf(Implementation.class));
  }

  public static interface Interface {}

  public static class Implementation implements Interface {}

}
