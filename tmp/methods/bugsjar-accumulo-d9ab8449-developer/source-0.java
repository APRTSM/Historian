  public ConnectorImpl(final Instance instance, Credentials cred) throws AccumuloException, AccumuloSecurityException {
    ArgumentChecker.notNull(instance, cred);
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
