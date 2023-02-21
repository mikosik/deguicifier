
__This project is abandoned.__

__It was meant to workaround Guice problems on Android devices while still being able to use Guice in code.__

__Nowadays one should simply use Dagger.__




deguicifier
===========

Tool for converting guice modules into plain old java factories.  
(similar to those you would write by hand).

Usage example
-------------

Suppose we have java application that is bootstrapped using guice as shown below:

```java
import my.app.AppModule;
import my.app.App;
import com.google.inject.Injector;

public class Main {
  public static void main(String[] args) {
    Injector injector = Guice.createInjector(new AppModule());
    App app = injector.getInstance(App.class);
    app.start();
  }
}
```

We can use deguicifier to generate wiring code so we could bootstrap our app without using guice.
To generate mentioned code run:

```sh
java -cp deguicifier-0.2.0.jar:guice-3.0.jar:javax.inject.jar:app.jar \
  com.perunlabs.deguicifier.Main \
  my.app.AppModule \
  my.app.App \
  my.app.AppFactory \
  >> my/app/AppFactory.java
```

This will generate AppFactory java class.
You can use it to bootstrap your app without guice:

```java
import my.app.App;
import my.app.AppFactory;
import javax.inject.Provider;

public class Main {
  public static void main(String[] args) {
    Provider<App> appProvider = new AppFactory();
    App app = appProvider.get();
    app.start();
  }
}
```

Guice features that are not supported
-------------------------------------
 * binding to instance (except binding to primitive types, their wrappers, Strings)
 * binding to provider instance
 * binding as eager singleton
 * using MapBinder (Multibindings are supported)
 * circular dependencies
 * field injections
 * binding Scope instance which class cannot be easily instantiated - It is either not public, have no public constructor or have generic parameters (simply put it's not possible to generate code that instantiate such class).

