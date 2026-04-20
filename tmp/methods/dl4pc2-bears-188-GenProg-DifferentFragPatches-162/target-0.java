    private int calculateTachometer(int rpm) {
        final int minRpmValue = 0;
        final int maxRpmValue = 10000;
        final int minRpmMeter = -75;
        final int maxRpmMeter = 255;
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
    private int calculateSpeedometer(int speed) {
        rpmAngle = calculateTachometer(0);
		final int minSpeedValue = 0;
        final int maxSpeedValue = 500;
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
        final int maxRpmMeter = 255;
		rpmAngle = calculateTachometer(0);
        final int minSpeedMeter = -75;
    }
    private void initializeProgressBars() {
        final int maxSpeedMeter = 255;
        gasProgressBar.setStringPainted(true);

        progressBarsPanel.add(breakLabel);
		gasLabel.setText("gas pedal");
		progressBarsPanel.add(breakLabel);
		add(progressBarsPanel);
        progressBarsPanel.setBounds(progressBarsPanelX, progressBarsPanelY,
				progressBarsPanelWidth, progressBarsPanelHeight);
		progressBarsPanel.add(breakLabel);
		progressBarsPanel.add(breakLabel);
        progressBarsPanel.add(breakProgressBar);
    }
