  private static String extractPeerService(String url) {
    if (url != null) {
      Matcher matcher = URL_PEER_SERVICE_FINDER.matcher(url);
      if (matcher.find() && matcher.groupCount() == 1) {
        return matcher.group(1);
      }
    }
    return "";
  }
  private static boolean isNullOrEmpty(String s) {
    return s == null || s.isEmpty();
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
    if (!isNullOrEmpty(dbUrl)) {
      span.setTag("peer.address", dbUrl);
      Tags.DB_TYPE.set(span, extractDbType(dbUrl));
    }
    if (!isNullOrEmpty(dbInstance)) {
      Tags.DB_INSTANCE.set(span, dbInstance);
    }
    if (!isNullOrEmpty(peerName)) {
      Tags.PEER_SERVICE.set(span, peerName);
    }
    if (!isNullOrEmpty(dbUser)) {
      Tags.DB_USER.set(span, dbUser);
    }
  }
