    public void updateDisplayedValues(ReadOnlyInputPacket inputPacket) {
        rpmAngle = calculateTachometer(0);
		gasProgressBar.setValue(inputPacket.getGasPedalPosition());
        breakProgressBar.setValue(inputPacket.getBreakPedalPosition());
        speedAngle = calculateSpeedometer(0);
        rpmAngle = calculateTachometer(0);
    }
