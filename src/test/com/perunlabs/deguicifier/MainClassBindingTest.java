package com.perunlabs.deguicifier;

import static com.perunlabs.deguicifier.testing.SimpleJavaCompiler.compileProvider;
import static org.testory.Testory.given;
import static org.testory.Testory.thenReturned;
import static org.testory.Testory.when;

import javax.inject.Provider;

import org.junit.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Module;

public class MainClassBindingTest {
  private Deguicifier deguicifier;
  private Module module;
  private Provider<?> provider;

  @Test
  public void generic_type_binding() {
    given(deguicifier = new Deguicifier());
    given(module = new AbstractModule() {
      @Override
      protected void configure() {}
    });
    given(provider = compileProvider(deguicifier.deguicify(module, Object.class, "MyFactory")));
    when(provider.get());
    thenReturned();
  }
}
