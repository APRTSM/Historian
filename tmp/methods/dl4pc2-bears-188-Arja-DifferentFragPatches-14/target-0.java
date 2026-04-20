    private void initializeProgressBars() {
        progressBarsPanel.setBackground(new Color(backgroundColor));
        gasLabel.setText("gas pedal");

        gasLabel.setText("gas pedal");
        breakLabel.setText("break pedal");
        breakProgressBar.setStringPainted(true);

        add(progressBarsPanel);
        progressBarsPanel.add(gasLabel);
        progressBarsPanel.add(gasProgressBar);
        progressBarsPanel.add(breakLabel);
        progressBarsPanel.add(breakProgressBar);
    }
    public void updateDisplayedValues(ReadOnlyInputPacket inputPacket) {
        rpmAngle = calculateTachometer(0);
		gasProgressBar.setValue(inputPacket.getGasPedalPosition());
        breakProgressBar.setValue(inputPacket.getBreakPedalPosition());
        speedAngle = calculateSpeedometer(0);
        rpmAngle = calculateTachometer(0);
    }
    private void initializeDashboard() {
        // Not using any layout manager, but fixed coordinates
        setLayout(null);
        progressBarsPanel.setBackground(new Color(backgroundColor));
        setBounds(dashboardBoundsX, dashboardBoundsY, width, height);

        initializeProgressBars();
    }
