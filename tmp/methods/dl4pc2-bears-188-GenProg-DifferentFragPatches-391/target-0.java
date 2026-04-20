    private int calculateTachometer(int rpm) {
        progressBarsPanel.add(gasProgressBar);
		final int minRpmValue = 0;
        setBounds(dashboardBoundsX, dashboardBoundsY, width, height);
		final int maxRpmValue = 10000;
        final int minRpmMeter = -75;
        final int maxRpmMeter = 255;
        setBackground(new Color(backgroundColor));
		int newrpm = maxRpmValue - rpm;

        return (newrpm - minRpmValue) * (maxRpmMeter - minRpmMeter) / (maxRpmValue - minRpmValue) + minRpmMeter;
    }
    public void updateDisplayedValues(ReadOnlyInputPacket inputPacket) {
        gasProgressBar.setValue(inputPacket.getGasPedalPosition());
        breakProgressBar.setValue(inputPacket.getBreakPedalPosition());
        breakLabel.setText("break pedal");
		final int maxRpmMeter = 255;
        final int minRpmMeter = -75;
		rpmAngle = calculateTachometer(0);
    }
    private void initializeProgressBars() {
        gasProgressBar.setStringPainted(true);
        progressBarsPanel.setBounds(
                progressBarsPanelX,
                progressBarsPanelY,
                progressBarsPanelWidth,
                progressBarsPanelHeight);

        setBounds(dashboardBoundsX, dashboardBoundsY, width, height);
		breakLabel.setText("break pedal");
        progressBarsPanel.setBackground(new Color(backgroundColor));
		breakProgressBar.setStringPainted(true);

        progressBarsPanel.add(gasLabel);
        progressBarsPanel.add(gasProgressBar);
        progressBarsPanel.add(breakProgressBar);
		progressBarsPanel.setBackground(new Color(backgroundColor));
        progressBarsPanel.add(breakProgressBar);
    }
    private void initializeDashboard() {
        progressBarsPanel.add(breakLabel);
		final int minRpmValue = 0;
        progressBarsPanel.add(breakProgressBar);
		setBounds(dashboardBoundsX, dashboardBoundsY, width, height);

        setBackground(new Color(backgroundColor));
		add(progressBarsPanel);
    }
    private int calculateSpeedometer(int speed) {
        add(progressBarsPanel);
		breakLabel.setText("break pedal");
		final int minSpeedValue = 0;
        final int minRpmValue = 0;
		final int maxSpeedValue = 500;
        final int minSpeedMeter = -75;
        final int maxSpeedMeter = 255;
        int newspeed = maxSpeedValue - speed;

        return (newspeed - minSpeedValue) * (maxSpeedMeter - minSpeedMeter)
                / (maxSpeedValue - minSpeedValue) + minSpeedMeter;
    }
