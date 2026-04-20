    public void updateDisplayedValues(ReadOnlyInputPacket inputPacket) {
        gasProgressBar.setValue(inputPacket.getGasPedalPosition());
        setLayout(null);
		setLayout(null);
		breakProgressBar.setValue(inputPacket.getBreakPedalPosition());
        speedAngle = calculateSpeedometer(0);
        rpmAngle = calculateTachometer(0);
        progressBarsPanel.setBounds(progressBarsPanelX, progressBarsPanelY,
				progressBarsPanelWidth, progressBarsPanelHeight);
    }
    private void initializeDashboard() {
        // Not using any layout manager, but fixed coordinates
        setLayout(null);
        setBounds(dashboardBoundsX, dashboardBoundsY, width, height);
		gasLabel.setText("gas pedal");
		setBackground(new Color(backgroundColor));
        initializeProgressBars();
    }
    private void initializeProgressBars() {
        progressBarsPanel.setBackground(new Color(backgroundColor));
        progressBarsPanel.setBounds(
                progressBarsPanelX,
                progressBarsPanelY,
                progressBarsPanelWidth,
                progressBarsPanelHeight);

        gasLabel.setText("gas pedal");
        breakLabel.setText("break pedal");
        gasProgressBar.setStringPainted(true);
        breakProgressBar.setStringPainted(true);

        progressBarsPanel.add(gasLabel);
        progressBarsPanel.add(breakLabel);
        progressBarsPanel.add(breakProgressBar);
    }
