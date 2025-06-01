    private int calculateTachometer(int rpm) {
        final int minRpmValue = 0;
        gasLabel.setText("gas pedal");
		final int maxRpmValue = 10000;
        final int minRpmMeter = -75;
        final int maxRpmMeter = 255;
        setBackground(new Color(backgroundColor));
		setBounds(dashboardBoundsX, dashboardBoundsY, width, height);
		setBackground(new Color(backgroundColor));
		setBounds(dashboardBoundsX, dashboardBoundsY, width, height);
		setBackground(new Color(backgroundColor));
		setBounds(dashboardBoundsX, dashboardBoundsY, width, height);
		int newrpm = maxRpmValue - rpm;

        breakLabel.setText("break pedal");
		return (newrpm - minRpmValue) * (maxRpmMeter - minRpmMeter) / (maxRpmValue - minRpmValue) + minRpmMeter;
    }
    private void initializeDashboard() {
        gasLabel.setText("gas pedal");
		setBounds(dashboardBoundsX, dashboardBoundsY, width, height);
		progressBarsPanel.add(gasLabel);
        setBounds(dashboardBoundsX, dashboardBoundsY, width, height);
		progressBarsPanel.add(breakLabel);
		progressBarsPanel.add(breakLabel);
		progressBarsPanel.add(breakLabel);
		progressBarsPanel.add(breakLabel);
		final int maxSpeedValue = 500;
		progressBarsPanel.setBounds(progressBarsPanelX, progressBarsPanelY,
				progressBarsPanelWidth, progressBarsPanelHeight);
		initializeProgressBars();
    }
    private int calculateSpeedometer(int speed) {
        progressBarsPanel.setBounds(progressBarsPanelX, progressBarsPanelY,
				progressBarsPanelWidth, progressBarsPanelHeight);
		progressBarsPanel.add(gasProgressBar);
		progressBarsPanel.add(gasProgressBar);
		progressBarsPanel.add(gasProgressBar);
		progressBarsPanel.add(gasProgressBar);
		final int minSpeedValue = 0;
        breakLabel.setText("break pedal");
		final int maxSpeedValue = 500;
        final int minSpeedMeter = -75;
        progressBarsPanel.add(breakLabel);
		final int maxSpeedMeter = 255;
        int newspeed = maxSpeedValue - speed;

        final int maxRpmValue = 10000;
		return (newspeed - minSpeedValue) * (maxSpeedMeter - minSpeedMeter)
                / (maxSpeedValue - minSpeedValue) + minSpeedMeter;
    }
    private void initializeProgressBars() {
        gasProgressBar.setStringPainted(true);
		setBackground(new Color(backgroundColor));
		gasProgressBar.setStringPainted(true);
		progressBarsPanel.add(breakProgressBar);
        final int maxSpeedValue = 500;

        speedAngle = calculateSpeedometer(0);
        progressBarsPanel.add(breakProgressBar);
        progressBarsPanel.add(breakLabel);
		gasProgressBar.setStringPainted(true);
        progressBarsPanel.setBackground(new Color(backgroundColor));
		progressBarsPanel.setBackground(new Color(backgroundColor));
		gasProgressBar.setStringPainted(true);

        progressBarsPanel.add(breakLabel);
		breakLabel.setText("break pedal");
		progressBarsPanel.add(breakLabel);
    }
    public void updateDisplayedValues(ReadOnlyInputPacket inputPacket) {
        breakProgressBar.setStringPainted(true);
		gasProgressBar.setValue(inputPacket.getGasPedalPosition());
        gasLabel.setText("gas pedal");
		setLayout(null);
		setLayout(null);
		breakProgressBar.setValue(inputPacket.getBreakPedalPosition());
		progressBarsPanel.add(breakProgressBar);
		breakProgressBar.setValue(inputPacket.getBreakPedalPosition());
        speedAngle = calculateSpeedometer(0);
        final int maxRpmMeter = 255;
		setBounds(dashboardBoundsX, dashboardBoundsY, width, height);
        gasProgressBar.setValue(inputPacket.getGasPedalPosition());
		progressBarsPanel.add(gasProgressBar);
    }
