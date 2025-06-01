  public static <T> T trace(final T instance, final Sampler sampler) {
    InvocationHandler handler = new InvocationHandler() {
      @Override
      public Object invoke(Object obj, Method method, Object[] args) throws Throwable {
        if (!sampler.next()) {
          return method.invoke(instance, args);
        }
        Span span = Trace.on(method.getName());
        try {
          return method.invoke(instance, args);
        } catch (Throwable ex) {
          ex.printStackTrace();
          throw ex;
        } finally {
          span.stop();
        }
      }
    };
    return (T) Proxy.newProxyInstance(instance.getClass().getClassLoader(), instance.getClass().getInterfaces(), handler);
  }
