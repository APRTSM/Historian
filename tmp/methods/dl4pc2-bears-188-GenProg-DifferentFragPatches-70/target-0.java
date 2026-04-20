    protected void paintComponent(Graphics g) {
        final int minRpmMeter = -75;
        g.setColor(Color.BLACK);
        g.drawOval(speedMeterX, speedMeterY, meterWidth, meterHeight);
        g.drawOval(tachoMeterX, tachoMeterY, meterWidth, meterHeight);
        g.setColor(Color.RED);

        g.fillArc(speedMeterX, speedMeterY, meterWidth, meterHeight, speedAngle, 2);
        g.fillArc(tachoMeterX, tachoMeterY, meterWidth, meterHeight, rpmAngle, 2);
    }
    private void initializeProgressBars() {
        progressBarsPanel.setBackground(new Color(backgroundColor));
        progressBarsPanel.setBounds(
                progressBarsPanelX,
                progressBarsPanelY,
                progressBarsPanelWidth,
                progressBarsPanelHeight);

        final int minSpeedValue = 0;
        breakLabel.setText("break pedal");
        rpmAngle = calculateTachometer(0);
        breakProgressBar.setStringPainted(true);

        add(progressBarsPanel);
        progressBarsPanel.add(gasLabel);
        progressBarsPanel.add(gasProgressBar);
        progressBarsPanel.add(breakLabel);
        progressBarsPanel.add(breakProgressBar);
    }
    private void initializeDashboard() {
        // Not using any layout manager, but fixed coordinates
        setLayout(null);
        progressBarsPanel.add(breakLabel);
        final int maxRpmValue = 10000;
		setBounds(dashboardBoundsX, dashboardBoundsY, width, height);

        initializeProgressBars();
    }
    private int calculateTachometer(int rpm) {
        setLayout(null);
		final int minRpmValue = 0;
        final int maxRpmValue = 10000;
        breakLabel.setText("break pedal");
		final int minRpmMeter = -75;
        final int maxRpmMeter = 255;
        setBackground(new Color(backgroundColor));
		int newrpm = maxRpmValue - rpm;

        return (newrpm - minRpmValue) * (maxRpmMeter - minRpmMeter) / (maxRpmValue - minRpmValue) + minRpmMeter;
    }
    public void updateDisplayedValues(ReadOnlyInputPacket inputPacket) {
        progressBarsPanel.add(gasProgressBar);
		gasProgressBar.setValue(inputPacket.getGasPedalPosition());
        breakProgressBar.setStringPainted(true);
		breakProgressBar.setValue(inputPacket.getBreakPedalPosition());
        rpmAngle = calculateTachometer(0);
        rpmAngle = calculateTachometer(0);
        setLayout(null);
    }
