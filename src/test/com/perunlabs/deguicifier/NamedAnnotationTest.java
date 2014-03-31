package com.perunlabs.deguicifier;

import static com.google.inject.name.Names.named;
import static com.perunlabs.deguicifier.testing.SimpleJavaCompiler.compileProvider;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.instanceOf;
import static org.testory.Testory.given;
import static org.testory.Testory.givenTest;
import static org.testory.Testory.thenReturned;
import static org.testory.Testory.when;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import org.junit.Before;
import org.junit.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Module;

public class NamedAnnotationTest {
  private Deguicifier deguicifier;
  private Module module;
  private Provider<?> provider;
  private static final String nameA = "nameA", nameB = "nameB";

  @Before
  public void before() {
    givenTest(this);
  }

  @Test
  public void binds_annotated_key() {
    given(deguicifier = new Deguicifier());
    given(module = new AbstractModule() {
      @Override
      protected void configure() {
        bind(Interface.class).annotatedWith(named(nameA)).to(ImplementationA.class);
      }
    });
    given(provider = compileProvider(deguicifier.deguicify(module, MainA.class)));
    when(((MainA) provider.get()).injected);
    thenReturned(instanceOf(ImplementationA.class));
  }

  public static class MainA {
    public final Interface injected;

    @Inject
    public MainA(@Named(nameA) Interface injected) {
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
        bind(Interface.class).annotatedWith(named(nameA)).to(ImplementationA.class);
        bind(Interface.class).annotatedWith(named(nameB)).to(ImplementationB.class);
      }
    });
    given(provider = compileProvider(deguicifier.deguicify(module, MainAB.class)));
    when(((MainAB) provider.get()).injected);
    thenReturned(containsInAnyOrder(instanceOf(ImplementationA.class),
        instanceOf(ImplementationB.class)));
  }

  public static class MainAB {
    public final Set<Interface> injected;

    @Inject
    public MainAB(@Named(nameA) Interface a, @Named(nameB) Interface b) {
      injected = new HashSet<Interface>(Arrays.asList(a, b));
    }
  }

  public static interface Interface {}

  public static class ImplementationA implements Interface {}

  public static class ImplementationB implements Interface {}
}
