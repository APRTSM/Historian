  public void receiveResponse(HttpClientResponse clientResponse) {
    int sc = clientResponse.statusCode();
    int maxRedirects = request.followRedirects ? client.getOptions().getMaxRedirects(): 0;
    if (redirects < maxRedirects && sc >= 300 && sc < 400) {
      redirects++;
      Future<HttpClientRequest> next = client.redirectHandler().apply(clientResponse);
      if (next != null) {
        next.setHandler(ar -> {
          if (ar.succeeded()) {
            sendRequest(ar.result());
          } else {
            fail(ar.cause());
          }
        });
        return;
      }
    }
    this.clientResponse = clientResponse;
    fire(ClientPhase.RECEIVE_RESPONSE);
  }
