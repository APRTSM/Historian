    public void updateDisplayedValues(ReadOnlyInputPacket inputPacket) {
        gasProgressBar.setValue(inputPacket.getGasPedalPosition());
        breakProgressBar.setValue(inputPacket.getBreakPedalPosition());
        speedAngle = calculateSpeedometer(0);
        setBackground(new Color(backgroundColor));
		rpmAngle = calculateTachometer(0);
        speedAngle = calculateSpeedometer(0);
    }
