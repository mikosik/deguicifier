package com.google.inject;

import static com.google.inject.name.Names.named;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.sameInstance;
import static org.testory.Testory.given;
import static org.testory.Testory.thenReturned;
import static org.testory.Testory.thenThrown;
import static org.testory.Testory.when;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.junit.Test;

import com.google.inject.util.Providers;

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

  @Test
  public void provider_of_provider_can_be_injected() throws Exception {
    given(injector = guice.createInjector(new AbstractModule() {
      @Override
      protected void configure() {
        bind(Integer.class).toInstance(Integer.valueOf(3));
      }
    }));
    when(injector.getInstance(Key.get(new TypeLiteral<Provider<Provider<Integer>>>() {})).get()
        .get());
    thenReturned(3);
  }

  @Test
  public void binding_same_scope_instance_to_two_different_annotations() throws Exception {
    given(injector = guice.createInjector(new AbstractModule() {
      @Override
      protected void configure() {
        MyScope scope = new MyScope();
        bindScope(MyScopingAnnotationA.class, scope);
        bindScope(MyScopingAnnotationB.class, scope);
        bind(Key.get(Object.class, named("a"))).to(Object.class).in(MyScopingAnnotationA.class);
        bind(Key.get(Object.class, named("b"))).to(Object.class).in(MyScopingAnnotationB.class);
      }
    }));
    when(injector.getInstance(Key.get(Object.class, named("a"))));
    thenReturned(sameInstance(injector.getInstance(Key.get(Object.class, named("b")))));
  }

  @Target({ ElementType.TYPE, ElementType.METHOD })
  @Retention(RUNTIME)
  @ScopeAnnotation
  public @interface MyScopingAnnotationA {}

  @Target({ ElementType.TYPE, ElementType.METHOD })
  @Retention(RUNTIME)
  @ScopeAnnotation
  public @interface MyScopingAnnotationB {}

  public static class MyScope implements Scope {
    private final Object object = new Object();

    @SuppressWarnings("unchecked")
    @Override
    public <T> Provider<T> scope(Key<T> key, Provider<T> unscoped) {
      return (Provider<T>) Providers.of(object);
    }
  }
}
