package com.perunlabs.deguicifier;

import static com.perunlabs.deguicifier.testing.SimpleJavaCompiler.compiledInstance;
import static org.testory.Testory.given;
import static org.testory.Testory.thenReturned;
import static org.testory.Testory.when;

import org.junit.Test;

import com.google.inject.AbstractModule;

public class EmptyModuleTest {
  Deguicifier deguicifier;
  String javaFile;

  @Test
  public void can_deguicify_empty_module() {
    given(deguicifier = new Deguicifier());
    given(javaFile = deguicifier.deguicify(new EmptyModule()));
    when(compiledInstance(javaFile));
    thenReturned();
  }

  private final class EmptyModule extends AbstractModule {
    @Override
    protected void configure() {}
  }
}
