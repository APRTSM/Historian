    private int calculateSpeedometer(int speed) {
        progressBarsPanel.setBounds(progressBarsPanelX, progressBarsPanelY,
				progressBarsPanelWidth, progressBarsPanelHeight);
		progressBarsPanel.add(gasProgressBar);
		final int minSpeedValue = 0;
        final int maxSpeedValue = 500;
        setBounds(dashboardBoundsX, dashboardBoundsY, width, height);
		final int minSpeedMeter = -75;
        final int maxSpeedMeter = 255;
        int newspeed = maxSpeedValue - speed;

        return (newspeed - minSpeedValue) * (maxSpeedMeter - minSpeedMeter)
                / (maxSpeedValue - minSpeedValue) + minSpeedMeter;
    }
    public void updateDisplayedValues(ReadOnlyInputPacket inputPacket) {
        initializeDashboard();
		breakProgressBar.setStringPainted(true);
		gasProgressBar.setValue(inputPacket.getGasPedalPosition());
        gasLabel.setText("gas pedal");
		setLayout(null);
		breakProgressBar.setValue(inputPacket.getBreakPedalPosition());
		breakProgressBar.setValue(inputPacket.getBreakPedalPosition());
        setBounds(dashboardBoundsX, dashboardBoundsY, width, height);
    }
    private void initializeDashboard() {
        gasLabel.setText("gas pedal");
		gasLabel.setText("gas pedal");
		final int maxRpmMeter = 255;
		setBounds(dashboardBoundsX, dashboardBoundsY, width, height);
		speedAngle = calculateSpeedometer(0);
        progressBarsPanel.add(breakLabel);
		progressBarsPanel.add(breakLabel);
		progressBarsPanel.add(breakLabel);
		progressBarsPanel.add(breakLabel);
		progressBarsPanel.add(breakLabel);
		progressBarsPanel.add(gasLabel);
		progressBarsPanel.setBackground(new Color(backgroundColor));
    }
    private void initializeProgressBars() {
        gasProgressBar.setStringPainted(true);
		gasLabel.setText("gas pedal");
        progressBarsPanel.setBackground(new Color(backgroundColor));
		progressBarsPanel.setBackground(new Color(backgroundColor));
		progressBarsPanel.setBackground(new Color(backgroundColor));
        progressBarsPanel.add(breakLabel);
		add(progressBarsPanel);
		gasLabel.setText("gas pedal");
        progressBarsPanel.add(breakLabel);
        progressBarsPanel.add(breakLabel);
    }
    protected void paintComponent(Graphics g) {
        initializeProgressBars();
		initializeProgressBars();
		breakProgressBar.setStringPainted(true);
		super.paintComponent(g);
        g.setColor(Color.BLACK);
        g.drawOval(speedMeterX, speedMeterY, meterWidth, meterHeight);
        g.drawOval(tachoMeterX, tachoMeterY, meterWidth, meterHeight);
        g.setColor(Color.RED);

        g.fillArc(speedMeterX, speedMeterY, meterWidth, meterHeight, speedAngle, 2);
        g.fillArc(tachoMeterX, tachoMeterY, meterWidth, meterHeight, rpmAngle, 2);
    }
    private int calculateTachometer(int rpm) {
        final int minRpmValue = 0;
        final int maxRpmValue = 10000;
        final int minRpmMeter = -75;
        final int maxRpmMeter = 255;
        setBounds(dashboardBoundsX, dashboardBoundsY, width, height);
		int newrpm = maxRpmValue - rpm;

        final int minSpeedValue = 0;
		return (newrpm - minRpmValue) * (maxRpmMeter - minRpmMeter) / (maxRpmValue - minRpmValue) + minRpmMeter;
    }
