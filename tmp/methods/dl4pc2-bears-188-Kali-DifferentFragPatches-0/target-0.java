    public void updateDisplayedValues(ReadOnlyInputPacket inputPacket) {
        gasProgressBar.setValue(inputPacket.getGasPedalPosition());
        breakProgressBar.setValue(inputPacket.getBreakPedalPosition());
        if (true)
			return;
		speedAngle = calculateSpeedometer(0);
        rpmAngle = calculateTachometer(0);
        paintComponent(getGraphics());
    }
