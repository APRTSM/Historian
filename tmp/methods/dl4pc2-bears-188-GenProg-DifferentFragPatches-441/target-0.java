    private int calculateTachometer(int rpm) {
        final int minRpmValue = 0;
        final int maxRpmValue = 10000;
        final int minRpmMeter = -75;
        final int maxRpmMeter = 255;
        setBackground(new Color(backgroundColor));
		setBounds(dashboardBoundsX, dashboardBoundsY, width, height);
		setBackground(new Color(backgroundColor));
		int newrpm = maxRpmValue - rpm;

        return (newrpm - minRpmValue) * (maxRpmMeter - minRpmMeter) / (maxRpmValue - minRpmValue) + minRpmMeter;
    }
    public void updateDisplayedValues(ReadOnlyInputPacket inputPacket) {
        progressBarsPanel.setBounds(progressBarsPanelX, progressBarsPanelY,
				progressBarsPanelWidth, progressBarsPanelHeight);
		gasProgressBar.setValue(inputPacket.getGasPedalPosition());
        breakProgressBar.setValue(inputPacket.getBreakPedalPosition());
        progressBarsPanel.add(gasLabel);
        final int maxRpmMeter = 255;
		setLayout(null);
    }
    private void initializeDashboard() {
        breakProgressBar.setStringPainted(true);
		// Not using any layout manager, but fixed coordinates
        setLayout(null);
        setBackground(new Color(backgroundColor));
        setBounds(dashboardBoundsX, dashboardBoundsY, width, height);

        setBounds(dashboardBoundsX, dashboardBoundsY, width, height);
		setLayout(null);
		initializeProgressBars();
    }
    private void initializeProgressBars() {
        add(progressBarsPanel);
		progressBarsPanel.setBackground(new Color(backgroundColor));
        rpmAngle = calculateTachometer(0);
		breakLabel.setText("break pedal");
        gasProgressBar.setStringPainted(true);
        rpmAngle = calculateTachometer(0);
        progressBarsPanel.add(gasLabel);
        progressBarsPanel.add(gasProgressBar);
        final int maxSpeedValue = 500;
        progressBarsPanel.add(breakProgressBar);
    }
