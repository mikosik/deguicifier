package com.perunlabs.deguicifier;

import static org.testory.Testory.given;
import static org.testory.Testory.thenEqual;
import static org.testory.Testory.thenReturned;
import static org.testory.Testory.thenThrown;
import static org.testory.Testory.when;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Module;

public class MainTest {
  private Deguicifier deguicifier;
  private Module module;
  private final MainWrapper main = new MainWrapper();

  private PrintStream outBefore;
  private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();

  @Before
  public void before() {
    outBefore = System.out;
    System.setOut(new PrintStream(outContent));
    given(deguicifier = new Deguicifier());
  }

  @After
  public void after() {
    System.setOut(outBefore);
  }

  @Test
  public void binds_boolean_instance() throws Exception {
    given(module = new MyModule());
    when(main).main(MyModule.class.getName(), String.class.getName());
    thenReturned();
    thenEqual(outContent.toString(), deguicifier.deguicify(module, String.class));
  }

  @Test
  public void too_few_arguments_causes_exception() throws Exception {
    given(module = new MyModule());
    when(main).main(MyModule.class.getName());
    thenThrown(RuntimeException.class);
  }

  @Test
  public void too_many_arguments_causes_exception() throws Exception {
    given(module = new MyModule());
    when(main).main(MyModule.class.getName(), String.class.getName(), "third arg");
    thenThrown(RuntimeException.class);
  }

  public static class MyModule extends AbstractModule {
    @Override
    protected void configure() {
      bind(String.class).toInstance("abc");
    }
  }

  public static class MainWrapper {
    public void main(String... args) throws Exception {
      Main.main(args);
    }
  }
}
