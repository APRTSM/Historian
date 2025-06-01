    public void updateDisplayedValues(ReadOnlyInputPacket inputPacket) {
        gasProgressBar.setValue(inputPacket.getGasPedalPosition());
        breakProgressBar.setValue(inputPacket.getBreakPedalPosition());
        speedAngle = calculateSpeedometer(0);
        gasProgressBar.setStringPainted(true);
		rpmAngle = calculateTachometer(0);
        initializeDashboard();
    }
