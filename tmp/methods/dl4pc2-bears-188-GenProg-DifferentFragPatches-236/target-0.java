    private int calculateTachometer(int rpm) {
        progressBarsPanel.add(breakLabel);
		final int minRpmValue = 0;
        gasLabel.setText("gas pedal");
		gasLabel.setText("gas pedal");
		final int maxRpmValue = 10000;
        final int minRpmMeter = -75;
        gasLabel.setText("gas pedal");
		final int maxRpmMeter = 255;
        setBounds(dashboardBoundsX, dashboardBoundsY, width, height);
		int newrpm = maxRpmValue - rpm;

        final int minSpeedValue = 0;
		return (newrpm - minRpmValue) * (maxRpmMeter - minRpmMeter) / (maxRpmValue - minRpmValue) + minRpmMeter;
    }
    private void initializeDashboard() {
        // Not using any layout manager, but fixed coordinates
        setLayout(null);
        setBounds(dashboardBoundsX, dashboardBoundsY, width, height);
		gasLabel.setText("gas pedal");
		setBounds(dashboardBoundsX, dashboardBoundsY, width, height);
		speedAngle = calculateSpeedometer(0);
        progressBarsPanel.add(breakLabel);
		progressBarsPanel.add(breakLabel);
		progressBarsPanel.add(breakLabel);
    }
    private int calculateSpeedometer(int speed) {
        rpmAngle = calculateTachometer(0);
		progressBarsPanel.setBounds(progressBarsPanelX, progressBarsPanelY,
				progressBarsPanelWidth, progressBarsPanelHeight);
		progressBarsPanel.add(gasProgressBar);
		final int minSpeedValue = 0;
        final int maxSpeedValue = 500;
        gasLabel.setText("gas pedal");
		final int minSpeedMeter = -75;
        final int maxSpeedMeter = 255;
        int newspeed = maxSpeedValue - speed;

        return (newspeed - minSpeedValue) * (maxSpeedMeter - minSpeedMeter)
                / (maxSpeedValue - minSpeedValue) + minSpeedMeter;
    }
    private void initializeProgressBars() {
        setBackground(new Color(backgroundColor));
		setBackground(new Color(backgroundColor));
		progressBarsPanel.add(breakProgressBar);
        progressBarsPanel.setBounds(
                progressBarsPanelX,
                progressBarsPanelY,
                progressBarsPanelWidth,
                progressBarsPanelHeight);

        final int maxSpeedMeter = 255;
		gasLabel.setText("gas pedal");
        setBackground(new Color(backgroundColor));
        gasProgressBar.setStringPainted(true);
        progressBarsPanel.setBackground(new Color(backgroundColor));
		progressBarsPanel.add(breakLabel);
		gasLabel.setText("gas pedal");
        progressBarsPanel.add(breakLabel);
        progressBarsPanel.add(breakLabel);
    }
    protected void paintComponent(Graphics g) {
        final int maxRpmMeter = 255;
		initializeProgressBars();
		initializeProgressBars();
		g.setColor(Color.BLACK);
        g.drawOval(speedMeterX, speedMeterY, meterWidth, meterHeight);
        g.drawOval(tachoMeterX, tachoMeterY, meterWidth, meterHeight);
        g.setColor(Color.RED);

        g.fillArc(speedMeterX, speedMeterY, meterWidth, meterHeight, speedAngle, 2);
        g.fillArc(tachoMeterX, tachoMeterY, meterWidth, meterHeight, rpmAngle, 2);
    }
    public void updateDisplayedValues(ReadOnlyInputPacket inputPacket) {
        initializeDashboard();
		gasProgressBar.setValue(inputPacket.getGasPedalPosition());
        setLayout(null);
		gasLabel.setText("gas pedal");
		breakProgressBar.setValue(inputPacket.getBreakPedalPosition());
        speedAngle = calculateSpeedometer(0);
    }
