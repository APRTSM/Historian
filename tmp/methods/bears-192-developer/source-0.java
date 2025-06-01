  public RepairRunStatus(
      UUID runId,
      String clusterName,
      String keyspaceName,
      Collection<String> columnFamilies,
      int segmentsRepaired,
      int totalSegments,
      RepairRun.RunState state,
      DateTime startTime,
      DateTime endTime,
      String cause,
      String owner,
      String lastEvent,
      DateTime creationTime,
      DateTime pauseTime,
      double intensity,
      boolean incrementalRepair,
      RepairParallelism repairParallelism,
      Collection<String> nodes,
      Collection<String> datacenters,
      Collection<String> blacklistedTables) {

    this.id = runId;
    this.cause = cause;
    this.owner = owner;
    this.clusterName = clusterName;
    this.columnFamilies = columnFamilies;
    this.keyspaceName = keyspaceName;
    this.state = state;
    this.creationTime = creationTime;
    this.startTime = startTime;
    this.endTime = endTime;
    this.pauseTime = pauseTime;
    this.intensity = roundDoubleNicely(intensity);
    this.incrementalRepair = incrementalRepair;
    this.totalSegments = totalSegments;
    this.repairParallelism = repairParallelism;
    this.segmentsRepaired = segmentsRepaired;
    this.lastEvent = lastEvent;

    this.nodes = nodes;
    this.datacenters = datacenters;
    this.blacklistedTables = blacklistedTables;

    if (startTime == null || endTime == null) {
      duration = null;
    } else {
      duration = DurationFormatUtils.formatDurationWords(
          new Duration(startTime.toInstant(), endTime.toInstant()).getMillis(), true, false);
    }

    if (startTime == null || (endTime != null && endTime.isAfter(startTime))) {
      estimatedTimeOfArrival = null;
    } else {
      if (state == RepairRun.RunState.ERROR
          || state == RepairRun.RunState.DELETED
          || state == RepairRun.RunState.ABORTED
          || segmentsRepaired == 0) {
        estimatedTimeOfArrival = null;
      } else {
        long now = DateTime.now().getMillis();
        long currentDuration = now - startTime.getMillis();
        long millisecondsPerSegment = currentDuration / segmentsRepaired;
        int segmentsLeft = totalSegments - segmentsRepaired;
        estimatedTimeOfArrival = new DateTime(now + millisecondsPerSegment * segmentsLeft);
      }
    }
  }
