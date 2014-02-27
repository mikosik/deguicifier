package com.google.inject;

import static org.hamcrest.Matchers.instanceOf;
import static org.testory.Testory.given;
import static org.testory.Testory.thenReturned;
import static org.testory.Testory.thenThrown;
import static org.testory.Testory.when;

import org.junit.Test;

public class GuiceTest {
  Guice guice = new Guice();
  Injector injector;

  @Test
  public void guice_injector_provides_binding_for_class_with_default_constructor() throws Exception {
    given(injector = guice.createInjector());
    when(injector.getInstance(Object.class));
    thenReturned(instanceOf(Object.class));
  }

  @Test
  public void interface_cannot_be_annotated_as_singleton() throws Exception {
    when(guice).createInjector(new ModuleBindingAnnotatedInterface());
    thenThrown(CreationException.class);
  }

  public static class ModuleBindingAnnotatedInterface extends AbstractModule {
    @Override
    protected void configure() {
      bind(AnnotatedInterface.class);
    }
  }

  @Test
  public void binding_wrapper_also_binds_primitive() throws Exception {
    given(injector = guice.createInjector(new AbstractModule() {
      @Override
      protected void configure() {
        bind(Integer.class).toInstance(Integer.valueOf(3));
      }
    }));
    when(injector.getInstance(int.class));
    thenReturned(3);
  }

  @Singleton
  public interface AnnotatedInterface {}

  private static class Guice {
    public Injector createInjector(Module... modules) {
      return com.google.inject.Guice.createInjector(modules);
    }
  }
}
