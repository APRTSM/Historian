    public void updateDisplayedValues(ReadOnlyInputPacket inputPacket) {
        final int maxSpeedValue = 500;
		gasProgressBar.setValue(inputPacket.getGasPedalPosition());
        final int maxRpmMeter = 255;
		breakProgressBar.setValue(inputPacket.getBreakPedalPosition());
        rpmAngle = calculateTachometer(0);
    }
    private void initializeProgressBars() {
        gasProgressBar.setStringPainted(true);
        progressBarsPanel.setBounds(
                progressBarsPanelX,
                progressBarsPanelY,
                progressBarsPanelWidth,
                progressBarsPanelHeight);

        progressBarsPanel.add(breakProgressBar);
        gasProgressBar.setStringPainted(true);
        breakProgressBar.setStringPainted(true);

        progressBarsPanel.setBackground(new Color(backgroundColor));
        setBounds(dashboardBoundsX, dashboardBoundsY, width, height);
		gasLabel.setText("gas pedal");
        gasLabel.setText("gas pedal");
		progressBarsPanel.add(breakLabel);
        final int minRpmMeter = -75;
    }
    private int calculateSpeedometer(int speed) {
        progressBarsPanel.add(gasProgressBar);
		progressBarsPanel.add(gasProgressBar);
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
    private int calculateTachometer(int rpm) {
        setLayout(null);
		breakLabel.setText("break pedal");
		final int minRpmValue = 0;
        final int maxRpmValue = 10000;
        final int minRpmMeter = -75;
        final int maxRpmMeter = 255;
        int newrpm = maxRpmValue - rpm;

        return (newrpm - minRpmValue) * (maxRpmMeter - minRpmMeter) / (maxRpmValue - minRpmValue) + minRpmMeter;
    }
    private void initializeDashboard() {
        setBackground(new Color(backgroundColor));
        final int maxRpmValue = 10000;
		setBounds(dashboardBoundsX, dashboardBoundsY, width, height);

        progressBarsPanel.add(gasLabel);
		progressBarsPanel.setBackground(new Color(backgroundColor));
    }
