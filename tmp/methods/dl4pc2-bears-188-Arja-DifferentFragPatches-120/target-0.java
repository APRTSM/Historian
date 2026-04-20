    public void updateDisplayedValues(ReadOnlyInputPacket inputPacket) {
        gasProgressBar.setValue(inputPacket.getGasPedalPosition());
        breakProgressBar.setValue(inputPacket.getBreakPedalPosition());
        gasProgressBar.setValue(inputPacket.getGasPedalPosition());
		speedAngle = calculateSpeedometer(0);
        rpmAngle = calculateTachometer(0);
        add(progressBarsPanel);
    }
