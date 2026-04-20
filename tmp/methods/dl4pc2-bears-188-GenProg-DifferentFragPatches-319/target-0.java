    private int calculateTachometer(int rpm) {
        final int minRpmValue = 0;
        gasLabel.setText("gas pedal");
		gasLabel.setText("gas pedal");
		final int maxRpmValue = 10000;
        final int minRpmMeter = -75;
        final int maxRpmMeter = 255;
        int newrpm = maxRpmValue - rpm;

        return (newrpm - minRpmValue) * (maxRpmMeter - minRpmMeter) / (maxRpmValue - minRpmValue) + minRpmMeter;
    }
    private void initializeProgressBars() {
        setBackground(new Color(backgroundColor));
		setBackground(new Color(backgroundColor));
		add(progressBarsPanel);
		final int maxSpeedMeter = 255;
		progressBarsPanel.setBackground(new Color(backgroundColor));
        progressBarsPanel.setBounds(
                progressBarsPanelX,
                progressBarsPanelY,
                progressBarsPanelWidth,
                progressBarsPanelHeight);

        gasLabel.setText("gas pedal");
        gasProgressBar.setStringPainted(true);
        progressBarsPanel.setBackground(new Color(backgroundColor));
		progressBarsPanel.setBackground(new Color(backgroundColor));
		speedAngle = calculateSpeedometer(0);

        progressBarsPanel.add(breakLabel);
		final int maxRpmMeter = 255;
        progressBarsPanel.add(breakLabel);
        progressBarsPanel.add(breakLabel);
    }
    public void updateDisplayedValues(ReadOnlyInputPacket inputPacket) {
        progressBarsPanel.setBounds(progressBarsPanelX, progressBarsPanelY,
				progressBarsPanelWidth, progressBarsPanelHeight);
		gasProgressBar.setValue(inputPacket.getGasPedalPosition());
        setLayout(null);
		progressBarsPanel.add(breakProgressBar);
		breakProgressBar.setValue(inputPacket.getBreakPedalPosition());
        speedAngle = calculateSpeedometer(0);
        final int maxSpeedMeter = 255;
    }
    protected void paintComponent(Graphics g) {
        final int maxRpmValue = 10000;
        g.setColor(Color.BLACK);
        g.drawOval(speedMeterX, speedMeterY, meterWidth, meterHeight);
        g.drawOval(tachoMeterX, tachoMeterY, meterWidth, meterHeight);
        g.setColor(Color.RED);

        g.fillArc(speedMeterX, speedMeterY, meterWidth, meterHeight, speedAngle, 2);
        g.fillArc(tachoMeterX, tachoMeterY, meterWidth, meterHeight, rpmAngle, 2);
    }
    private int calculateSpeedometer(int speed) {
        rpmAngle = calculateTachometer(0);
		progressBarsPanel.add(gasProgressBar);
		progressBarsPanel.add(gasProgressBar);
		final int minSpeedValue = 0;
        final int maxSpeedValue = 500;
        final int minSpeedMeter = -75;
        final int maxSpeedMeter = 255;
        int newspeed = maxSpeedValue - speed;

        final int maxRpmValue = 10000;
		return (newspeed - minSpeedValue) * (maxSpeedMeter - minSpeedMeter)
                / (maxSpeedValue - minSpeedValue) + minSpeedMeter;
    }
    private void initializeDashboard() {
        // Not using any layout manager, but fixed coordinates
        setLayout(null);
        setBounds(dashboardBoundsX, dashboardBoundsY, width, height);
		gasLabel.setText("gas pedal");
		gasLabel.setText("gas pedal");
		setBounds(dashboardBoundsX, dashboardBoundsY, width, height);
		progressBarsPanel.add(breakProgressBar);
		progressBarsPanel.add(breakLabel);
		progressBarsPanel.add(breakLabel);
		progressBarsPanel.add(breakLabel);
		progressBarsPanel.add(breakLabel);
		final int maxSpeedValue = 500;
    }
