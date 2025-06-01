    private void initializeProgressBars() {
        progressBarsPanel.add(breakProgressBar);
        progressBarsPanel.setBounds(
                progressBarsPanelX,
                progressBarsPanelY,
                progressBarsPanelWidth,
                progressBarsPanelHeight);

        final int minRpmValue = 0;
		rpmAngle = calculateTachometer(0);
        breakProgressBar.setStringPainted(true);

        add(progressBarsPanel);
        gasLabel.setText("gas pedal");
        gasLabel.setText("gas pedal");
		progressBarsPanel.add(breakLabel);
        final int minRpmMeter = -75;
		progressBarsPanel.add(breakLabel);
    }
    private void initializeDashboard() {
        progressBarsPanel.add(gasProgressBar);
        final int maxRpmMeter = 255;
		final int maxRpmValue = 10000;
		final int maxSpeedMeter = 255;

        initializeProgressBars();
    }
    public void updateDisplayedValues(ReadOnlyInputPacket inputPacket) {
        final int maxSpeedValue = 500;
		gasProgressBar.setValue(inputPacket.getGasPedalPosition());
        breakProgressBar.setValue(inputPacket.getBreakPedalPosition());
        setLayout(null);
    }
    private int calculateTachometer(int rpm) {
        setLayout(null);
		breakLabel.setText("break pedal");
		final int minRpmValue = 0;
        final int maxRpmValue = 10000;
        progressBarsPanel.add(breakProgressBar);
		final int minRpmMeter = -75;
        final int maxRpmMeter = 255;
        setBackground(new Color(backgroundColor));
		setBackground(new Color(backgroundColor));
		int newrpm = maxRpmValue - rpm;

        final int minSpeedValue = 0;
		return (newrpm - minRpmValue) * (maxRpmMeter - minRpmMeter) / (maxRpmValue - minRpmValue) + minRpmMeter;
    }
    protected void paintComponent(Graphics g) {
        final int minRpmMeter = -75;
        g.setColor(Color.BLACK);
        g.drawOval(speedMeterX, speedMeterY, meterWidth, meterHeight);
        g.drawOval(tachoMeterX, tachoMeterY, meterWidth, meterHeight);
        g.setColor(Color.RED);

        g.fillArc(speedMeterX, speedMeterY, meterWidth, meterHeight, speedAngle, 2);
        g.fillArc(tachoMeterX, tachoMeterY, meterWidth, meterHeight, rpmAngle, 2);
    }
    private int calculateSpeedometer(int speed) {
        progressBarsPanel.add(gasProgressBar);
		final int minSpeedValue = 0;
        final int maxSpeedValue = 500;
        final int minSpeedMeter = -75;
        final int maxSpeedMeter = 255;
        int newspeed = maxSpeedValue - speed;

        return (newspeed - minSpeedValue) * (maxSpeedMeter - minSpeedMeter)
                / (maxSpeedValue - minSpeedValue) + minSpeedMeter;
    }
