  public static <T> T trace(final T instance, final Sampler sampler) {
    InvocationHandler handler = new InvocationHandler() {
      @Override
      public Object invoke(Object obj, Method method, Object[] args) throws Throwable {
        Span span = null;
        if (sampler.next()) {
          span = Trace.on(method.getName());
        }
        try {
          return method.invoke(instance, args);
          // Can throw RuntimeException, Error, or any checked exceptions of the method.
        } catch (InvocationTargetException ite) {
          Throwable cause = ite.getCause();
          if (cause == null) {
            // This should never happen, but account for it anyway
            log.error("Invocation exception during trace with null cause: ", ite);
            throw new RuntimeException(ite);
          }
          throw cause;
        } catch (IllegalAccessException e) {
          throw new RuntimeException(e);
        } finally {
          if (span != null) {
            span.stop();
          }
        }
      }
    };
    return (T) Proxy.newProxyInstance(instance.getClass().getClassLoader(), instance.getClass().getInterfaces(), handler);
  }
