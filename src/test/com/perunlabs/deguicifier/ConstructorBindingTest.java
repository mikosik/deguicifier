package com.perunlabs.deguicifier;

import static com.perunlabs.deguicifier.testing.SimpleJavaCompiler.compiledInstance;
import static org.hamcrest.Matchers.instanceOf;
import static org.testory.Testory.given;
import static org.testory.Testory.thenReturned;
import static org.testory.Testory.when;

import javax.inject.Provider;

import org.junit.Test;

import com.google.inject.AbstractModule;

public class ConstructorBindingTest {
  private Deguicifier deguicifier;
  private String javaFile;

  @Test
  public void generate_get_instance_for_constructor_binding() {
    given(deguicifier = new Deguicifier());
    given(javaFile = deguicifier.deguicify(new AbstractModule() {
      @Override
      protected void configure() {
        bind(Implementation.class);
      }
    }, Implementation.class));
    when(((Provider<Implementation>) compiledInstance(javaFile)).get());
    thenReturned(instanceOf(Implementation.class));
  }

  public static class Implementation {}

}
