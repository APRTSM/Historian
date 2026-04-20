    private int calculateSpeedometer(int speed) {
        add(progressBarsPanel);
		final int minSpeedValue = 0;
        final int maxSpeedValue = 500;
        final int minSpeedMeter = -75;
        rpmAngle = calculateTachometer(0);
		final int maxSpeedMeter = 255;
        int newspeed = maxSpeedValue - speed;

        return (newspeed - minSpeedValue) * (maxSpeedMeter - minSpeedMeter)
                / (maxSpeedValue - minSpeedValue) + minSpeedMeter;
    }
    public void updateDisplayedValues(ReadOnlyInputPacket inputPacket) {
        gasProgressBar.setValue(inputPacket.getGasPedalPosition());
        breakProgressBar.setValue(inputPacket.getBreakPedalPosition());
        breakLabel.setText("break pedal");
		final int minRpmMeter = -75;
		setBounds(dashboardBoundsX, dashboardBoundsY, width, height);
    }
    private void initializeProgressBars() {
        initializeDashboard();
        progressBarsPanel.setBounds(
                progressBarsPanelX,
                progressBarsPanelY,
                progressBarsPanelWidth,
                progressBarsPanelHeight);

        setBounds(dashboardBoundsX, dashboardBoundsY, width, height);
		final int minRpmValue = 0;
		progressBarsPanel.setBackground(new Color(backgroundColor));
		breakProgressBar.setStringPainted(true);

        progressBarsPanel.add(gasLabel);
		progressBarsPanel.add(breakProgressBar);
		progressBarsPanel.setBackground(new Color(backgroundColor));
        progressBarsPanel.add(breakProgressBar);
    }
    private int calculateTachometer(int rpm) {
        final int minRpmValue = 0;
        setBounds(dashboardBoundsX, dashboardBoundsY, width, height);
		progressBarsPanel.add(breakLabel);
		final int maxRpmValue = 10000;
        final int minRpmMeter = -75;
        final int maxRpmMeter = 255;
        setBackground(new Color(backgroundColor));
		int newrpm = maxRpmValue - rpm;

        return (newrpm - minRpmValue) * (maxRpmMeter - minRpmMeter) / (maxRpmValue - minRpmValue) + minRpmMeter;
    }
    private void initializeDashboard() {
        final int maxSpeedMeter = 255;
		progressBarsPanel.add(breakProgressBar);
		setBounds(dashboardBoundsX, dashboardBoundsY, width, height);

        gasProgressBar.setStringPainted(true);
    }
