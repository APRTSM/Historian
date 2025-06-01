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
    private void initializeDashboard() {
        progressBarsPanel.add(breakProgressBar);
		setBackground(new Color(backgroundColor));
        progressBarsPanel.add(breakLabel);
		final int maxRpmMeter = 255;
    }
    public void updateDisplayedValues(ReadOnlyInputPacket inputPacket) {
        gasProgressBar.setValue(inputPacket.getGasPedalPosition());
        gasLabel.setText("gas pedal");
		breakProgressBar.setValue(inputPacket.getBreakPedalPosition());
        add(progressBarsPanel);
        setLayout(null);
    }
    private void initializeProgressBars() {
        progressBarsPanel.setBackground(new Color(backgroundColor));
        gasLabel.setText("gas pedal");
        progressBarsPanel.add(breakProgressBar);
        final int minSpeedValue = 0;

        add(progressBarsPanel);
        progressBarsPanel.add(gasProgressBar);
        breakProgressBar.setStringPainted(true);
        progressBarsPanel.add(breakLabel);
    }
    private int calculateTachometer(int rpm) {
        setLayout(null);
		setLayout(null);
		final int minRpmValue = 0;
        final int maxRpmValue = 10000;
        final int minRpmMeter = -75;
        final int maxRpmMeter = 255;
        setBackground(new Color(backgroundColor));
		setBounds(dashboardBoundsX, dashboardBoundsY, width, height);
		int newrpm = maxRpmValue - rpm;

        gasLabel.setText("gas pedal");
		return (newrpm - minRpmValue) * (maxRpmMeter - minRpmMeter) / (maxRpmValue - minRpmValue) + minRpmMeter;
    }
    private int calculateSpeedometer(int speed) {
        progressBarsPanel.add(gasProgressBar);
		progressBarsPanel.add(gasProgressBar);
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
