package com.perunlabs.deguicifier;

import static org.testory.Testory.given;
import static org.testory.Testory.thenThrown;
import static org.testory.Testory.when;

import javax.inject.Provider;

import org.junit.Before;
import org.junit.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.util.Providers;

public class UnsupportedBindingsTest {
  private Deguicifier deguicifier;
  private Module module;

  @Before
  public void before() {
    given(deguicifier = new Deguicifier());
  }

  @Test
  public void instance_binding() throws Exception {
    given(module = new AbstractModule() {
      @Override
      public void configure() {
        bind(Implementation.class).toInstance(new Implementation());
      }
    });

    when(deguicifier).deguicify(module);
    thenThrown(DeguicifierException.class);
  }

  @Test
  public void constructor_binding() throws Exception {
    given(module = new AbstractModule() {
      @Override
      public void configure() {
        bind(Implementation.class);
      }
    });

    when(deguicifier).deguicify(module);
    thenThrown(DeguicifierException.class);
  }

  @Test
  public void linked_binding() throws Exception {
    given(module = new AbstractModule() {
      @Override
      public void configure() {
        bind(Interface.class).to(Implementation.class);
      }
    });

    when(deguicifier).deguicify(module);
    thenThrown(DeguicifierException.class);
  }

  @Test
  public void provides_method_binding() throws Exception {
    given(module = new AbstractModule() {
      @Override
      public void configure() {}

      @Provides
      public Implementation provideImplementation() {
        return new Implementation();
      }
    });

    when(deguicifier).deguicify(module);
    thenThrown(DeguicifierException.class);
  }

  @Test
  public void provider_instance_binding() throws Exception {
    given(module = new AbstractModule() {
      @Override
      public void configure() {
        bind(Implementation.class).toProvider(Providers.of(new Implementation()));
      }
    });

    when(deguicifier).deguicify(module);
    thenThrown(DeguicifierException.class);
  }

  @Test
  public void provider_key_binding() throws Exception {
    given(module = new AbstractModule() {
      @Override
      public void configure() {
        bind(Implementation.class).toProvider(ImplementationProvider.class);
      }
    });

    when(deguicifier).deguicify(module);
    thenThrown(DeguicifierException.class);
  }

  public static interface Interface {}

  public static class Implementation implements Interface {}

  public static class ImplementationProvider implements Provider<Implementation> {
    @Override
    public Implementation get() {
      return new Implementation();
    }
  }
}
