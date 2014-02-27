package com.perunlabs.deguicifier.testing;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import com.google.inject.spi.BindingTargetVisitor;
import com.perunlabs.deguicifier.Deguicifier;

public class Debug {
  @SuppressWarnings("unchecked")
  public static BindingTargetVisitor<Object, Void> printingVisitor() {
    return (BindingTargetVisitor<Object, Void>) Proxy.newProxyInstance(Deguicifier.class
        .getClassLoader(), new Class<?>[] { BindingTargetVisitor.class }, new InvocationHandler() {
      @Override
      public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String name = method.getParameterTypes()[0].getSimpleName();
        System.out.println(name + ":" + args[0]);
        return null;
      }
    });
  }
}
