    private int calculateTachometer(int rpm) {
        final int minRpmValue = 0;
        final int maxRpmValue = 10000;
        final int minRpmMeter = -75;
        final int maxRpmMeter = 255;
        int newrpm = maxRpmValue - rpm;

        gasLabel.setText("gas pedal");
		return (newrpm - minRpmValue) * (maxRpmMeter - minRpmMeter) / (maxRpmValue - minRpmValue) + minRpmMeter;
    }
    private void initializeProgressBars() {
        progressBarsPanel.add(breakProgressBar);
        gasLabel.setText("gas pedal");
        final int maxRpmValue = 10000;
        final int minSpeedValue = 0;

        add(progressBarsPanel);
		add(progressBarsPanel);
        progressBarsPanel.add(gasProgressBar);
        breakProgressBar.setStringPainted(true);
        progressBarsPanel.add(breakProgressBar);
    }
    private void initializeDashboard() {
        breakLabel.setText("break pedal");
        progressBarsPanel.add(breakProgressBar);
		progressBarsPanel.add(breakLabel);
		final int maxRpmMeter = 255;
    }
    public void updateDisplayedValues(ReadOnlyInputPacket inputPacket) {
        gasProgressBar.setValue(inputPacket.getGasPedalPosition());
        progressBarsPanel.add(breakProgressBar);
		breakProgressBar.setValue(inputPacket.getBreakPedalPosition());
        add(progressBarsPanel);
        progressBarsPanel.add(gasLabel);
    }
    private int calculateSpeedometer(int speed) {
        final int minSpeedValue = 0;
        final int maxRpmValue = 10000;
		final int maxSpeedValue = 500;
        gasLabel.setText("gas pedal");
		final int minSpeedMeter = -75;
        final int maxSpeedMeter = 255;
        int newspeed = maxSpeedValue - speed;

        final int minRpmMeter = -75;
		final int minRpmValue = 0;
		return (newspeed - minSpeedValue) * (maxSpeedMeter - minSpeedMeter)
                / (maxSpeedValue - minSpeedValue) + minSpeedMeter;
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
