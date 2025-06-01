    private int calculateSpeedometer(int speed) {
        progressBarsPanel.add(gasProgressBar);
		progressBarsPanel.add(gasProgressBar);
		progressBarsPanel.add(gasProgressBar);
		progressBarsPanel.setBounds(progressBarsPanelX, progressBarsPanelY,
				progressBarsPanelWidth, progressBarsPanelHeight);
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
    public void updateDisplayedValues(ReadOnlyInputPacket inputPacket) {
        gasProgressBar.setValue(inputPacket.getGasPedalPosition());
        gasLabel.setText("gas pedal");
		progressBarsPanel.add(breakProgressBar);
		setLayout(null);
		progressBarsPanel.add(breakProgressBar);
		breakProgressBar.setValue(inputPacket.getBreakPedalPosition());
        progressBarsPanel.add(gasLabel);
		progressBarsPanel.add(gasLabel);
    }
    private void initializeDashboard() {
        gasLabel.setText("gas pedal");
		progressBarsPanel.add(breakProgressBar);
		setBackground(new Color(backgroundColor));
        final int maxRpmValue = 10000;
		final int minSpeedMeter = -75;
		progressBarsPanel.add(breakLabel);
		progressBarsPanel.add(breakLabel);
		progressBarsPanel.add(breakLabel);
		progressBarsPanel.add(gasLabel);
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
    private void initializeProgressBars() {
        setBackground(new Color(backgroundColor));
		final int maxSpeedMeter = 255;
		final int maxRpmValue = 10000;
        initializeProgressBars();
		progressBarsPanel.setBackground(new Color(backgroundColor));
		final int minSpeedValue = 0;

        breakProgressBar.setStringPainted(true);
		gasLabel.setText("gas pedal");
        gasLabel.setText("gas pedal");
		progressBarsPanel.add(breakLabel);
        progressBarsPanel.add(breakLabel);
    }
    private int calculateTachometer(int rpm) {
        setLayout(null);
		setLayout(null);
		progressBarsPanel.add(breakLabel);
		breakLabel.setText("break pedal");
		final int minRpmValue = 0;
        gasLabel.setText("gas pedal");
		final int maxRpmValue = 10000;
        final int minRpmMeter = -75;
        gasLabel.setText("gas pedal");
		final int maxRpmMeter = 255;
        setBackground(new Color(backgroundColor));
		setBounds(dashboardBoundsX, dashboardBoundsY, width, height);
		setBounds(dashboardBoundsX, dashboardBoundsY, width, height);
		int newrpm = maxRpmValue - rpm;

        gasLabel.setText("gas pedal");
		initializeDashboard();
		return (newrpm - minRpmValue) * (maxRpmMeter - minRpmMeter) / (maxRpmValue - minRpmValue) + minRpmMeter;
    }
