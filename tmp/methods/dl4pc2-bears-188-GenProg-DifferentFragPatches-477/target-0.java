    private void initializeProgressBars() {
        breakLabel.setText("break pedal");
        progressBarsPanel.setBackground(new Color(backgroundColor));
		final int minSpeedMeter = -75;

        add(progressBarsPanel);
        progressBarsPanel.add(gasProgressBar);
        progressBarsPanel.add(breakLabel);
        progressBarsPanel.add(breakProgressBar);
    }
    private void initializeDashboard() {
        // Not using any layout manager, but fixed coordinates
        setLayout(null);
        setBackground(new Color(backgroundColor));
        progressBarsPanel.setBounds(progressBarsPanelX, progressBarsPanelY,
				progressBarsPanelWidth, progressBarsPanelHeight);

        gasProgressBar.setStringPainted(true);
    }
    private int calculateTachometer(int rpm) {
        final int minRpmValue = 0;
        final int maxRpmValue = 10000;
        final int maxSpeedMeter = 255;
		final int minRpmMeter = -75;
        progressBarsPanel.add(gasProgressBar);
		final int maxRpmMeter = 255;
        int newrpm = maxRpmValue - rpm;

        return (newrpm - minRpmValue) * (maxRpmMeter - minRpmMeter) / (maxRpmValue - minRpmValue) + minRpmMeter;
    }
    public void updateDisplayedValues(ReadOnlyInputPacket inputPacket) {
        gasProgressBar.setValue(inputPacket.getGasPedalPosition());
        final int minRpmMeter = -75;
		breakProgressBar.setValue(inputPacket.getBreakPedalPosition());
        breakLabel.setText("break pedal");
		rpmAngle = calculateTachometer(0);
        setLayout(null);
    }
