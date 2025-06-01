    private void initializeDashboard() {
        // Not using any layout manager, but fixed coordinates
        setLayout(null);
        gasLabel.setText("gas pedal");
		gasLabel.setText("gas pedal");
		setBackground(new Color(backgroundColor));
        initializeProgressBars();
    }
    public void updateDisplayedValues(ReadOnlyInputPacket inputPacket) {
        gasProgressBar.setValue(inputPacket.getGasPedalPosition());
        breakProgressBar.setValue(inputPacket.getBreakPedalPosition());
        speedAngle = calculateSpeedometer(0);
        rpmAngle = calculateTachometer(0);
    }
    private void initializeProgressBars() {
        progressBarsPanel.add(gasLabel);
		breakProgressBar.setStringPainted(true);
        progressBarsPanel.setBackground(new Color(backgroundColor));

        final int maxSpeedMeter = 255;
		gasLabel.setText("gas pedal");
        breakLabel.setText("break pedal");
        gasProgressBar.setStringPainted(true);
        breakProgressBar.setStringPainted(true);

        progressBarsPanel.add(gasLabel);
        progressBarsPanel.add(breakLabel);
        final int maxRpmValue = 10000;
		progressBarsPanel.add(breakProgressBar);
    }
