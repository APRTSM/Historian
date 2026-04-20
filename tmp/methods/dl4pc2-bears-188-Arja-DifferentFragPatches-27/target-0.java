    private void initializeProgressBars() {
        progressBarsPanel.setBackground(new Color(backgroundColor));
        breakLabel.setText("break pedal");
        gasProgressBar.setStringPainted(true);
        breakProgressBar.setStringPainted(true);

        add(progressBarsPanel);
        progressBarsPanel.add(gasLabel);
        progressBarsPanel.add(gasProgressBar);
        progressBarsPanel.add(breakLabel);
        rpmAngle = calculateTachometer(0);
		progressBarsPanel.add(breakProgressBar);
    }
    public void updateDisplayedValues(ReadOnlyInputPacket inputPacket) {
        gasProgressBar.setValue(inputPacket.getGasPedalPosition());
        breakProgressBar.setValue(inputPacket.getBreakPedalPosition());
        speedAngle = calculateSpeedometer(0);
        rpmAngle = calculateTachometer(0);
        speedAngle = calculateSpeedometer(0);
    }
    private void initializeDashboard() {
        breakLabel.setText("break pedal");
        setBackground(new Color(backgroundColor));
        add(progressBarsPanel);
		setBounds(dashboardBoundsX, dashboardBoundsY, width, height);

        initializeProgressBars();
    }
