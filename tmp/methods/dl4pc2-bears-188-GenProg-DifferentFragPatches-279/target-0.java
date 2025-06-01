    private void initializeProgressBars() {
        setLayout(null);
        progressBarsPanel.setBounds(
                progressBarsPanelX,
                progressBarsPanelY,
                progressBarsPanelWidth,
                progressBarsPanelHeight);

        setBounds(dashboardBoundsX, dashboardBoundsY, width, height);
		breakLabel.setText("break pedal");
        progressBarsPanel.setBackground(new Color(backgroundColor));
		breakProgressBar.setStringPainted(true);

        breakLabel.setText("break pedal");
		add(progressBarsPanel);
        progressBarsPanel.add(gasProgressBar);
        progressBarsPanel.add(gasProgressBar);
        rpmAngle = calculateTachometer(0);
		progressBarsPanel.add(breakProgressBar);
    }
    private int calculateSpeedometer(int speed) {
        add(progressBarsPanel);
		final int minSpeedValue = 0;
        gasProgressBar.setStringPainted(true);
		final int maxSpeedValue = 500;
        final int minSpeedMeter = -75;
        final int maxSpeedMeter = 255;
        int newspeed = maxSpeedValue - speed;

        return (newspeed - minSpeedValue) * (maxSpeedMeter - minSpeedMeter)
                / (maxSpeedValue - minSpeedValue) + minSpeedMeter;
    }
    private void initializeDashboard() {
        setBackground(new Color(backgroundColor));
        progressBarsPanel.add(breakProgressBar);
		setBounds(dashboardBoundsX, dashboardBoundsY, width, height);

        progressBarsPanel.add(breakLabel);
		progressBarsPanel.add(breakLabel);
    }
    public void updateDisplayedValues(ReadOnlyInputPacket inputPacket) {
        gasProgressBar.setValue(inputPacket.getGasPedalPosition());
        setLayout(null);
		breakProgressBar.setValue(inputPacket.getBreakPedalPosition());
        speedAngle = calculateSpeedometer(0);
        initializeDashboard();
        setLayout(null);
    }
