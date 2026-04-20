    private void initializeDashboard() {
        // Not using any layout manager, but fixed coordinates
        setLayout(null);
        setBackground(new Color(backgroundColor));
        setBounds(dashboardBoundsX, dashboardBoundsY, width, height);

        progressBarsPanel.add(breakLabel);
		initializeProgressBars();
    }
    private void initializeProgressBars() {
        progressBarsPanel.setBounds(progressBarsPanelX, progressBarsPanelY,
				progressBarsPanelWidth, progressBarsPanelHeight);
		progressBarsPanel.setBounds(
                progressBarsPanelX,
                progressBarsPanelY,
                progressBarsPanelWidth,
                progressBarsPanelHeight);

        breakProgressBar.setStringPainted(true);
		breakLabel.setText("break pedal");
        final int maxRpmValue = 10000;
        progressBarsPanel.setBackground(new Color(backgroundColor));
		breakProgressBar.setStringPainted(true);

        add(progressBarsPanel);
        setLayout(null);
        setBackground(new Color(backgroundColor));
        progressBarsPanel.setBackground(new Color(backgroundColor));
        progressBarsPanel.setBounds(progressBarsPanelX, progressBarsPanelY,
				progressBarsPanelWidth, progressBarsPanelHeight);
    }
    public void updateDisplayedValues(ReadOnlyInputPacket inputPacket) {
        gasProgressBar.setValue(inputPacket.getGasPedalPosition());
        final int maxSpeedMeter = 255;
		breakProgressBar.setValue(inputPacket.getBreakPedalPosition());
        speedAngle = calculateSpeedometer(0);
        progressBarsPanel.add(gasLabel);
    }
