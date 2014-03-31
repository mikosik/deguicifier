package com.perunlabs.deguicifier;

import static com.google.inject.multibindings.Multibinder.newSetBinder;
import static com.perunlabs.deguicifier.testing.SimpleJavaCompiler.compileProvider;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.instanceOf;
import static org.testory.Testory.given;
import static org.testory.Testory.thenReturned;
import static org.testory.Testory.when;

import java.util.Set;

import javax.inject.Inject;
import javax.inject.Provider;

import org.junit.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.multibindings.Multibinder;

public class MultibindingTest {
  private Deguicifier deguicifier;
  private Module module;
  private Provider<?> provider;

  @Test
  public void binds_empty_set() throws Exception {
    given(deguicifier = new Deguicifier());
    given(module = new AbstractModule() {
      @Override
      protected void configure() {
        newSetBinder(binder(), Interface.class);
      }
    });
    given(provider = compileProvider(deguicifier.deguicify(module, MainClass.class)));
    when(((MainClass) provider.get()).injectedSet);
    thenReturned(empty());
  }

  @Test
  public void binds_set_with_one_element() throws Exception {
    given(deguicifier = new Deguicifier());
    given(module = new AbstractModule() {
      @Override
      protected void configure() {
        Multibinder<Interface> multibinder = newSetBinder(binder(), Interface.class);
        multibinder.addBinding().to(ImplementationA.class);
      }
    });
    given(provider = compileProvider(deguicifier.deguicify(module, MainClass.class)));
    when(((MainClass) provider.get()).injectedSet);
    thenReturned(contains(instanceOf(ImplementationA.class)));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void binds_set_with_two_elements() throws Exception {
    given(deguicifier = new Deguicifier());
    given(module = new AbstractModule() {
      @Override
      protected void configure() {
        Multibinder<Interface> multibinder = newSetBinder(binder(), Interface.class);
        multibinder.addBinding().to(ImplementationA.class);
        multibinder.addBinding().to(ImplementationB.class);
      }
    });
    String s = deguicifier.deguicify(module, MainClass.class);
    given(provider = compileProvider(s));
    when(((MainClass) provider.get()).injectedSet);
    thenReturned(containsInAnyOrder(instanceOf(ImplementationA.class),
        instanceOf(ImplementationB.class)));
  }

  public static class MainClass {
    public final Set<Interface> injectedSet;

    @Inject
    public MainClass(Set<Interface> set) {
      this.injectedSet = set;
    }
  }

  public static interface Interface {}

  public static class ImplementationA implements Interface {}

  public static class ImplementationB implements Interface {}
}
