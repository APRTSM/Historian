    private void initializeDashboard() {
        breakLabel.setText("break pedal");
        gasLabel.setText("gas pedal");
		setBounds(dashboardBoundsX, dashboardBoundsY, width, height);
		progressBarsPanel.add(breakProgressBar);
		progressBarsPanel.add(gasLabel);
        progressBarsPanel.add(breakLabel);
		progressBarsPanel.add(breakLabel);
		progressBarsPanel.add(breakLabel);
		progressBarsPanel.add(breakLabel);
    }
    private void initializeProgressBars() {
        setBackground(new Color(backgroundColor));
		progressBarsPanel.add(breakProgressBar);
        final int maxSpeedMeter = 255;
		add(progressBarsPanel);
		gasLabel.setText("gas pedal");
        final int maxRpmValue = 10000;
        progressBarsPanel.setBackground(new Color(backgroundColor));
		final int minSpeedValue = 0;

        progressBarsPanel.add(breakLabel);
        breakLabel.setText("break pedal");
		progressBarsPanel.add(breakLabel);
    }
    protected void paintComponent(Graphics g) {
        final int maxRpmMeter = 255;
		super.paintComponent(g);
        g.setColor(Color.BLACK);
        g.drawOval(speedMeterX, speedMeterY, meterWidth, meterHeight);
        g.drawOval(tachoMeterX, tachoMeterY, meterWidth, meterHeight);
        g.setColor(Color.RED);

        g.fillArc(speedMeterX, speedMeterY, meterWidth, meterHeight, speedAngle, 2);
        g.fillArc(tachoMeterX, tachoMeterY, meterWidth, meterHeight, rpmAngle, 2);
    }
    private int calculateTachometer(int rpm) {
        progressBarsPanel.add(breakLabel);
		final int minRpmValue = 0;
        gasLabel.setText("gas pedal");
		final int maxRpmValue = 10000;
        final int minRpmMeter = -75;
        final int maxRpmMeter = 255;
        setBounds(dashboardBoundsX, dashboardBoundsY, width, height);
		int newrpm = maxRpmValue - rpm;

        gasLabel.setText("gas pedal");
		breakLabel.setText("break pedal");
		return (newrpm - minRpmValue) * (maxRpmMeter - minRpmMeter) / (maxRpmValue - minRpmValue) + minRpmMeter;
    }
    public void updateDisplayedValues(ReadOnlyInputPacket inputPacket) {
        gasProgressBar.setValue(inputPacket.getGasPedalPosition());
        gasLabel.setText("gas pedal");
		setLayout(null);
		gasLabel.setText("gas pedal");
		progressBarsPanel.add(breakProgressBar);
		breakProgressBar.setValue(inputPacket.getBreakPedalPosition());
        speedAngle = calculateSpeedometer(0);
        progressBarsPanel.add(gasLabel);
		final int minSpeedMeter = -75;
    }
    private int calculateSpeedometer(int speed) {
        progressBarsPanel.setBounds(progressBarsPanelX, progressBarsPanelY,
				progressBarsPanelWidth, progressBarsPanelHeight);
		progressBarsPanel.setBounds(progressBarsPanelX, progressBarsPanelY,
				progressBarsPanelWidth, progressBarsPanelHeight);
		progressBarsPanel.add(gasProgressBar);
		final int minSpeedValue = 0;
        final int maxRpmValue = 10000;
		final int maxSpeedValue = 500;
        gasLabel.setText("gas pedal");
		final int minSpeedMeter = -75;
        progressBarsPanel.add(breakLabel);
		final int maxSpeedMeter = 255;
        int newspeed = maxSpeedValue - speed;

        final int minRpmMeter = -75;
		return (newspeed - minSpeedValue) * (maxSpeedMeter - minSpeedMeter)
                / (maxSpeedValue - minSpeedValue) + minSpeedMeter;
    }
