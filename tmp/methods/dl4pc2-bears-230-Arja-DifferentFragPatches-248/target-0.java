  private Scope buildSpan(Tracer tracer, String operationName, StatementInformation statementInformation) {
    try {
      final Scope activeScope = tracer.scopeManager().active();
      final String dbUrl =
          statementInformation.getConnectionInformation().getConnection().getMetaData().getURL();
      final Tracer.SpanBuilder spanBuilder = tracer
          .buildSpan(operationName)
          .withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CLIENT);
      final Scope scope = spanBuilder.startActive(true);
      return scope;
    } catch (SQLException e) {
      return NoopScopeManager.NoopScope.INSTANCE;
    }
  }
  private void onBefore(String operationName, StatementInformation statementInformation) {
    final Tracer tracer = GlobalTracer.get();
    final Scope scope = buildSpan(tracer, operationName, statementInformation);
    currentScope.set(scope);
  }
