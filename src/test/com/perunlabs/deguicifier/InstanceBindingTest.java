package com.perunlabs.deguicifier;

import static com.perunlabs.deguicifier.testing.SimpleJavaCompiler.compileProvider;
import static org.testory.Testory.given;
import static org.testory.Testory.thenReturned;
import static org.testory.Testory.thenThrown;
import static org.testory.Testory.when;

import javax.inject.Provider;

import org.junit.Before;
import org.junit.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Module;

public class InstanceBindingTest {
  private Deguicifier deguicifier;
  private Module module;
  private Provider<?> provider;
  private Boolean booleanValue;
  private Character characterValue;
  private Byte byteValue;
  private Short shortValue;
  private Integer integerValue;
  private Long longValue;
  private Float floatValue;
  private Double doubleValue;
  private String stringValue;

  @Before
  public void before() {
    given(booleanValue = true);
    given(characterValue = 'x');
    given(byteValue = 11);
    given(shortValue = 22);
    given(integerValue = 33);
    given(longValue = 44L);
    given(floatValue = -1.23f);
    given(doubleValue = -4.56);
    given(stringValue = "stringValue");
    given(deguicifier = new Deguicifier());
  }

  @Test
  public void binds_boolean_instance() {
    given(module = new AbstractModule() {
      @Override
      protected void configure() {
        bind(Boolean.class).toInstance(booleanValue);
      }
    });
    given(provider = compileProvider(deguicifier.deguicify(module, Boolean.class, "MyFactory")));
    when(provider.get());
    thenReturned(booleanValue);
  }

  @Test
  public void binds_character_instance() {
    given(module = new AbstractModule() {
      @Override
      protected void configure() {
        bind(Character.class).toInstance(characterValue);
      }
    });
    given(provider = compileProvider(deguicifier.deguicify(module, Character.class, "MyFactory")));
    when(provider.get());
    thenReturned(characterValue);
  }

  @Test
  public void binds_byte_instance() {
    given(module = new AbstractModule() {
      @Override
      protected void configure() {
        bind(Byte.class).toInstance(byteValue);
      }
    });
    given(provider = compileProvider(deguicifier.deguicify(module, Byte.class, "MyFactory")));
    when(provider.get());
    thenReturned(byteValue);
  }

  @Test
  public void binds_short_instance() {
    given(module = new AbstractModule() {
      @Override
      protected void configure() {
        bind(Short.class).toInstance(shortValue);
      }
    });
    given(provider = compileProvider(deguicifier.deguicify(module, Short.class, "MyFactory")));
    when(provider.get());
    thenReturned(shortValue);
  }

  @Test
  public void binds_integer_instance() {
    given(module = new AbstractModule() {
      @Override
      protected void configure() {
        bind(Integer.class).toInstance(integerValue);
      }
    });
    given(provider = compileProvider(deguicifier.deguicify(module, Integer.class, "MyFactory")));
    when(provider.get());
    thenReturned(integerValue);
  }

  @Test
  public void binds_long_instance() {
    given(module = new AbstractModule() {
      @Override
      protected void configure() {
        bind(Long.class).toInstance(longValue);
      }
    });
    given(provider = compileProvider(deguicifier.deguicify(module, Long.class, "MyFactory")));
    when(provider.get());
    thenReturned(longValue);
  }

  @Test
  public void binds_float_instance() {
    given(module = new AbstractModule() {
      @Override
      protected void configure() {
        bind(Float.class).toInstance(floatValue);
      }
    });
    given(provider = compileProvider(deguicifier.deguicify(module, Float.class, "MyFactory")));
    when(provider.get());
    thenReturned(floatValue);
  }

  @Test
  public void binds_double_instance() {
    given(module = new AbstractModule() {
      @Override
      protected void configure() {
        bind(Double.class).toInstance(doubleValue);
      }
    });
    given(provider = compileProvider(deguicifier.deguicify(module, Double.class, "MyFactory")));
    when(provider.get());
    thenReturned(doubleValue);
  }

  @Test
  public void binds_string_instance() {
    given(module = new AbstractModule() {
      @Override
      protected void configure() {
        bind(String.class).toInstance(stringValue);
      }
    });
    given(provider = compileProvider(deguicifier.deguicify(module, String.class, "MyFactory")));
    when(provider.get());
    thenReturned(stringValue);
  }

  @Test
  public void binds_string_instance_escaping() {
    given(stringValue = "   \"   \\   \t   \n   \r   \b   \f   ");
    given(module = new AbstractModule() {
      @Override
      protected void configure() {
        bind(String.class).toInstance(stringValue);
      }
    });
    given(provider = compileProvider(deguicifier.deguicify(module, String.class, "MyFactory")));
    when(provider.get());
    thenReturned(stringValue);
  }

  @Test
  public void does_not_bind_other_types() throws Exception {
    given(module = new AbstractModule() {
      @Override
      public void configure() {
        bind(Implementation.class).toInstance(new Implementation());
      }
    });

    when(deguicifier).deguicify(module, Implementation.class, "MyFactory");
    thenThrown(DeguicifierException.class);
  }

  public static class Implementation {}
}
