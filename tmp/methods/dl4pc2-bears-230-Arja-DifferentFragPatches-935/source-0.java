  private Scope buildSpan(Tracer tracer, String operationName, StatementInformation statementInformation) {
    try {
      final Scope activeScope = tracer.scopeManager().active();
      final String dbUrl =
          statementInformation.getConnectionInformation().getConnection().getMetaData().getURL();
      if (!allowTraceWithNoActiveSpan(dbUrl) && activeScope == null) {
        return NoopScopeManager.NoopScope.INSTANCE;
      }

      final Tracer.SpanBuilder spanBuilder = tracer
          .buildSpan(operationName)
          .withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CLIENT);
      if (activeScope != null) {
        spanBuilder.asChildOf(activeScope.span());
      }
      final Scope scope = spanBuilder.startActive(true);
      decorate(scope.span(), statementInformation);
      return scope;
    } catch (SQLException e) {
      return NoopScopeManager.NoopScope.INSTANCE;
    }
  }
