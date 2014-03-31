package com.google.inject;

import static com.google.inject.Guice.createInjector;
import static org.testory.Testory.any;
import static org.testory.Testory.given;
import static org.testory.Testory.givenTest;
import static org.testory.Testory.onInstance;
import static org.testory.Testory.thenCalled;
import static org.testory.Testory.thenCalledTimes;
import static org.testory.Testory.when;

import org.junit.Before;
import org.junit.Test;

import com.google.inject.spi.BindingScopingVisitor;

public class BindingScopingVisitorTest {
  private Injector injector;
  private BindingScopingVisitor<?> visitor;
  private Key<CharSequence> key;
  private Class<String> implementation;

  @Before
  public void before() {
    givenTest(this);
    given(key = Key.get(CharSequence.class));
    given(implementation = String.class);
  }

  @Test
  public void visit_no_scoping() {
    given(injector = createInjector(new AbstractModule() {
      @Override
      protected void configure() {
        bind(key).to(implementation);
      }
    }));
    when(injector.getBinding(key).acceptScopingVisitor(visitor));
    thenCalled(visitor).visitNoScoping();
    thenCalledTimes(1, onInstance(visitor));
  }

  @Test
  public void visit_scope_annotation() {
    given(injector = createInjector(new AbstractModule() {
      @Override
      protected void configure() {
        bind(key).to(implementation).in(Singleton.class);
      }
    }));
    when(injector.getBinding(key).acceptScopingVisitor(visitor));
    thenCalled(visitor).visitScope(any(Scope.class));
    thenCalledTimes(1, onInstance(visitor));
  }

  @Test
  public void visit_scope_instance() {
    given(injector = createInjector(new AbstractModule() {
      @Override
      protected void configure() {
        bind(key).to(implementation).in(Scopes.SINGLETON);
      }
    }));
    when(injector.getBinding(key).acceptScopingVisitor(visitor));
    thenCalled(visitor).visitScope(any(Scope.class));
    thenCalledTimes(1, onInstance(visitor));
  }

  @Test
  public void visit_eager_singleton() {
    given(injector = createInjector(new AbstractModule() {
      @Override
      protected void configure() {
        bind(key).to(implementation).asEagerSingleton();
      }
    }));
    when(injector.getBinding(key).acceptScopingVisitor(visitor));
    thenCalled(visitor).visitEagerSingleton();
    thenCalledTimes(1, onInstance(visitor));
  }

  @Test
  public void visit_provides_method() {
    given(injector = createInjector(new AbstractModule() {
      @Override
      protected void configure() {}

      @Provides
      @Singleton
      public CharSequence provide() {
        return "";
      }
    }));
    when(injector.getBinding(key).acceptScopingVisitor(visitor));
    thenCalled(visitor).visitScope(any(Scope.class));
    thenCalledTimes(1, onInstance(visitor));
  }
}
