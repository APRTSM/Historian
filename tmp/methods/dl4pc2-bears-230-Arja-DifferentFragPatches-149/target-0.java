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
  private static OptionalBoolean withActiveSpanOnly(String url) {
    if(url.contains(TRACE_WITH_ACTIVE_SPAN_ONLY_FINDER) && url.contains(TRACE_WITHOUT_ACTIVE_SPAN_ONLY_FINDER)) {
      if(log.isLoggable(Level.WARNING)) {
        log.warning("jdbc url contains contradictory traceWithActiveSpanOnly=true and traceWithActiveSpanOnly=false options. Defaulting to no options");
      }
      return OptionalBoolean.OPTION_NOT_FOUND;
    }
    if(url.contains(TRACE_WITH_ACTIVE_SPAN_ONLY_FINDER)) {
      return OptionalBoolean.TRUE;
    } else if (url.contains(TRACE_WITHOUT_ACTIVE_SPAN_ONLY_FINDER)) {
      return OptionalBoolean.FALSE;
    }
    return OptionalBoolean.OPTION_NOT_FOUND;
  }
