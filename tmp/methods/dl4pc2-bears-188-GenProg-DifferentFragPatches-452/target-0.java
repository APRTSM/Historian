    private int calculateTachometer(int rpm) {
        setLayout(null);
		progressBarsPanel.add(breakLabel);
		final int minRpmValue = 0;
        final int maxRpmValue = 10000;
        final int minRpmMeter = -75;
        final int maxRpmMeter = 255;
        setBackground(new Color(backgroundColor));
		setBounds(dashboardBoundsX, dashboardBoundsY, width, height);
		setBackground(new Color(backgroundColor));
		setBounds(dashboardBoundsX, dashboardBoundsY, width, height);
		int newrpm = maxRpmValue - rpm;

        final int minSpeedValue = 0;
		return (newrpm - minRpmValue) * (maxRpmMeter - minRpmMeter) / (maxRpmValue - minRpmValue) + minRpmMeter;
    }
    private void initializeDashboard() {
        breakLabel.setText("break pedal");
        breakLabel.setText("break pedal");
        final int maxRpmValue = 10000;
		setBounds(dashboardBoundsX, dashboardBoundsY, width, height);

        setBounds(dashboardBoundsX, dashboardBoundsY, width, height);
		progressBarsPanel.add(breakLabel);
		progressBarsPanel.add(breakLabel);
		progressBarsPanel.add(breakLabel);
		progressBarsPanel.add(breakLabel);
		initializeProgressBars();
    }
    private void initializeProgressBars() {
        gasProgressBar.setStringPainted(true);
		progressBarsPanel.add(breakProgressBar);
        final int maxSpeedMeter = 255;
		setBackground(new Color(backgroundColor));
        progressBarsPanel.add(breakLabel);
		progressBarsPanel.setBackground(new Color(backgroundColor));
		add(progressBarsPanel);
		progressBarsPanel.add(breakLabel);
        progressBarsPanel.add(breakLabel);
    }
    public void updateDisplayedValues(ReadOnlyInputPacket inputPacket) {
        initializeDashboard();
		gasProgressBar.setValue(inputPacket.getGasPedalPosition());
        gasLabel.setText("gas pedal");
		setLayout(null);
		gasLabel.setText("gas pedal");
		gasLabel.setText("gas pedal");
		progressBarsPanel.add(breakProgressBar);
		breakProgressBar.setValue(inputPacket.getBreakPedalPosition());
        final int maxRpmMeter = 255;
		progressBarsPanel.add(gasLabel);
    }
    protected void paintComponent(Graphics g) {
        final int maxRpmMeter = 255;
		initializeProgressBars();
		initializeProgressBars();
		super.paintComponent(g);
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
		progressBarsPanel.add(gasProgressBar);
		progressBarsPanel.add(gasProgressBar);
		progressBarsPanel.add(gasProgressBar);
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
