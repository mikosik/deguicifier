package com.perunlabs.deguicifier;

import static com.perunlabs.deguicifier.testing.SimpleJavaCompiler.compileProvider;
import static org.testory.Testory.given;
import static org.testory.Testory.thenReturned;
import static org.testory.Testory.when;

import javax.inject.Provider;

import org.junit.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Module;

public class GeneratedClassTest {
  private Deguicifier deguicifier;
  private Module module;
  private Provider<?> provider;
  private final String canonicalName = "my.company.MyClass";
  private final String simpleName = "MyClass";

  @Test
  public void generates_class_with_specified_package() throws Exception {
    given(deguicifier = new Deguicifier());
    given(module = new AbstractModule() {
      @Override
      protected void configure() {
        bind(Object.class);
      }
    });
    given(provider = compileProvider(deguicifier.deguicify(module, Object.class, canonicalName)));
    when(provider.getClass().getCanonicalName());
    thenReturned(canonicalName);
  }

  @Test
  public void generates_class_in_default_package() throws Exception {
    given(deguicifier = new Deguicifier());
    given(module = new AbstractModule() {
      @Override
      protected void configure() {
        bind(Object.class);
      }
    });
    given(provider = compileProvider(deguicifier.deguicify(module, Object.class, simpleName)));
    when(provider.getClass().getCanonicalName());
    thenReturned(simpleName);
  }
}
