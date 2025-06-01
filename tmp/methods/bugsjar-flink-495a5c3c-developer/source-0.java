	public JobGraph createJobGraph(String jobName) {
		jobGraph = new JobGraph(jobName);

		// make sure that all vertices start immediately
		jobGraph.setScheduleMode(ScheduleMode.ALL);
		
		
		init();

		setChaining();

		setPhysicalEdges();

		setSlotSharing();
		
		configureCheckpointing();

		return jobGraph;
	}
