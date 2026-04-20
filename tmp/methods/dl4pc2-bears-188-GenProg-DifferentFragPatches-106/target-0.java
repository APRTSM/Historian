    public void updateDisplayedValues(ReadOnlyInputPacket inputPacket) {
        final int maxSpeedValue = 500;
		gasProgressBar.setValue(inputPacket.getGasPedalPosition());
        breakProgressBar.setValue(inputPacket.getBreakPedalPosition());
        speedAngle = calculateSpeedometer(0);
        rpmAngle = calculateTachometer(0);
    }
    private void initializeDashboard() {
        final int maxRpmValue = 10000;
        setBackground(new Color(backgroundColor));
        setBounds(dashboardBoundsX, dashboardBoundsY, width, height);
    }
    private int calculateSpeedometer(int speed) {
        rpmAngle = calculateTachometer(0);
		final int minSpeedValue = 0;
        gasProgressBar.setStringPainted(true);
		final int minRpmValue = 0;
		final int maxSpeedValue = 500;
        final int minSpeedMeter = -75;
        final int maxSpeedMeter = 255;
        int newspeed = maxSpeedValue - speed;

        return (newspeed - minSpeedValue) * (maxSpeedMeter - minSpeedMeter)
                / (maxSpeedValue - minSpeedValue) + minSpeedMeter;
    }
    protected void paintComponent(Graphics g) {
        speedAngle = calculateSpeedometer(0);
        g.setColor(Color.BLACK);
        g.drawOval(speedMeterX, speedMeterY, meterWidth, meterHeight);
        g.drawOval(tachoMeterX, tachoMeterY, meterWidth, meterHeight);
        g.setColor(Color.RED);

        g.fillArc(speedMeterX, speedMeterY, meterWidth, meterHeight, speedAngle, 2);
        g.fillArc(tachoMeterX, tachoMeterY, meterWidth, meterHeight, rpmAngle, 2);
    }
    private void initializeProgressBars() {
        progressBarsPanel.setBounds(progressBarsPanelX, progressBarsPanelY,
				progressBarsPanelWidth, progressBarsPanelHeight);
		progressBarsPanel.setBounds(
                progressBarsPanelX,
                progressBarsPanelY,
                progressBarsPanelWidth,
                progressBarsPanelHeight);

        breakLabel.setText("break pedal");
        final int maxRpmValue = 10000;
        progressBarsPanel.setBackground(new Color(backgroundColor));
		breakProgressBar.setStringPainted(true);

        add(progressBarsPanel);
        setLayout(null);
        progressBarsPanel.add(gasProgressBar);
        progressBarsPanel.add(breakLabel);
        progressBarsPanel.add(breakProgressBar);
    }
