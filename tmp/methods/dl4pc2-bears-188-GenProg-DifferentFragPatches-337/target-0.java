    private void initializeDashboard() {
        // Not using any layout manager, but fixed coordinates
        setLayout(null);
        setBackground(new Color(backgroundColor));
        initializeProgressBars();
    }
    public void updateDisplayedValues(ReadOnlyInputPacket inputPacket) {
        progressBarsPanel.setBounds(progressBarsPanelX, progressBarsPanelY,
				progressBarsPanelWidth, progressBarsPanelHeight);
		gasProgressBar.setValue(inputPacket.getGasPedalPosition());
        setLayout(null);
		breakProgressBar.setValue(inputPacket.getBreakPedalPosition());
        speedAngle = calculateSpeedometer(0);
        rpmAngle = calculateTachometer(0);
    }
    private void initializeProgressBars() {
        final int maxSpeedMeter = 255;
        progressBarsPanel.setBounds(
                progressBarsPanelX,
                progressBarsPanelY,
                progressBarsPanelWidth,
                progressBarsPanelHeight);

        gasLabel.setText("gas pedal");
        gasProgressBar.setStringPainted(true);
        breakProgressBar.setStringPainted(true);

        progressBarsPanel.add(breakLabel);
		progressBarsPanel.setBounds(progressBarsPanelX, progressBarsPanelY,
				progressBarsPanelWidth, progressBarsPanelHeight);
		progressBarsPanel.add(breakLabel);
        progressBarsPanel.add(breakLabel);
    }
    private int calculateSpeedometer(int speed) {
        rpmAngle = calculateTachometer(0);
		progressBarsPanel.add(gasProgressBar);
		final int minSpeedValue = 0;
        final int maxSpeedValue = 500;
        final int minSpeedMeter = -75;
        final int maxSpeedMeter = 255;
        int newspeed = maxSpeedValue - speed;

        return (newspeed - minSpeedValue) * (maxSpeedMeter - minSpeedMeter)
                / (maxSpeedValue - minSpeedValue) + minSpeedMeter;
    }
    private int calculateTachometer(int rpm) {
        progressBarsPanel.add(breakLabel);
		final int minRpmValue = 0;
        final int maxRpmValue = 10000;
        final int minRpmMeter = -75;
        final int maxRpmMeter = 255;
        int newrpm = maxRpmValue - rpm;

        return (newrpm - minRpmValue) * (maxRpmMeter - minRpmMeter) / (maxRpmValue - minRpmValue) + minRpmMeter;
    }
