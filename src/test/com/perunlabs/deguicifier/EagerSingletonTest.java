package com.perunlabs.deguicifier;

import static org.testory.Testory.given;
import static org.testory.Testory.thenThrown;
import static org.testory.Testory.when;

import org.junit.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Module;

public class EagerSingletonTest {
  private Deguicifier deguicifier;
  private Module module;

  @Test
  public void eager_singleton_is_not_supported() {
    given(deguicifier = new Deguicifier());
    given(module = new AbstractModule() {
      @Override
      protected void configure() {
        bind(Object.class).asEagerSingleton();
      }
    });
    when(deguicifier).deguicify(module, Object.class);
    thenThrown(DeguicifierException.class);
  }
}
