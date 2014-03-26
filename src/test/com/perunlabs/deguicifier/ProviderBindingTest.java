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

public class ProviderBindingTest {
  private Deguicifier deguicifier;
  private Module module;
  private Provider<?> provider;

  @Test
  public void implicit_default_constructor_binding() {
    given(deguicifier = new Deguicifier());
    given(module = new AbstractModule() {
      @Override
      protected void configure() {
        bind(Injectable.class);
      }
    });
    given(provider = compileProvider(deguicifier.deguicify(module, Injectable.class)));
    when(((Injectable) provider.get()).injectedProvider.get());
    thenReturned(instanceOf(Implementation.class));
  }

  public static class Implementation {}

  public static class Injectable {
    public final Provider<Implementation> injectedProvider;

    @Inject
    public Injectable(Provider<Implementation> provider) {
      this.injectedProvider = provider;
    }
  }
}
