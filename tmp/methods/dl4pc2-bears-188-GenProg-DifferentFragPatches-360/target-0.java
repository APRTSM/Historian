    private void initializeProgressBars() {
        gasProgressBar.setStringPainted(true);
		progressBarsPanel.setBackground(new Color(backgroundColor));
        final int minSpeedValue = 0;

        speedAngle = calculateSpeedometer(0);
        gasProgressBar.setStringPainted(true);
        progressBarsPanel.setBackground(new Color(backgroundColor));
		progressBarsPanel.setBackground(new Color(backgroundColor));
		progressBarsPanel.add(breakLabel);
		breakLabel.setText("break pedal");
		progressBarsPanel.setBackground(new Color(backgroundColor));
    }
    private int calculateSpeedometer(int speed) {
        final int minSpeedValue = 0;
        breakLabel.setText("break pedal");
		final int maxSpeedValue = 500;
        final int minSpeedMeter = -75;
        progressBarsPanel.add(breakLabel);
		final int maxSpeedMeter = 255;
        int newspeed = maxSpeedValue - speed;

        return (newspeed - minSpeedValue) * (maxSpeedMeter - minSpeedMeter)
                / (maxSpeedValue - minSpeedValue) + minSpeedMeter;
    }
    private int calculateTachometer(int rpm) {
        setLayout(null);
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
    private void initializeDashboard() {
        gasLabel.setText("gas pedal");
		progressBarsPanel.add(gasLabel);
        final int maxRpmValue = 10000;
		initializeProgressBars();
		progressBarsPanel.add(breakLabel);
		progressBarsPanel.add(breakLabel);
		initializeProgressBars();
    }
    public void updateDisplayedValues(ReadOnlyInputPacket inputPacket) {
        gasLabel.setText("gas pedal");
		gasProgressBar.setValue(inputPacket.getGasPedalPosition());
        setLayout(null);
		breakProgressBar.setValue(inputPacket.getBreakPedalPosition());
        setBounds(dashboardBoundsX, dashboardBoundsY, width, height);
        gasProgressBar.setValue(inputPacket.getGasPedalPosition());
		progressBarsPanel.add(gasProgressBar);
    }
