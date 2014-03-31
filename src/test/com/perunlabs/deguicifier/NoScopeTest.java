package com.perunlabs.deguicifier;

import static com.perunlabs.deguicifier.testing.SimpleJavaCompiler.compileProvider;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;
import static org.testory.Testory.given;
import static org.testory.Testory.thenReturned;
import static org.testory.Testory.when;

import javax.inject.Provider;

import org.junit.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Module;

public class NoScopeTest {
  private Deguicifier deguicifier;
  private Module module;
  private Provider<?> provider;

  @Test
  public void provides_different_instance_for_unscoped_binding() {
    given(deguicifier = new Deguicifier());
    given(module = new AbstractModule() {
      @Override
      protected void configure() {
        bind(Implementation.class);
      }
    });
    given(provider = compileProvider(deguicifier.deguicify(module, Implementation.class)));
    when(provider.get());
    thenReturned(not(sameInstance(provider.get())));
  }

  public static class Implementation {}
}
