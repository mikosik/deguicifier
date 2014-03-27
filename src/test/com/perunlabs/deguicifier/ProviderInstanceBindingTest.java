package com.perunlabs.deguicifier;

import static com.perunlabs.deguicifier.testing.SimpleJavaCompiler.compileProvider;
import static org.testory.Testory.given;
import static org.testory.Testory.givenTest;
import static org.testory.Testory.thenReturned;
import static org.testory.Testory.when;

import javax.inject.Provider;

import org.junit.Before;
import org.junit.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.Provides;

public class ProviderInstanceBindingTest {
  private Deguicifier deguicifier;
  private Module module;
  private Provider<?> provider;
  private static CharSequence string = "string";

  @Before
  public void before() {
    givenTest(this);
    given(deguicifier = new Deguicifier());
  }

  @Test
  public void binds_provides_method() {
    given(module = new MyModule());
    given(provider = compileProvider(deguicifier.deguicify(module, CharSequence.class)));
    when(provider.get());
    thenReturned(string);
  }

  public static class MyModule extends AbstractModule {
    @Override
    protected void configure() {}

    @Provides
    public CharSequence providesCharSequence() {
      return string;
    }
  }

  public static class Implementation {}

  @Test
  public void does_not_bind_to_provider_instance() {

  }
}
