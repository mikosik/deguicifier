package com.perunlabs.deguicifier;

import static com.google.inject.name.Names.named;
import static com.perunlabs.deguicifier.testing.SimpleJavaCompiler.compileProvider;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;
import static org.testory.Testory.given;
import static org.testory.Testory.givenTest;
import static org.testory.Testory.thenReturned;
import static org.testory.Testory.when;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import org.junit.Before;
import org.junit.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.Scope;
import com.google.inject.ScopeAnnotation;

public class ScopeTest {
  private Deguicifier deguicifier;
  private Module module;
  private Provider<?> provider;
  private Provider<ScopableObject> scopedProvider;
  private MainAB main;
  private static final String nameA = "nameA", nameB = "nameB";

  @Before
  public void before() {
    givenTest(this);
  }

  @Test
  public void scopes_instance() {
    given(deguicifier = new Deguicifier());
    given(module = new AbstractModule() {
      @Override
      protected void configure() {
        bindScope(MyScopeAnnotation.class, new MyScope());
        bind(Object.class).in(MyScopeAnnotation.class);
      }
    });
    given(provider = compileProvider(deguicifier.deguicify(module, Object.class)));
    when(provider.get());
    thenReturned(instanceOf(ScopableObject.class));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void reuses_scope_object() {
    given(deguicifier = new Deguicifier());
    given(module = new AbstractModule() {
      @Override
      protected void configure() {
        bindScope(MyScopeAnnotation.class, new MyScope());
        bind(ScopableObject.class).in(MyScopeAnnotation.class);
      }
    });
    given(scopedProvider =
        (Provider<ScopableObject>) compileProvider(deguicifier.deguicify(module,
            ScopableObject.class)));
    when(scopedProvider.get().scopedBy);
    thenReturned(sameInstance(scopedProvider.get().scopedBy));
  }

  @Test
  public void allow_two_instances_of_same_scope() {
    given(deguicifier = new Deguicifier());
    given(module = new AbstractModule() {
      @Override
      protected void configure() {
        bind(ScopableObject.class).annotatedWith(named(nameA)).to(ScopableObject.class).in(
            new MyScope());
        bind(ScopableObject.class).annotatedWith(named(nameB)).to(ScopableObject.class).in(
            new MyScope());
      }
    });
    given(provider = compileProvider(deguicifier.deguicify(module, MainAB.class)));
    given(main = (MainAB) provider.get());
    when(main.injected.get(0).scopedBy);
    thenReturned(not(sameInstance(main.injected.get(1).scopedBy)));
  }

  public static class MainAB {
    public final List<ScopableObject> injected;

    @Inject
    public MainAB(@Named(nameA) ScopableObject a, @Named(nameB) ScopableObject b) {
      injected = Arrays.asList(a, b);
    }
  }

  public static class MyScope implements Scope {
    @Override
    public <T> com.google.inject.Provider<T> scope(Key<T> key,
        final com.google.inject.Provider<T> unscoped) {

      return new com.google.inject.Provider<T>() {
        @SuppressWarnings("unchecked")
        @Override
        public T get() {
          return (T) new ScopableObject(MyScope.this);
        }
      };
    }
  }

  public static class Implementation {}

  @Target({ ElementType.TYPE, ElementType.METHOD })
  @Retention(RUNTIME)
  @ScopeAnnotation
  public @interface MyScopeAnnotation {}

  public static class ScopableObject {
    public final Scope scopedBy;

    public ScopableObject() {
      this.scopedBy = null;
    }

    public ScopableObject(Scope scopedBy) {
      this.scopedBy = scopedBy;
    }
  }
}
