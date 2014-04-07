package com.perunlabs.deguicifier;

import static com.perunlabs.deguicifier.testing.SimpleJavaCompiler.compileProvider;
import static org.hamcrest.Matchers.instanceOf;
import static org.testory.Testory.given;
import static org.testory.Testory.thenReturned;
import static org.testory.Testory.thenThrown;
import static org.testory.Testory.when;

import javax.inject.Inject;
import javax.inject.Provider;

import org.junit.Test;
import org.testory.Closure;

import com.google.inject.AbstractModule;
import com.google.inject.Module;

public class ProviderBindingTest {
  private Deguicifier deguicifier;
  private Module module;
  private Provider<?> provider;

  @Test
  public void injects_provider() {
    given(deguicifier = new Deguicifier());
    given(module = new AbstractModule() {
      @Override
      protected void configure() {
        bind(Injectable.class);
      }
    });
    given(provider = compileProvider(deguicifier.deguicify(module, Injectable.class, "MyFactory")));
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

  @Test
  public void does_not_inject_google_provider() {
    given(deguicifier = new Deguicifier());
    given(module = new AbstractModule() {
      @Override
      protected void configure() {
        bind(GoogleInjectable.class);
      }
    });
    when($compileProvider(deguicifier.deguicify(module, GoogleInjectable.class, "MyFactory")));
    thenThrown(RuntimeException.class);
  }

  private static Closure $compileProvider(final String source) {
    return new Closure() {
      @Override
      public Object invoke() throws Throwable {
        return compileProvider(source);
      }
    };
  }

  public static class GoogleInjectable {
    @Inject
    public GoogleInjectable(com.google.inject.Provider<Implementation> provider) {}
  }
}
