        ApplicationsService applicationsService() {
            return new ApplicationsService();
        }
        DeploymentsService deploymentsService() {
            return new DeploymentsService();
        }
        public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
            taskRegistrar.setScheduler(taskExecutor());
            taskRegistrar.addTriggerTask(
                    new Runnable() {
                        @Override public void run() {
                            LOGGER.debug(">>> Refreshing Apps now: " + System.currentTimeMillis());
                            applicationsService().refresh();
                        }
                    },
                    new Trigger() {
                        @Override public Date nextExecutionTime(TriggerContext triggerContext) {
                            Calendar nextExecutionTime =  new GregorianCalendar();
                            Date lastActualExecutionTime = triggerContext.lastActualExecutionTime();
                            nextExecutionTime.setTime(lastActualExecutionTime != null ? lastActualExecutionTime : new Date());
                            nextExecutionTime.add(Calendar.MILLISECOND, refreshRate);
                            return nextExecutionTime.getTime();
                        }
                    }
            );
        }
