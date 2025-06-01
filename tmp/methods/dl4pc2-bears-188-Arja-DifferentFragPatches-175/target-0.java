    public void updateDisplayedValues(ReadOnlyInputPacket inputPacket) {
        gasProgressBar.setValue(inputPacket.getGasPedalPosition());
        breakProgressBar.setValue(inputPacket.getBreakPedalPosition());
        add(progressBarsPanel);
		speedAngle = calculateSpeedometer(0);
        rpmAngle = calculateTachometer(0);
    }
