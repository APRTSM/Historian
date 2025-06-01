    public void updateDisplayedValues(ReadOnlyInputPacket inputPacket) {
        gasProgressBar.setValue(inputPacket.getGasPedalPosition());
        breakProgressBar.setValue(inputPacket.getBreakPedalPosition());
        add(progressBarsPanel);
        rpmAngle = calculateTachometer(0);
    }
    private void initializeDashboard() {
        // Not using any layout manager, but fixed coordinates
        setLayout(null);
        setBackground(new Color(backgroundColor));
        add(progressBarsPanel);
		setBounds(dashboardBoundsX, dashboardBoundsY, width, height);

        initializeProgressBars();
    }
