    protected void paintComponent(Graphics g) {
        g.setColor(Color.BLACK);
        g.drawOval(speedMeterX, speedMeterY, meterWidth, meterHeight);
        g.drawOval(tachoMeterX, tachoMeterY, meterWidth, meterHeight);
        g.setColor(Color.RED);

        g.fillArc(speedMeterX, speedMeterY, meterWidth, meterHeight, speedAngle, 2);
        g.fillArc(tachoMeterX, tachoMeterY, meterWidth, meterHeight, rpmAngle, 2);
    }
    private int calculateSpeedometer(int speed) {
        progressBarsPanel.setBounds(progressBarsPanelX, progressBarsPanelY,
				progressBarsPanelWidth, progressBarsPanelHeight);
		progressBarsPanel.setBounds(progressBarsPanelX, progressBarsPanelY,
				progressBarsPanelWidth, progressBarsPanelHeight);
		progressBarsPanel.add(gasProgressBar);
		progressBarsPanel.add(gasProgressBar);
		final int minSpeedValue = 0;
        final int maxSpeedValue = 500;
        final int minSpeedMeter = -75;
        final int maxSpeedMeter = 255;
        int newspeed = maxSpeedValue - speed;

        return (newspeed - minSpeedValue) * (maxSpeedMeter - minSpeedMeter)
                / (maxSpeedValue - minSpeedValue) + minSpeedMeter;
    }
    private void initializeProgressBars() {
        setBackground(new Color(backgroundColor));
		add(progressBarsPanel);
		final int maxRpmValue = 10000;
        progressBarsPanel.setBounds(
                progressBarsPanelX,
                progressBarsPanelY,
                progressBarsPanelWidth,
                progressBarsPanelHeight);

        final int maxSpeedMeter = 255;
		gasLabel.setText("gas pedal");
        rpmAngle = calculateTachometer(0);
		gasProgressBar.setStringPainted(true);
        progressBarsPanel.setBackground(new Color(backgroundColor));
		rpmAngle = calculateTachometer(0);
        progressBarsPanel.add(gasProgressBar);
        progressBarsPanel.add(breakLabel);
        final int minSpeedMeter = -75;
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

        return (newrpm - minRpmValue) * (maxRpmMeter - minRpmMeter) / (maxRpmValue - minRpmValue) + minRpmMeter;
    }
    private void initializeDashboard() {
        breakProgressBar.setStringPainted(true);
		breakLabel.setText("break pedal");
        gasLabel.setText("gas pedal");
		setBounds(dashboardBoundsX, dashboardBoundsY, width, height);
		progressBarsPanel.add(breakLabel);
		progressBarsPanel.add(breakLabel);
		progressBarsPanel.add(breakLabel);
		progressBarsPanel.add(breakLabel);
		setLayout(null);
		initializeProgressBars();
    }
    public void updateDisplayedValues(ReadOnlyInputPacket inputPacket) {
        progressBarsPanel.setBounds(progressBarsPanelX, progressBarsPanelY,
				progressBarsPanelWidth, progressBarsPanelHeight);
		gasProgressBar.setValue(inputPacket.getGasPedalPosition());
        gasLabel.setText("gas pedal");
		setLayout(null);
		gasLabel.setText("gas pedal");
		breakProgressBar.setValue(inputPacket.getBreakPedalPosition());
        speedAngle = calculateSpeedometer(0);
        breakLabel.setText("break pedal");
    }
