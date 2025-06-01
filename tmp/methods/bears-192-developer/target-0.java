  public void setCurrentTime(DateTime currentTime) {
    this.currentTime = currentTime;
  }
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
    this.currentTime = DateTime.now();
    this.intensity = roundDoubleNicely(intensity);
    this.incrementalRepair = incrementalRepair;
    this.totalSegments = totalSegments;
    this.repairParallelism = repairParallelism;
    this.segmentsRepaired = segmentsRepaired;
    this.lastEvent = lastEvent;

    this.nodes = nodes;
    this.datacenters = datacenters;
    this.blacklistedTables = blacklistedTables;

    if (startTime == null) {
      duration = null;
    } else {
      if (state == RepairRun.RunState.RUNNING || state == RepairRun.RunState.PAUSED) {
        duration =
            DurationFormatUtils.formatDurationWords(
                new Duration(startTime.toInstant(), currentTime.toInstant()).getMillis(),
                true,
                false);
      } else if (state == RepairRun.RunState.ABORTED) {
        duration =
            DurationFormatUtils.formatDurationWords(
                new Duration(startTime.toInstant(), pauseTime.toInstant()).getMillis(),
                true,
                false);
      } else if (endTime != null) {
        duration =
            DurationFormatUtils.formatDurationWords(
                new Duration(startTime.toInstant(), endTime.toInstant()).getMillis(), true, false);
      } else {
        duration = null;
      }
    }

    if (startTime == null) {
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
  public String getCurrentTimeIso8601() {
    return dateTimeToIso8601(currentTime);
  }
  public DateTime getCurrentTime() {
    return currentTime;
  }
  public void setCurrentTimeIso8601(String dateStr) {
    if (null != dateStr) {
      currentTime = ISODateTimeFormat.dateTimeNoMillis().parseDateTime(dateStr);
    }
  }
