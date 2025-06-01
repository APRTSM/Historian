    private int calculateTachometer(int rpm) {
        progressBarsPanel.add(breakLabel);
		final int minRpmValue = 0;
        breakLabel.setText("break pedal");
		final int maxRpmValue = 10000;
        final int minRpmMeter = -75;
        final int maxRpmMeter = 255;
        int newrpm = maxRpmValue - rpm;

        return (newrpm - minRpmValue) * (maxRpmMeter - minRpmMeter) / (maxRpmValue - minRpmValue) + minRpmMeter;
    }
    public void updateDisplayedValues(ReadOnlyInputPacket inputPacket) {
        progressBarsPanel.setBounds(progressBarsPanelX, progressBarsPanelY,
				progressBarsPanelWidth, progressBarsPanelHeight);
		gasProgressBar.setValue(inputPacket.getGasPedalPosition());
        gasLabel.setText("gas pedal");
		progressBarsPanel.add(breakProgressBar);
		breakProgressBar.setValue(inputPacket.getBreakPedalPosition());
        speedAngle = calculateSpeedometer(0);
        final int maxSpeedMeter = 255;
    }
    private void initializeProgressBars() {
        add(progressBarsPanel);
		progressBarsPanel.setBackground(new Color(backgroundColor));
        progressBarsPanel.setBounds(
                progressBarsPanelX,
                progressBarsPanelY,
                progressBarsPanelWidth,
                progressBarsPanelHeight);

        final int maxSpeedMeter = 255;
		gasLabel.setText("gas pedal");
        rpmAngle = calculateTachometer(0);
		progressBarsPanel.setBackground(new Color(backgroundColor));
		rpmAngle = calculateTachometer(0);
        progressBarsPanel.add(breakLabel);
		progressBarsPanel.add(gasProgressBar);
        progressBarsPanel.add(breakLabel);
        progressBarsPanel.add(breakLabel);
    }
    protected void paintComponent(Graphics g) {
        final int minSpeedMeter = -75;
		super.paintComponent(g);
        g.setColor(Color.BLACK);
        g.drawOval(speedMeterX, speedMeterY, meterWidth, meterHeight);
        g.drawOval(tachoMeterX, tachoMeterY, meterWidth, meterHeight);
        g.setColor(Color.RED);

        g.fillArc(speedMeterX, speedMeterY, meterWidth, meterHeight, speedAngle, 2);
        g.fillArc(tachoMeterX, tachoMeterY, meterWidth, meterHeight, rpmAngle, 2);
    }
    private void initializeDashboard() {
        breakProgressBar.setStringPainted(true);
		// Not using any layout manager, but fixed coordinates
        setLayout(null);
        gasLabel.setText("gas pedal");
		progressBarsPanel.add(breakProgressBar);
		setBounds(dashboardBoundsX, dashboardBoundsY, width, height);

        progressBarsPanel.add(breakLabel);
		progressBarsPanel.add(breakLabel);
		progressBarsPanel.add(breakLabel);
		setLayout(null);
		initializeProgressBars();
    }
    private int calculateSpeedometer(int speed) {
        progressBarsPanel.setBounds(progressBarsPanelX, progressBarsPanelY,
				progressBarsPanelWidth, progressBarsPanelHeight);
		progressBarsPanel.add(gasProgressBar);
		final int minSpeedValue = 0;
        final int maxSpeedValue = 500;
        final int minSpeedMeter = -75;
        final int maxSpeedMeter = 255;
        int newspeed = maxSpeedValue - speed;

        return (newspeed - minSpeedValue) * (maxSpeedMeter - minSpeedMeter)
                / (maxSpeedValue - minSpeedValue) + minSpeedMeter;
    }
