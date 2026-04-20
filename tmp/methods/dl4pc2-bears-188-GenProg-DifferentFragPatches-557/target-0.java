    private int calculateSpeedometer(int speed) {
        progressBarsPanel.add(gasProgressBar);
		progressBarsPanel.add(gasProgressBar);
		progressBarsPanel.setBounds(progressBarsPanelX, progressBarsPanelY,
				progressBarsPanelWidth, progressBarsPanelHeight);
		progressBarsPanel.add(gasProgressBar);
		final int minSpeedValue = 0;
        final int maxSpeedValue = 500;
        final int minSpeedMeter = -75;
        progressBarsPanel.add(breakLabel);
		final int maxSpeedMeter = 255;
        int newspeed = maxSpeedValue - speed;

        return (newspeed - minSpeedValue) * (maxSpeedMeter - minSpeedMeter)
                / (maxSpeedValue - minSpeedValue) + minSpeedMeter;
    }
    public void updateDisplayedValues(ReadOnlyInputPacket inputPacket) {
        gasProgressBar.setValue(inputPacket.getGasPedalPosition());
        gasLabel.setText("gas pedal");
		setLayout(null);
		breakProgressBar.setValue(inputPacket.getBreakPedalPosition());
        speedAngle = calculateSpeedometer(0);
        final int maxRpmMeter = 255;
		setBounds(dashboardBoundsX, dashboardBoundsY, width, height);
        gasProgressBar.setValue(inputPacket.getGasPedalPosition());
    }
    private void initializeProgressBars() {
        gasProgressBar.setStringPainted(true);
		gasProgressBar.setStringPainted(true);
		progressBarsPanel.setBackground(new Color(backgroundColor));
        final int maxSpeedValue = 500;

        add(progressBarsPanel);
        progressBarsPanel.add(breakProgressBar);
        progressBarsPanel.add(breakLabel);
		gasProgressBar.setStringPainted(true);
        progressBarsPanel.setBackground(new Color(backgroundColor));
		progressBarsPanel.add(breakLabel);
        progressBarsPanel.add(breakLabel);
    }
    private int calculateTachometer(int rpm) {
        final int minRpmValue = 0;
        final int maxRpmValue = 10000;
        final int minRpmMeter = -75;
        final int maxRpmMeter = 255;
        setBackground(new Color(backgroundColor));
		setBounds(dashboardBoundsX, dashboardBoundsY, width, height);
		setBackground(new Color(backgroundColor));
		int newrpm = maxRpmValue - rpm;

        progressBarsPanel.setBounds(progressBarsPanelX, progressBarsPanelY,
				progressBarsPanelWidth, progressBarsPanelHeight);
		return (newrpm - minRpmValue) * (maxRpmMeter - minRpmMeter) / (maxRpmValue - minRpmValue) + minRpmMeter;
    }
    private void initializeDashboard() {
        initializeProgressBars();
        setBackground(new Color(backgroundColor));
        setBounds(dashboardBoundsX, dashboardBoundsY, width, height);

        setBounds(dashboardBoundsX, dashboardBoundsY, width, height);
		progressBarsPanel.add(breakLabel);
		progressBarsPanel.add(breakLabel);
		initializeProgressBars();
    }
