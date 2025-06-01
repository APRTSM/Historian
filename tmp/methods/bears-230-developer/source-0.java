  private static String extractPeerService(String url) {
    Matcher matcher = URL_PEER_SERVICE_FINDER.matcher(url);
    if (matcher.find() && matcher.groupCount() == 1) {
      return matcher.group(1);
    }
    return "";
  }
  private static OptionalBoolean withActiveSpanOnly(String url) {
    if(url == null) {
      return OptionalBoolean.OPTION_NOT_FOUND;
    }
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
  private void decorate(Span span, StatementInformation statementInformation)
      throws SQLException {
    final String dbUrl =
        statementInformation.getConnectionInformation().getConnection().getMetaData().getURL();
    final String extractedPeerName = extractPeerService(dbUrl);
    final String peerName =
        extractedPeerName != null && !extractedPeerName.isEmpty() ? extractedPeerName
            : defaultPeerService;
    final String dbUser = statementInformation.getConnectionInformation()
        .getConnection()
        .getMetaData()
        .getUserName();
    final String dbInstance =
        statementInformation.getConnectionInformation().getConnection().getCatalog();

    Tags.COMPONENT.set(span, "java-p6spy");
    Tags.DB_STATEMENT.set(span, statementInformation.getSql());
    Tags.DB_TYPE.set(span, extractDbType(dbUrl));
    Tags.DB_INSTANCE.set(span, dbInstance);
    span.setTag("peer.address", dbUrl);
    if (peerName != null && !peerName.isEmpty()) {
      Tags.PEER_SERVICE.set(span, peerName);
    }
    if (dbUser != null && !dbUser.isEmpty()) {
      Tags.DB_USER.set(span, dbUser);
    }
  }
