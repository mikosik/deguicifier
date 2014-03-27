package com.perunlabs.deguicifier;

import static com.perunlabs.deguicifier.testing.SimpleJavaCompiler.compileProvider;
import static org.testory.Testory.given;
import static org.testory.Testory.givenTest;
import static org.testory.Testory.thenReturned;
import static org.testory.Testory.thenThrown;
import static org.testory.Testory.when;

import javax.inject.Provider;

import org.junit.Before;
import org.junit.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.util.Providers;
import com.perunlabs.deguicifier.UnsupportedBindingsTest.Implementation;

public class ProviderInstanceBindingTest {
  private Deguicifier deguicifier;
  private Module module;
  private Provider<?> provider;
  private static String string = "string";

  @Before
  public void before() {
    givenTest(this);
    given(deguicifier = new Deguicifier());
  }

  @Test
  public void binds_provides_method_with_no_arguments() {
    given(module = new ProvidesWithoutArgumentsModule());
    given(provider = compileProvider(deguicifier.deguicify(module, CharSequence.class)));
    when(provider.get());
    thenReturned(string);
  }

  public static class ProvidesWithoutArgumentsModule extends AbstractModule {
    @Override
    protected void configure() {}

    @Provides
    public CharSequence providesCharSequence() {
      return string;
    }
  }

  @Test
  public void binds_provides_method_with_arguments() {
    given(module = new ProvidesWithArgumentsModule());
    given(provider = compileProvider(deguicifier.deguicify(module, CharSequence.class)));
    when(provider.get());
    thenReturned(string);
  }

  public static class ProvidesWithArgumentsModule extends AbstractModule {
    @Override
    protected void configure() {}

    @Provides
    public String providesString() {
      return string;
    }

    @Provides
    public CharSequence providesCharSequence(String injectedString) {
      return injectedString;
    }
  }

  @Test
  public void does_not_bind_to_provider_instance() throws Exception {
    given(module = new AbstractModule() {
      @Override
      public void configure() {
        bind(CharSequence.class).toProvider(Providers.of(string));
      }
    });

    when(deguicifier).deguicify(module, Implementation.class);
    thenThrown(DeguicifierException.class);
  }
}
