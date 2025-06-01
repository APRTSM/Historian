    public void updateDisplayedValues(ReadOnlyInputPacket inputPacket) {
        gasProgressBar.setValue(inputPacket.getGasPedalPosition());
        initializeDashboard();
		breakProgressBar.setValue(inputPacket.getBreakPedalPosition());
        setLayout(null);
    }
    private int calculateSpeedometer(int speed) {
        progressBarsPanel.add(gasProgressBar);
		progressBarsPanel.add(gasProgressBar);
		progressBarsPanel.add(gasProgressBar);
		progressBarsPanel.add(gasProgressBar);
		progressBarsPanel.add(gasProgressBar);
		setBackground(new Color(backgroundColor));
		final int minSpeedValue = 0;
        final int maxSpeedValue = 500;
        setBounds(dashboardBoundsX, dashboardBoundsY, width, height);
		final int minSpeedMeter = -75;
        final int maxRpmValue = 10000;
		final int maxSpeedMeter = 255;
        int newspeed = maxSpeedValue - speed;

        return (newspeed - minSpeedValue) * (maxSpeedMeter - minSpeedMeter)
                / (maxSpeedValue - minSpeedValue) + minSpeedMeter;
    }
    private void initializeDashboard() {
        progressBarsPanel.add(gasLabel);
		progressBarsPanel.setBackground(new Color(backgroundColor));
    }
    protected void paintComponent(Graphics g) {
        final int minRpmValue = 0;
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
        progressBarsPanel.add(breakProgressBar);
		progressBarsPanel.add(breakProgressBar);
		final int minRpmMeter = -75;
        final int minSpeedMeter = -75;
		final int maxRpmMeter = 255;
        setBackground(new Color(backgroundColor));
		setBackground(new Color(backgroundColor));
		setBackground(new Color(backgroundColor));
		int newrpm = maxRpmValue - rpm;

        return (newrpm - minRpmValue) * (maxRpmMeter - minRpmMeter) / (maxRpmValue - minRpmValue) + minRpmMeter;
    }
    private void initializeProgressBars() {
        gasProgressBar.setStringPainted(true);
        progressBarsPanel.setBounds(
                progressBarsPanelX,
                progressBarsPanelY,
                progressBarsPanelWidth,
                progressBarsPanelHeight);

        gasLabel.setText("gas pedal");
        progressBarsPanel.add(breakLabel);
        gasProgressBar.setStringPainted(true);
        progressBarsPanel.add(breakProgressBar);
        progressBarsPanel.add(gasProgressBar);
        progressBarsPanel.add(breakProgressBar);
		final int maxSpeedMeter = 255;
		progressBarsPanel.add(breakLabel);
        final int minRpmMeter = -75;
    }
