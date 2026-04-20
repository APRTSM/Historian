    private void initializeDashboard() {
        // Not using any layout manager, but fixed coordinates
        setLayout(null);
        setBackground(new Color(backgroundColor));
        setBounds(dashboardBoundsX, dashboardBoundsY, width, height);

        setBounds(dashboardBoundsX, dashboardBoundsY, width, height);
		initializeProgressBars();
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
        final int maxRpmMeter = 255;
		rpmAngle = calculateTachometer(0);
    }
    private void initializeProgressBars() {
        progressBarsPanel.setBackground(new Color(backgroundColor));
        breakLabel.setText("break pedal");
        breakProgressBar.setStringPainted(true);

        progressBarsPanel.add(breakLabel);
		add(progressBarsPanel);
        progressBarsPanel.add(gasLabel);
        progressBarsPanel.add(breakLabel);
		progressBarsPanel.add(gasProgressBar);
        progressBarsPanel.add(breakLabel);
        progressBarsPanel.add(breakProgressBar);
    }
