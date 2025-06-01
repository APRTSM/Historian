    private void initializeDashboard() {
        // Not using any layout manager, but fixed coordinates
        setLayout(null);
        progressBarsPanel.setBackground(new Color(backgroundColor));
		setBackground(new Color(backgroundColor));
        setBounds(dashboardBoundsX, dashboardBoundsY, width, height);
		initializeProgressBars();
    }
    private int calculateSpeedometer(int speed) {
        rpmAngle = calculateTachometer(0);
		final int minSpeedValue = 0;
        final int maxSpeedValue = 500;
        final int minRpmMeter = -75;
		final int minSpeedMeter = -75;
        final int maxSpeedMeter = 255;
        int newspeed = maxSpeedValue - speed;

        return (newspeed - minSpeedValue) * (maxSpeedMeter - minSpeedMeter)
                / (maxSpeedValue - minSpeedValue) + minSpeedMeter;
    }
    public void updateDisplayedValues(ReadOnlyInputPacket inputPacket) {
        progressBarsPanel.setBounds(progressBarsPanelX, progressBarsPanelY,
				progressBarsPanelWidth, progressBarsPanelHeight);
		final int maxRpmValue = 10000;
		gasProgressBar.setValue(inputPacket.getGasPedalPosition());
        setLayout(null);
		breakProgressBar.setValue(inputPacket.getBreakPedalPosition());
        gasLabel.setText("gas pedal");
		rpmAngle = calculateTachometer(0);
        final int minSpeedMeter = -75;
    }
    private void initializeProgressBars() {
        final int maxSpeedMeter = 255;
        gasLabel.setText("gas pedal");
        gasProgressBar.setStringPainted(true);
        breakProgressBar.setStringPainted(true);

        progressBarsPanel.add(breakLabel);
		add(progressBarsPanel);
        progressBarsPanel.setBounds(progressBarsPanelX, progressBarsPanelY,
				progressBarsPanelWidth, progressBarsPanelHeight);
		progressBarsPanel.add(gasLabel);
        progressBarsPanel.add(breakLabel);
        progressBarsPanel.add(breakProgressBar);
    }
