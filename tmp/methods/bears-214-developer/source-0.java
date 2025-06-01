  public CharSequence preferSameRackWeighting(Collection<UpstreamInfo> upstreams, UpstreamInfo currentUpstream, Options options) {
    final RackMethodsHelper rackHelper = new RackMethodsHelper();
    final List<String> allRacks = rackHelper.generateAllRacks(upstreams);
    final BigDecimal totalPendingLoad = rackHelper.getTotalPendingLoad(allRacks);
    final BigDecimal capacity = rackHelper.calculateCapacity(allRacks);
    return preferSameRackWeightingOperation(upstreams, currentUpstream, allRacks, capacity, totalPendingLoad, null);
  }
