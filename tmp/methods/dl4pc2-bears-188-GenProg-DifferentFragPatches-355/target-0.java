    private int calculateTachometer(int rpm) {
        breakLabel.setText("break pedal");
		final int minRpmValue = 0;
        breakProgressBar.setStringPainted(true);
		final int maxRpmValue = 10000;
        final int minRpmMeter = -75;
        final int maxRpmMeter = 255;
        setBackground(new Color(backgroundColor));
		setBounds(dashboardBoundsX, dashboardBoundsY, width, height);
		int newrpm = maxRpmValue - rpm;

        breakLabel.setText("break pedal");
		return (newrpm - minRpmValue) * (maxRpmMeter - minRpmMeter) / (maxRpmValue - minRpmValue) + minRpmMeter;
    }
    public void updateDisplayedValues(ReadOnlyInputPacket inputPacket) {
        final int maxSpeedValue = 500;
		gasLabel.setText("gas pedal");
		gasProgressBar.setValue(inputPacket.getGasPedalPosition());
        setLayout(null);
		breakProgressBar.setValue(inputPacket.getBreakPedalPosition());
        setBounds(dashboardBoundsX, dashboardBoundsY, width, height);
        gasProgressBar.setValue(inputPacket.getGasPedalPosition());
		progressBarsPanel.add(gasProgressBar);
    }
    private void initializeProgressBars() {
        gasProgressBar.setStringPainted(true);
		gasProgressBar.setStringPainted(true);
        final int minSpeedValue = 0;

        speedAngle = calculateSpeedometer(0);
        gasProgressBar.setStringPainted(true);
        progressBarsPanel.setBackground(new Color(backgroundColor));
		progressBarsPanel.setBackground(new Color(backgroundColor));
		setBounds(dashboardBoundsX, dashboardBoundsY, width, height);
        gasLabel.setText("gas pedal");
		breakLabel.setText("break pedal");
		progressBarsPanel.setBackground(new Color(backgroundColor));
    }
    private int calculateSpeedometer(int speed) {
        final int minSpeedValue = 0;
        breakLabel.setText("break pedal");
		final int maxSpeedValue = 500;
        setBounds(dashboardBoundsX, dashboardBoundsY, width, height);
		final int minSpeedMeter = -75;
        progressBarsPanel.add(breakLabel);
		final int maxSpeedMeter = 255;
        int newspeed = maxSpeedValue - speed;

        return (newspeed - minSpeedValue) * (maxSpeedMeter - minSpeedMeter)
                / (maxSpeedValue - minSpeedValue) + minSpeedMeter;
    }
    protected void paintComponent(Graphics g) {
        g.setColor(Color.BLACK);
        g.drawOval(speedMeterX, speedMeterY, meterWidth, meterHeight);
        g.drawOval(tachoMeterX, tachoMeterY, meterWidth, meterHeight);
        g.setColor(Color.RED);

        g.fillArc(speedMeterX, speedMeterY, meterWidth, meterHeight, speedAngle, 2);
        g.fillArc(tachoMeterX, tachoMeterY, meterWidth, meterHeight, rpmAngle, 2);
    }
    private void initializeDashboard() {
        breakLabel.setText("break pedal");
        gasLabel.setText("gas pedal");
		progressBarsPanel.add(gasLabel);
        progressBarsPanel.add(gasLabel);
		progressBarsPanel.add(breakLabel);
		progressBarsPanel.add(breakLabel);
		initializeProgressBars();
    }
