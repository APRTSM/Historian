    private int calculateSpeedometer(int speed) {
        final int minSpeedValue = 0;
        final int maxSpeedValue = 500;
        rpmAngle = calculateTachometer(0);
		final int minSpeedMeter = -75;
        final int maxSpeedMeter = 255;
        int newspeed = maxSpeedValue - speed;

        return (newspeed - minSpeedValue) * (maxSpeedMeter - minSpeedMeter)
                / (maxSpeedValue - minSpeedValue) + minSpeedMeter;
    }
    private void initializeDashboard() {
        setBounds(dashboardBoundsX, dashboardBoundsY, width, height);
        setBackground(new Color(backgroundColor));
        setBounds(dashboardBoundsX, dashboardBoundsY, width, height);

        initializeProgressBars();
    }
    public void updateDisplayedValues(ReadOnlyInputPacket inputPacket) {
        gasProgressBar.setValue(inputPacket.getGasPedalPosition());
        breakProgressBar.setValue(inputPacket.getBreakPedalPosition());
        speedAngle = calculateSpeedometer(0);
        rpmAngle = calculateTachometer(0);
        progressBarsPanel.add(breakProgressBar);
    }
