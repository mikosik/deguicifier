package com.perunlabs.deguicifier;

import static com.perunlabs.deguicifier.testing.SimpleJavaCompiler.compileProvider;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.instanceOf;
import static org.testory.Testory.given;
import static org.testory.Testory.thenReturned;
import static org.testory.Testory.when;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Provider;

import org.junit.Test;

import com.google.inject.AbstractModule;
import com.google.inject.BindingAnnotation;
import com.google.inject.Module;

public class BindingAnnotationTest {
  private Deguicifier deguicifier;
  private Module module;
  private Provider<?> provider;

  @Test
  public void binds_annotated_key() {
    given(deguicifier = new Deguicifier());
    given(module = new AbstractModule() {
      @Override
      protected void configure() {
        bind(Interface.class).annotatedWith(MyAnnotationA.class).to(ImplementationA.class);
      }
    });
    given(provider = compileProvider(deguicifier.deguicify(module, MainA.class, "MyFactory")));
    when(((MainA) provider.get()).injected);
    thenReturned(instanceOf(ImplementationA.class));
  }

  public static class MainA {
    public final Interface injected;

    @Inject
    public MainA(@MyAnnotationA Interface injected) {
      this.injected = injected;
    }
  }

  @SuppressWarnings("unchecked")
  @Test
  public void binds_same_types_with_different_annotations() {
    given(deguicifier = new Deguicifier());
    given(module = new AbstractModule() {
      @Override
      protected void configure() {
        bind(Interface.class).annotatedWith(MyAnnotationA.class).to(ImplementationA.class);
        bind(Interface.class).annotatedWith(MyAnnotationB.class).to(ImplementationB.class);
      }
    });
    given(provider = compileProvider(deguicifier.deguicify(module, MainAB.class, "MyFactory")));
    when(((MainAB) provider.get()).injected);
    thenReturned(containsInAnyOrder(instanceOf(ImplementationA.class),
        instanceOf(ImplementationB.class)));
  }

  public static class MainAB {
    public final Set<Interface> injected;

    @Inject
    public MainAB(@MyAnnotationA Interface a, @MyAnnotationB Interface b) {
      injected = new HashSet<Interface>(Arrays.asList(a, b));
    }
  }

  @Retention(RUNTIME)
  @Target({ ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD })
  @BindingAnnotation
  public @interface MyAnnotationA {}

  @Retention(RUNTIME)
  @Target({ ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD })
  @BindingAnnotation
  public @interface MyAnnotationB {}

  public static interface Interface {}

  public static class ImplementationA implements Interface {}

  public static class ImplementationB implements Interface {}
}
