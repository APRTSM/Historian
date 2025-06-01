    public void updateDisplayedValues(ReadOnlyInputPacket inputPacket) {
        gasProgressBar.setValue(inputPacket.getGasPedalPosition());
        breakProgressBar.setValue(inputPacket.getBreakPedalPosition());
        progressBarsPanel.add(breakProgressBar);
        setBackground(new Color(backgroundColor));
		rpmAngle = calculateTachometer(0);
        speedAngle = calculateSpeedometer(0);
    }
