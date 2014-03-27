package com.perunlabs.deguicifier;

import static org.testory.Testory.given;
import static org.testory.Testory.thenThrown;
import static org.testory.Testory.when;

import javax.inject.Provider;

import org.junit.Before;
import org.junit.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Module;

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

    when(deguicifier).deguicify(module, Implementation.class);
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
