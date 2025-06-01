  public Authorizations(byte[] authorizations) {
    
    ArgumentChecker.notNull(authorizations);

    String authsString = new String(authorizations);
    if (authsString.startsWith(HEADER)) {
      // its the new format
      authsString = authsString.substring(HEADER.length());
      if (authsString.length() > 0) {
        for (String encAuth : authsString.split(",")) {
          byte[] auth = Base64.decodeBase64(encAuth.getBytes());
          auths.add(new ArrayByteSequence(auth));
        }
        checkAuths();
      }
    } else {
      // its the old format
      ArgumentChecker.notNull(authorizations);
      if (authorizations.length > 0)
        setAuthorizations(authsString.split(","));
    }
  }
