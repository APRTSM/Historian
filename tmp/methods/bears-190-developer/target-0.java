  private GossipInfo parseEndpointStatesString(
      String sourceNode,
      String allEndpointStates,
      Map<String, String> simpleStates) {

    List<EndpointState> endpointStates = Lists.newArrayList();
    Set<String> endpoints = Sets.newHashSet();
    Matcher matcher;

    // Split into endpointState record strings
    String[] endpointLines = allEndpointStates.split("\n");
    List<String> strEndpoints = Lists.newArrayList();
    StringBuilder recordBuilder = null;
    for (String line: endpointLines) {
      if (!line.startsWith("  ")) {
        if (recordBuilder != null) {
          strEndpoints.add(recordBuilder.toString());
        }
        recordBuilder = new StringBuilder(line.substring(line.indexOf('/') + 1));
      } else if (recordBuilder != null) {
        recordBuilder.append('\n');
        recordBuilder.append(line);
      }
    }
    if (recordBuilder != null) {
      strEndpoints.add(recordBuilder.toString());
    }

    // Cleanup hostnames from simpleStates keys
    Map<String, String> simpleStatesCopy = new HashMap<>();
    for (Map.Entry<String, String> entry: simpleStates.entrySet()) {
      String entryKey = entry.getKey().substring(entry.getKey().indexOf('/'));
      simpleStatesCopy.put(entryKey, entry.getValue());
    }
    simpleStates = simpleStatesCopy;

    Double totalLoad = 0.0;

    for (String endpointString: strEndpoints) {
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
