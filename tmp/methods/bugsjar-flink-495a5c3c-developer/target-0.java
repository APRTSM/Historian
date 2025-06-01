	public JobGraph createJobGraph(String jobName) {
		jobGraph = new JobGraph(jobName);

		// make sure that all vertices start immediately
		jobGraph.setScheduleMode(ScheduleMode.ALL);
		
		
		init();

		setChaining();

		setPhysicalEdges();

		setSlotSharing();
		
		configureCheckpointing();

		try {
			InstantiationUtil.writeObjectToConfig(this.streamGraph.getExecutionConfig(), this.jobGraph.getJobConfiguration(), ExecutionConfig.CONFIG_KEY);
		} catch (IOException e) {
			throw new RuntimeException("Config object could not be written to Job Configuration: ", e);
		}

		return jobGraph;
	}
