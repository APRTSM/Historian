    private int calculateTachometer(int rpm) {
        setLayout(null);
		final int minRpmValue = 0;
        gasLabel.setText("gas pedal");
		final int maxRpmValue = 10000;
        final int minRpmMeter = -75;
        final int maxRpmMeter = 255;
        setBackground(new Color(backgroundColor));
		progressBarsPanel.setBackground(new Color(backgroundColor));
		int newrpm = maxRpmValue - rpm;

        return (newrpm - minRpmValue) * (maxRpmMeter - minRpmMeter) / (maxRpmValue - minRpmValue) + minRpmMeter;
    }
    private int calculateSpeedometer(int speed) {
        final int minSpeedValue = 0;
        final int maxSpeedValue = 500;
        final int minSpeedMeter = -75;
        final int maxSpeedMeter = 255;
        int newspeed = maxSpeedValue - speed;

        final int maxRpmValue = 10000;
		return (newspeed - minSpeedValue) * (maxSpeedMeter - minSpeedMeter)
                / (maxSpeedValue - minSpeedValue) + minSpeedMeter;
    }
    private void initializeProgressBars() {
        setBackground(new Color(backgroundColor));
		progressBarsPanel.setBounds(
                progressBarsPanelX,
                progressBarsPanelY,
                progressBarsPanelWidth,
                progressBarsPanelHeight);

        gasProgressBar.setStringPainted(true);
        progressBarsPanel.setBackground(new Color(backgroundColor));
		breakProgressBar.setStringPainted(true);

        speedAngle = calculateSpeedometer(0);
        progressBarsPanel.add(breakLabel);
		progressBarsPanel.add(breakProgressBar);
    }
    private void initializeDashboard() {
        gasLabel.setText("gas pedal");
		setBounds(dashboardBoundsX, dashboardBoundsY, width, height);
		setBackground(new Color(backgroundColor));
        final int maxRpmValue = 10000;
		progressBarsPanel.add(breakLabel);
		progressBarsPanel.add(breakLabel);
		final int maxSpeedValue = 500;
		progressBarsPanel.setBounds(progressBarsPanelX, progressBarsPanelY,
				progressBarsPanelWidth, progressBarsPanelHeight);
		initializeProgressBars();
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
        breakProgressBar.setStringPainted(true);
		final int minRpmValue = 0;
		gasProgressBar.setValue(inputPacket.getGasPedalPosition());
        setLayout(null);
		breakProgressBar.setValue(inputPacket.getBreakPedalPosition());
		breakProgressBar.setValue(inputPacket.getBreakPedalPosition());
        setBounds(dashboardBoundsX, dashboardBoundsY, width, height);
    }
