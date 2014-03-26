package com.perunlabs.deguicifier;

import static com.perunlabs.deguicifier.testing.SimpleJavaCompiler.compileProvider;
import static org.testory.Testory.given;
import static org.testory.Testory.thenReturned;
import static org.testory.Testory.when;

import javax.inject.Inject;
import javax.inject.Provider;

import org.junit.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.TypeLiteral;

public class GenericBindingTest {
  private Deguicifier deguicifier;
  private Module module;
  private Provider<?> provider;

  private static String string = "abc";
  private static int integer = 3;

  @Test
  public void binds_generic_type() {
    given(deguicifier = new Deguicifier());
    given(module = new AbstractModule() {
      @Override
      protected void configure() {
        bind(new TypeLiteral<Holder<String>>() {}).to(StringHolder.class);
      }
    });
    given(provider = compileProvider(deguicifier.deguicify(module, Wrapper.class)));
    when(((Wrapper) provider.get()).heldValue.get());
    thenReturned(string);
  }

  public static class Wrapper {
    public final Holder<String> heldValue;

    @Inject
    public Wrapper(Holder<String> stringHolder) {
      this.heldValue = stringHolder;
    }
  }

  @Test
  public void binds_generic_type_twice_with_different_generic_parameter() {
    given(deguicifier = new Deguicifier());
    given(module = new AbstractModule() {
      @Override
      protected void configure() {
        bind(new TypeLiteral<Holder<String>>() {}).to(StringHolder.class);
        bind(new TypeLiteral<Holder<Integer>>() {}).to(IntegerHolder.class);
      }
    });
    given(provider = compileProvider(deguicifier.deguicify(module, DoubleWrapper.class)));
    when(((DoubleWrapper) provider.get()).heldValue.get());
    thenReturned(string);
  }

  public static class DoubleWrapper {
    public final Holder<String> heldValue;
    public final Holder<Integer> integerHolder;

    @Inject
    public DoubleWrapper(Holder<String> stringHolder, Holder<Integer> integerHolder) {
      this.heldValue = stringHolder;
      this.integerHolder = integerHolder;
    }
  }

  public interface Holder<E> {
    public E get();
  }

  public static class IntegerHolder implements Holder<Integer> {
    @Override
    public Integer get() {
      return integer;
    }
  }

  public static class StringHolder implements Holder<String> {
    @Override
    public String get() {
      return string;
    }
  }
}
