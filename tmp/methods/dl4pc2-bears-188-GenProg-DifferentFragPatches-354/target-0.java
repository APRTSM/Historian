    protected void paintComponent(Graphics g) {
        g.setColor(Color.BLACK);
        g.drawOval(speedMeterX, speedMeterY, meterWidth, meterHeight);
        g.drawOval(tachoMeterX, tachoMeterY, meterWidth, meterHeight);
        g.setColor(Color.RED);

        g.fillArc(speedMeterX, speedMeterY, meterWidth, meterHeight, speedAngle, 2);
        g.fillArc(tachoMeterX, tachoMeterY, meterWidth, meterHeight, rpmAngle, 2);
    }
    private void initializeProgressBars() {
        final int maxSpeedMeter = 255;
        add(progressBarsPanel);
        progressBarsPanel.setBackground(new Color(backgroundColor));
		gasProgressBar.setStringPainted(true);

        gasLabel.setText("gas pedal");
		progressBarsPanel.add(breakLabel);
		final int maxSpeedValue = 500;
		add(progressBarsPanel);
        progressBarsPanel.setBounds(progressBarsPanelX, progressBarsPanelY,
				progressBarsPanelWidth, progressBarsPanelHeight);
		progressBarsPanel.add(breakLabel);
		progressBarsPanel.add(breakLabel);
        progressBarsPanel.add(breakProgressBar);
    }
    public void updateDisplayedValues(ReadOnlyInputPacket inputPacket) {
        progressBarsPanel.setBounds(progressBarsPanelX, progressBarsPanelY,
				progressBarsPanelWidth, progressBarsPanelHeight);
		final int maxRpmValue = 10000;
		gasProgressBar.setValue(inputPacket.getGasPedalPosition());
        setLayout(null);
		breakProgressBar.setValue(inputPacket.getBreakPedalPosition());
		breakProgressBar.setValue(inputPacket.getBreakPedalPosition());
        final int maxRpmMeter = 255;
		setBounds(dashboardBoundsX, dashboardBoundsY, width, height);
        setLayout(null);
    }
    private int calculateTachometer(int rpm) {
        final int minRpmValue = 0;
        final int maxRpmValue = 10000;
        final int minRpmMeter = -75;
        final int maxRpmMeter = 255;
        setBackground(new Color(backgroundColor));
		setBounds(dashboardBoundsX, dashboardBoundsY, width, height);
		int newrpm = maxRpmValue - rpm;

        return (newrpm - minRpmValue) * (maxRpmMeter - minRpmMeter) / (maxRpmValue - minRpmValue) + minRpmMeter;
    }
    private void initializeDashboard() {
        // Not using any layout manager, but fixed coordinates
        setLayout(null);
        setBackground(new Color(backgroundColor));
        progressBarsPanel.add(breakLabel);
		progressBarsPanel.add(breakLabel);
		setBounds(dashboardBoundsX, dashboardBoundsY, width, height);
    }
