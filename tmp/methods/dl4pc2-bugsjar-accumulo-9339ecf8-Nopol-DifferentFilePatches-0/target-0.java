  protected byte[] createPassword(AuthenticationTokenIdentifier identifier) {
    DelegationTokenConfig cfg = identifier.getConfig();

    long now = System.currentTimeMillis();
    final AuthenticationKey secretKey = currentKey;
    identifier.setKeyId(secretKey.getKeyId());
    identifier.setIssueDate(now);
    long expiration = now + tokenMaxLifetime;
    // Catch overflow
    if (expiration < now) {
      expiration = Long.MAX_VALUE;
    }
    identifier.setExpirationDate(expiration);

    // Limit the lifetime if the user requests it
    if (null != cfg) {
      long requestedLifetime = cfg.getTokenLifetime(TimeUnit.MILLISECONDS);
      if (0 < requestedLifetime) {
        if (0 < requestedLifetime) {
          long requestedExpirationDate = identifier.getIssueDate() + requestedLifetime;
          // Catch overflow again
          if (requestedExpirationDate < identifier.getIssueDate()) {
            requestedExpirationDate = Long.MAX_VALUE;
          }
          // Ensure that the user doesn't try to extend the expiration date -- they may only limit it
          if (requestedExpirationDate > identifier.getExpirationDate()) {
            throw new RuntimeException("Requested token lifetime exceeds configured maximum");
          }
          log.trace("Overriding token expiration date from {} to {}", identifier.getExpirationDate(), requestedExpirationDate);
          identifier.setExpirationDate(requestedExpirationDate);
        }
      }
    }

    identifier.setInstanceId(instance.getInstanceID());
    return createPassword(identifier.getBytes(), secretKey.getKey());
  }
