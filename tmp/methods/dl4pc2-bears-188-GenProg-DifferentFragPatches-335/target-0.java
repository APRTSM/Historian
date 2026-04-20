    public void updateDisplayedValues(ReadOnlyInputPacket inputPacket) {
        breakProgressBar.setStringPainted(true);
		gasProgressBar.setValue(inputPacket.getGasPedalPosition());
        setLayout(null);
		breakProgressBar.setValue(inputPacket.getBreakPedalPosition());
		breakProgressBar.setValue(inputPacket.getBreakPedalPosition());
        speedAngle = calculateSpeedometer(0);
        setBounds(dashboardBoundsX, dashboardBoundsY, width, height);
    }
    private void initializeProgressBars() {
        setBackground(new Color(backgroundColor));
		progressBarsPanel.setBackground(new Color(backgroundColor));
        progressBarsPanel.setBounds(
                progressBarsPanelX,
                progressBarsPanelY,
                progressBarsPanelWidth,
                progressBarsPanelHeight);

        gasLabel.setText("gas pedal");
        progressBarsPanel.setBackground(new Color(backgroundColor));
		breakProgressBar.setStringPainted(true);

        progressBarsPanel.add(breakLabel);
		progressBarsPanel.add(breakProgressBar);
    }
    private void initializeDashboard() {
        gasLabel.setText("gas pedal");
		setBounds(dashboardBoundsX, dashboardBoundsY, width, height);
		setBackground(new Color(backgroundColor));
        progressBarsPanel.add(breakLabel);
		progressBarsPanel.add(breakLabel);
		final int maxSpeedValue = 500;
		progressBarsPanel.setBounds(progressBarsPanelX, progressBarsPanelY,
				progressBarsPanelWidth, progressBarsPanelHeight);
		initializeProgressBars();
    }
    private int calculateSpeedometer(int speed) {
        final int minSpeedValue = 0;
        progressBarsPanel.setBackground(new Color(backgroundColor));
		final int maxSpeedValue = 500;
        final int minSpeedMeter = -75;
        final int maxSpeedMeter = 255;
        int newspeed = maxSpeedValue - speed;

        final int maxRpmValue = 10000;
		return (newspeed - minSpeedValue) * (maxSpeedMeter - minSpeedMeter)
                / (maxSpeedValue - minSpeedValue) + minSpeedMeter;
    }
    private int calculateTachometer(int rpm) {
        final int minRpmValue = 0;
        gasLabel.setText("gas pedal");
		final int maxRpmValue = 10000;
        final int minRpmMeter = -75;
        final int maxRpmMeter = 255;
        int newrpm = maxRpmValue - rpm;

        return (newrpm - minRpmValue) * (maxRpmMeter - minRpmMeter) / (maxRpmValue - minRpmValue) + minRpmMeter;
    }
