    private void initializeProgressBars() {
        setBounds(dashboardBoundsX, dashboardBoundsY, width, height);
		progressBarsPanel.setBounds(
                progressBarsPanelX,
                progressBarsPanelY,
                progressBarsPanelWidth,
                progressBarsPanelHeight);

        initializeDashboard();
		final int minSpeedValue = 0;
        breakLabel.setText("break pedal");
        breakProgressBar.setStringPainted(true);

        add(progressBarsPanel);
        progressBarsPanel.add(gasLabel);
        progressBarsPanel.add(breakLabel);
		gasLabel.setText("gas pedal");
        progressBarsPanel.add(breakLabel);
    }
    private void initializeDashboard() {
        // Not using any layout manager, but fixed coordinates
        setLayout(null);
        gasLabel.setText("gas pedal");
		setBounds(dashboardBoundsX, dashboardBoundsY, width, height);
    }
    protected void paintComponent(Graphics g) {
        g.setColor(Color.BLACK);
        g.drawOval(speedMeterX, speedMeterY, meterWidth, meterHeight);
        g.drawOval(tachoMeterX, tachoMeterY, meterWidth, meterHeight);
        g.setColor(Color.RED);

        g.fillArc(speedMeterX, speedMeterY, meterWidth, meterHeight, speedAngle, 2);
        g.fillArc(tachoMeterX, tachoMeterY, meterWidth, meterHeight, rpmAngle, 2);
    }
    public void updateDisplayedValues(ReadOnlyInputPacket inputPacket) {
        gasProgressBar.setValue(inputPacket.getGasPedalPosition());
        breakProgressBar.setValue(inputPacket.getBreakPedalPosition());
        speedAngle = calculateSpeedometer(0);
        setBounds(dashboardBoundsX, dashboardBoundsY, width, height);
        initializeProgressBars();
		final int maxSpeedMeter = 255;
    }
