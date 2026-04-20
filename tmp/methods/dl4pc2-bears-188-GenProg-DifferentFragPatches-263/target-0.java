    private int calculateSpeedometer(int speed) {
        final int minSpeedValue = 0;
        gasProgressBar.setStringPainted(true);
		final int maxSpeedValue = 500;
        final int minSpeedMeter = -75;
        gasLabel.setText("gas pedal");
		final int maxSpeedMeter = 255;
        int newspeed = maxSpeedValue - speed;

        return (newspeed - minSpeedValue) * (maxSpeedMeter - minSpeedMeter)
                / (maxSpeedValue - minSpeedValue) + minSpeedMeter;
    }
    private void initializeDashboard() {
        // Not using any layout manager, but fixed coordinates
        setLayout(null);
        setBackground(new Color(backgroundColor));
        progressBarsPanel.add(breakLabel);
		progressBarsPanel.add(breakLabel);
		progressBarsPanel.add(breakLabel);
    }
    private void initializeProgressBars() {
        progressBarsPanel.setBackground(new Color(backgroundColor));
        progressBarsPanel.setBounds(
                progressBarsPanelX,
                progressBarsPanelY,
                progressBarsPanelWidth,
                progressBarsPanelHeight);

        gasLabel.setText("gas pedal");
        gasProgressBar.setStringPainted(true);
        breakProgressBar.setStringPainted(true);

        add(progressBarsPanel);
        progressBarsPanel.add(gasLabel);
        progressBarsPanel.add(gasProgressBar);
        breakLabel.setText("break pedal");
        progressBarsPanel.add(breakProgressBar);
    }
    public void updateDisplayedValues(ReadOnlyInputPacket inputPacket) {
        gasProgressBar.setValue(inputPacket.getGasPedalPosition());
        setLayout(null);
		breakProgressBar.setValue(inputPacket.getBreakPedalPosition());
        speedAngle = calculateSpeedometer(0);
        rpmAngle = calculateTachometer(0);
        setLayout(null);
    }
