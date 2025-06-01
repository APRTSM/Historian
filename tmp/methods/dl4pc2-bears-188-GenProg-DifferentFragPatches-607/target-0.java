    private int calculateTachometer(int rpm) {
        final int minRpmValue = 0;
        final int maxRpmValue = 10000;
        final int minSpeedMeter = -75;
		final int minRpmMeter = -75;
        progressBarsPanel.setBackground(new Color(backgroundColor));
		final int maxRpmMeter = 255;
        int newrpm = maxRpmValue - rpm;

        return (newrpm - minRpmValue) * (maxRpmMeter - minRpmMeter) / (maxRpmValue - minRpmValue) + minRpmMeter;
    }
    private void initializeProgressBars() {
        setLayout(null);
        progressBarsPanel.setBounds(
                progressBarsPanelX,
                progressBarsPanelY,
                progressBarsPanelWidth,
                progressBarsPanelHeight);

        gasLabel.setText("gas pedal");
        breakProgressBar.setStringPainted(true);
		final int maxRpmValue = 10000;
        breakProgressBar.setStringPainted(true);

        add(progressBarsPanel);
        rpmAngle = calculateTachometer(0);
		setBackground(new Color(backgroundColor));
        progressBarsPanel.add(breakLabel);
        progressBarsPanel.setBounds(progressBarsPanelX, progressBarsPanelY,
				progressBarsPanelWidth, progressBarsPanelHeight);
    }
    public void updateDisplayedValues(ReadOnlyInputPacket inputPacket) {
        gasProgressBar.setValue(inputPacket.getGasPedalPosition());
        setLayout(null);
		final int maxSpeedMeter = 255;
		gasProgressBar.setStringPainted(true);
		breakProgressBar.setValue(inputPacket.getBreakPedalPosition());
        speedAngle = calculateSpeedometer(0);
        rpmAngle = calculateTachometer(0);
    }
    private void initializeDashboard() {
        progressBarsPanel.add(breakLabel);
        setBackground(new Color(backgroundColor));
        final int maxSpeedValue = 500;
		setBounds(dashboardBoundsX, dashboardBoundsY, width, height);

        progressBarsPanel.add(breakLabel);
		progressBarsPanel.add(breakLabel);
		progressBarsPanel.add(breakLabel);
		breakLabel.setText("break pedal");
    }
    private int calculateSpeedometer(int speed) {
        final int minSpeedValue = 0;
        gasProgressBar.setStringPainted(true);
		final int maxSpeedValue = 500;
        final int minSpeedMeter = -75;
        gasLabel.setText("gas pedal");
		final int maxSpeedMeter = 255;
        int newspeed = maxSpeedValue - speed;

        final int maxRpmValue = 10000;
		return (newspeed - minSpeedValue) * (maxSpeedMeter - minSpeedMeter)
                / (maxSpeedValue - minSpeedValue) + minSpeedMeter;
    }
