    private int calculateTachometer(int rpm) {
        final int minRpmValue = 0;
        breakProgressBar.setStringPainted(true);
		final int maxRpmValue = 10000;
        final int minRpmMeter = -75;
        final int maxRpmMeter = 255;
        setBackground(new Color(backgroundColor));
		setBackground(new Color(backgroundColor));
		setBounds(dashboardBoundsX, dashboardBoundsY, width, height);
		int newrpm = maxRpmValue - rpm;

        breakLabel.setText("break pedal");
		return (newrpm - minRpmValue) * (maxRpmMeter - minRpmMeter) / (maxRpmValue - minRpmValue) + minRpmMeter;
    }
    private int calculateSpeedometer(int speed) {
        progressBarsPanel.add(gasProgressBar);
		progressBarsPanel.add(gasProgressBar);
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
    public void updateDisplayedValues(ReadOnlyInputPacket inputPacket) {
        gasLabel.setText("gas pedal");
		gasProgressBar.setValue(inputPacket.getGasPedalPosition());
        setLayout(null);
		breakProgressBar.setValue(inputPacket.getBreakPedalPosition());
        setBounds(dashboardBoundsX, dashboardBoundsY, width, height);
        gasProgressBar.setValue(inputPacket.getGasPedalPosition());
		progressBarsPanel.add(gasProgressBar);
    }
    private void initializeDashboard() {
        breakLabel.setText("break pedal");
        gasLabel.setText("gas pedal");
		progressBarsPanel.add(gasLabel);
        progressBarsPanel.add(breakLabel);
		progressBarsPanel.add(breakLabel);
		initializeProgressBars();
    }
    private void initializeProgressBars() {
        gasProgressBar.setStringPainted(true);
		progressBarsPanel.add(breakProgressBar);
        speedAngle = calculateSpeedometer(0);
        gasProgressBar.setStringPainted(true);
        progressBarsPanel.setBackground(new Color(backgroundColor));
		progressBarsPanel.setBackground(new Color(backgroundColor));
		breakLabel.setText("break pedal");
		progressBarsPanel.add(breakLabel);
		breakLabel.setText("break pedal");
		progressBarsPanel.setBackground(new Color(backgroundColor));
    }
