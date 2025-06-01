  public ConnectorImpl(final Instance instance, Credentials cred) throws AccumuloException, AccumuloSecurityException {
    ArgumentChecker.notNull(instance, cred);
    if (cred.getToken().isDestroyed())
      throw new AccumuloSecurityException(cred.getPrincipal(), SecurityErrorCode.TOKEN_EXPIRED);

    this.instance = instance;
    
    this.credentials = cred;
    
    // Skip fail fast for system services; string literal for class name, to avoid
    if (!"org.apache.accumulo.server.security.SystemCredentials$SystemToken".equals(cred.getToken().getClass().getName())) {
      ServerClient.execute(instance, new ClientExec<ClientService.Client>() {
        @Override
        public void execute(ClientService.Client iface) throws Exception {
          if (!iface.authenticate(Tracer.traceInfo(), credentials.toThrift(instance)))
            throw new AccumuloSecurityException("Authentication failed, access denied", SecurityErrorCode.BAD_CREDENTIALS);
        }
      });
    }
  }
  MockConnector(String username, MockInstance instance) throws AccumuloSecurityException {
    this(new Credentials(username, new NullToken()), new MockAccumulo(MockInstance.getDefaultFileSystem()), instance);
  }
  MockConnector(Credentials credentials, MockAccumulo acu, MockInstance instance) throws AccumuloSecurityException {
    if (credentials.getToken().isDestroyed())
      throw new AccumuloSecurityException(credentials.getPrincipal(), SecurityErrorCode.TOKEN_EXPIRED);
    this.username = credentials.getPrincipal();
    this.acu = acu;
    this.instance = instance;
  }
