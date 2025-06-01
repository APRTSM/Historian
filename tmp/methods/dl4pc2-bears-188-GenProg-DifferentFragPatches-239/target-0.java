    private void initializeProgressBars() {
        progressBarsPanel.setBackground(new Color(backgroundColor));
        final int maxSpeedMeter = 255;
		progressBarsPanel.add(gasProgressBar);
        progressBarsPanel.setBackground(new Color(backgroundColor));
		gasProgressBar.setStringPainted(true);

        gasLabel.setText("gas pedal");
		progressBarsPanel.add(breakLabel);
		gasLabel.setText("gas pedal");
        progressBarsPanel.add(breakLabel);
		progressBarsPanel.add(gasProgressBar);
        progressBarsPanel.add(breakProgressBar);
        progressBarsPanel.add(breakProgressBar);
    }
    private int calculateTachometer(int rpm) {
        final int minRpmValue = 0;
        progressBarsPanel.add(gasProgressBar);
		final int maxRpmValue = 10000;
        final int minRpmMeter = -75;
        final int maxRpmMeter = 255;
        setBackground(new Color(backgroundColor));
		setBounds(dashboardBoundsX, dashboardBoundsY, width, height);
		int newrpm = maxRpmValue - rpm;

        return (newrpm - minRpmValue) * (maxRpmMeter - minRpmMeter) / (maxRpmValue - minRpmValue) + minRpmMeter;
    }
    protected void paintComponent(Graphics g) {
        g.setColor(Color.BLACK);
        g.drawOval(speedMeterX, speedMeterY, meterWidth, meterHeight);
        g.drawOval(tachoMeterX, tachoMeterY, meterWidth, meterHeight);
        g.setColor(Color.RED);

        g.fillArc(speedMeterX, speedMeterY, meterWidth, meterHeight, speedAngle, 2);
        g.fillArc(tachoMeterX, tachoMeterY, meterWidth, meterHeight, rpmAngle, 2);
    }
    public void updateDisplayedValues(ReadOnlyInputPacket inputPacket) {
        final int maxRpmValue = 10000;
		gasProgressBar.setValue(inputPacket.getGasPedalPosition());
        gasLabel.setText("gas pedal");
		gasProgressBar.setStringPainted(true);
		breakProgressBar.setValue(inputPacket.getBreakPedalPosition());
        final int maxRpmMeter = 255;
		setBounds(dashboardBoundsX, dashboardBoundsY, width, height);
        setLayout(null);
    }
    private int calculateSpeedometer(int speed) {
        progressBarsPanel.setBounds(progressBarsPanelX, progressBarsPanelY,
				progressBarsPanelWidth, progressBarsPanelHeight);
		final int minSpeedValue = 0;
        final int maxSpeedValue = 500;
        final int minSpeedMeter = -75;
        final int maxSpeedMeter = 255;
        int newspeed = maxSpeedValue - speed;

        return (newspeed - minSpeedValue) * (maxSpeedMeter - minSpeedMeter)
                / (maxSpeedValue - minSpeedValue) + minSpeedMeter;
    }
    private void initializeDashboard() {
        // Not using any layout manager, but fixed coordinates
        setLayout(null);
        setBackground(new Color(backgroundColor));
        setBounds(dashboardBoundsX, dashboardBoundsY, width, height);

        progressBarsPanel.add(breakLabel);
		progressBarsPanel.add(breakLabel);
		setBounds(dashboardBoundsX, dashboardBoundsY, width, height);
    }
