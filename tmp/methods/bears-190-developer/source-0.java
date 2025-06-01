  private GossipInfo parseEndpointStatesString(
      String sourceNode,
      String allEndpointStates,
      Map<String, String> simpleStates) {

    List<EndpointState> endpointStates = Lists.newArrayList();
    Set<String> endpoints = Sets.newHashSet();
    Matcher matcher;

    String[] strEndpoints = allEndpointStates.split("(?<![0-9a-zA-Z ])/");
    Double totalLoad = 0.0;

    for (int i = 1; i < strEndpoints.length; i++) {
      String endpointString = strEndpoints[i];
      Optional<String> status = Optional.absent();
      Optional<String> endpoint = parseEndpointState(ENDPOINT_NAME_PATTERNS, endpointString, 1, String.class);

      for (Pattern endpointStatusPattern : ENDPOINT_STATUS_PATTERNS) {
        matcher = endpointStatusPattern.matcher(endpointString);
        if (matcher.find() && matcher.groupCount() >= 3) {
          status = Optional.of(matcher.group(3) + " - " + simpleStates.getOrDefault("/" + endpoint.or(""), "UNKNOWN"));
          break;
        }
      }

      Optional<String> dc = parseEndpointState(ENDPOINT_DC_PATTERNS, endpointString, 3, String.class);
      Optional<String> rack = parseEndpointState(ENDPOINT_RACK_PATTERNS, endpointString, 3, String.class);
      Optional<Double> severity = parseEndpointState(ENDPOINT_SEVERITY_PATTERNS, endpointString, 3, Double.class);
      Optional<String> releaseVersion = parseEndpointState(ENDPOINT_RELEASE_PATTERNS, endpointString, 3, String.class);
      Optional<String> hostId = parseEndpointState(ENDPOINT_HOSTID_PATTERNS, endpointString, 3, String.class);
      Optional<String> tokens = parseEndpointState(ENDPOINT_TOKENS_PATTERNS, endpointString, 2, String.class);
      Optional<Double> load = parseEndpointState(ENDPOINT_LOAD_PATTERNS, endpointString, 3, Double.class);
      totalLoad += load.or(0.0);

      EndpointState endpointState = new EndpointState(
          endpoint.or(NOT_AVAILABLE),
          hostId.or(NOT_AVAILABLE),
          dc.or(NOT_AVAILABLE),
          rack.or(NOT_AVAILABLE),
          status.or(NOT_AVAILABLE),
          severity.or(0.0),
          releaseVersion.or(NOT_AVAILABLE),
          tokens.or(NOT_AVAILABLE),
          load.or(0.0));

      endpoints.add(endpoint.or(NOT_AVAILABLE));
      endpointStates.add(endpointState);
    }

    Map<String, Map<String, List<EndpointState>>> endpointsByDcAndRack = Maps.newHashMap();
    Map<String, List<EndpointState>> endpointsByDc
        = endpointStates.stream().collect(Collectors.groupingBy(EndpointState::getDc, Collectors.toList()));

    for (String dc : endpointsByDc.keySet()) {
      Map<String, List<EndpointState>> endpointsByRack
          = endpointsByDc.get(dc).stream().collect(Collectors.groupingBy(EndpointState::getRack, Collectors.toList()));
      endpointsByDcAndRack.put(dc, endpointsByRack);
    }

    return new GossipInfo(sourceNode, endpointsByDcAndRack, totalLoad, endpoints);
  }
