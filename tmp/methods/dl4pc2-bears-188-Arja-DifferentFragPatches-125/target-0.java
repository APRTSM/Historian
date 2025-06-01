    private void initializeDashboard() {
        // Not using any layout manager, but fixed coordinates
        setLayout(null);
        breakProgressBar.setStringPainted(true);
		setBackground(new Color(backgroundColor));
        setBounds(dashboardBoundsX, dashboardBoundsY, width, height);

        initializeProgressBars();
    }
    public void updateDisplayedValues(ReadOnlyInputPacket inputPacket) {
        gasProgressBar.setValue(inputPacket.getGasPedalPosition());
        breakProgressBar.setValue(inputPacket.getBreakPedalPosition());
        gasProgressBar.setValue(inputPacket.getGasPedalPosition());
		speedAngle = calculateSpeedometer(0);
        rpmAngle = calculateTachometer(0);
        add(progressBarsPanel);
    }
