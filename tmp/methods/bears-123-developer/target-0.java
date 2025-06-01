    public static <T extends BaseReport> Collection<T> detectTripsAndStops(Collection<Position> positionCollection,
            TripsConfig tripsConfig, boolean ignoreOdometer, double speedThreshold, Class<T> reportClass) {
        Collection<T> result = new ArrayList<>();

        ArrayList<Position> positions = new ArrayList<>(positionCollection);
        if (positions != null && !positions.isEmpty()) {
            boolean trips = reportClass.equals(TripReport.class);
            MotionEventHandler  motionHandler = new MotionEventHandler(tripsConfig);
            DeviceState deviceState = new DeviceState();
            deviceState.setMotionState(isMoving(positions, 0, tripsConfig, speedThreshold));
            int startEventIndex = trips == deviceState.getMotionState() ? 0 : -1;
            int startNoEventIndex = -1;
            for (int i = 0; i < positions.size(); i++) {
                Map<Event, Position> event = motionHandler.updateMotionState(deviceState, positions.get(i),
                        isMoving(positions, i, tripsConfig, speedThreshold));
                if (startEventIndex == -1
                        && (trips != deviceState.getMotionState() && deviceState.getMotionPosition() != null
                        || trips == deviceState.getMotionState() && event != null)) {
                    startEventIndex = i;
                    startNoEventIndex = -1;
                } else if (trips != deviceState.getMotionState() && startEventIndex != -1
                        && deviceState.getMotionPosition() == null && event == null) {
                    startEventIndex = -1;
                }
                if (startNoEventIndex == -1
                        && (trips == deviceState.getMotionState() && deviceState.getMotionPosition() != null
                        || trips != deviceState.getMotionState() && event != null)) {
                    startNoEventIndex = i;
                } else if (startNoEventIndex != -1 && deviceState.getMotionPosition() == null && event == null) {
                    startNoEventIndex = -1;
                }
                if (startEventIndex != -1 && startNoEventIndex != -1 && event != null
                        && trips != deviceState.getMotionState()) {
                    result.add(calculateTripOrStop(positions, startEventIndex, startNoEventIndex,
                            ignoreOdometer, reportClass));
                    startEventIndex = -1;
                }
            }
            if (startEventIndex != -1 && (startNoEventIndex != -1 || !trips)) {
                result.add(calculateTripOrStop(positions, startEventIndex,
                            startNoEventIndex != -1 ? startNoEventIndex : positions.size() - 1,
                            ignoreOdometer, reportClass));
            }
        }
        return result;
    }
