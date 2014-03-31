package com.perunlabs.deguicifier;

import static com.perunlabs.deguicifier.testing.SimpleJavaCompiler.compileProvider;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.sameInstance;
import static org.testory.Testory.given;
import static org.testory.Testory.thenReturned;
import static org.testory.Testory.when;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.junit.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Module;

public class SingletonTest {
  private Deguicifier deguicifier;
  private Module module;
  private Provider<?> provider;

  @Test
  public void provides_same_instance_for_singleton_scope() {
    given(deguicifier = new Deguicifier());
    given(module = new AbstractModule() {
      @Override
      protected void configure() {
        bind(Object.class).in(Singleton.class);
      }
    });
    given(provider = compileProvider(deguicifier.deguicify(module, Object.class)));
    when(provider.get());
    thenReturned(sameInstance(provider.get()));
  }

  @Test
  public void different_singletons_do_not_share_injected_objects() {
    given(deguicifier = new Deguicifier());
    given(module = new AbstractModule() {
      @Override
      protected void configure() {
        bind(InterfaceA.class).to(Implementation.class).in(Singleton.class);
        bind(InterfaceB.class).to(Implementation.class).in(Singleton.class);
      }
    });
    given(provider = compileProvider(deguicifier.deguicify(module, MainClass.class)));
    when(((MainClass) provider.get()).injected);
    thenReturned(hasSize(2));
  }

  @Test
  public void different_singletons_share_injected_singleton() {
    given(deguicifier = new Deguicifier());
    given(module = new AbstractModule() {
      @Override
      protected void configure() {
        bind(InterfaceA.class).to(Implementation.class);
        bind(InterfaceB.class).to(Implementation.class);
        bind(Implementation.class).in(Singleton.class);
      }
    });
    given(provider = compileProvider(deguicifier.deguicify(module, MainClass.class)));
    when(((MainClass) provider.get()).injected);
    thenReturned(hasSize(1));
  }

  public static class MainClass {
    public final Set<Object> injected;

    @Inject
    public MainClass(InterfaceA a, InterfaceB b) {
      injected = new HashSet<Object>(Arrays.asList(a, b));
    }
  }

  public interface InterfaceA {}

  public interface InterfaceB {}

  public static class Implementation implements InterfaceA, InterfaceB {}
}
