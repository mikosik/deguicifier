package com.google.inject;

import static org.hamcrest.Matchers.instanceOf;
import static org.testory.Testory.given;
import static org.testory.Testory.thenReturned;
import static org.testory.Testory.when;

import org.junit.Test;

import com.google.inject.spi.ConstructorBinding;
import com.google.inject.spi.InstanceBinding;
import com.google.inject.spi.LinkedKeyBinding;
import com.google.inject.spi.ProviderInstanceBinding;
import com.google.inject.spi.ProviderKeyBinding;

public class BindingTypesTest {
  private Module module;
  private Injector injector;

  @Test
  public void binds_constructor() {
    given(module = new AbstractModule() {
      @Override
      protected void configure() {
        bind(Implementation.class);
      }
    });
    given(injector = Guice.createInjector(module));
    when(injector.getBinding(Implementation.class));
    thenReturned(instanceOf(ConstructorBinding.class));
  }

  @Test
  public void binds_linked_key() {
    given(module = new AbstractModule() {
      @Override
      protected void configure() {
        bind(Interface.class).to(Implementation.class);
      }
    });
    given(injector = Guice.createInjector(module));
    when(injector.getBinding(Interface.class));
    thenReturned(instanceOf(LinkedKeyBinding.class));
  }

  @Test
  public void binds_provider_key() {
    given(module = new AbstractModule() {
      @Override
      protected void configure() {
        bind(Interface.class).toProvider(InterfaceProvider.class);
      }
    });
    given(injector = Guice.createInjector(module));
    when(injector.getBinding(Interface.class));
    thenReturned(instanceOf(ProviderKeyBinding.class));
  }

  @Test
  public void binds_provider_instance() {
    given(module = new AbstractModule() {
      @Override
      protected void configure() {
        bind(Interface.class).toProvider(new InterfaceProvider());
      }
    });
    given(injector = Guice.createInjector(module));
    when(injector.getBinding(Interface.class));
    thenReturned(instanceOf(ProviderInstanceBinding.class));
  }

  @Test
  public void binds_provider_instance_from_method() {
    given(module = new AbstractModule() {
      @Override
      protected void configure() {}

      @Provides
      public Interface provide() {
        return new Implementation();
      }
    });
    given(injector = Guice.createInjector(module));
    when(injector.getBinding(Interface.class));
    thenReturned(instanceOf(ProviderInstanceBinding.class));
  }

  @Test
  public void binds_instance() {
    given(module = new AbstractModule() {
      @Override
      protected void configure() {
        bind(Interface.class).toInstance(new Implementation());
      }
    });
    given(injector = Guice.createInjector(module));
    when(injector.getBinding(Interface.class));
    thenReturned(instanceOf(InstanceBinding.class));
  }

  private static interface Interface {}

  private static class Implementation implements Interface {}

  private static class InterfaceProvider implements Provider<Interface> {
    @Override
    public Interface get() {
      return null;
    }
  }
}
