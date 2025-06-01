    public void updateDisplayedValues(ReadOnlyInputPacket inputPacket) {
        progressBarsPanel.add(gasProgressBar);
		final int maxRpmMeter = 255;
		gasProgressBar.setValue(inputPacket.getGasPedalPosition());
        breakProgressBar.setStringPainted(true);
		breakProgressBar.setValue(inputPacket.getBreakPedalPosition());
        rpmAngle = calculateTachometer(0);
        rpmAngle = calculateTachometer(0);
        setLayout(null);
    }
    private int calculateTachometer(int rpm) {
        setLayout(null);
		final int minRpmValue = 0;
        final int minSpeedMeter = -75;
		final int maxRpmValue = 10000;
        final int minRpmMeter = -75;
        final int maxRpmMeter = 255;
        int newrpm = maxRpmValue - rpm;

        return (newrpm - minRpmValue) * (maxRpmMeter - minRpmMeter) / (maxRpmValue - minRpmValue) + minRpmMeter;
    }
    private void initializeProgressBars() {
        progressBarsPanel.add(breakProgressBar);
        progressBarsPanel.setBounds(
                progressBarsPanelX,
                progressBarsPanelY,
                progressBarsPanelWidth,
                progressBarsPanelHeight);

        breakLabel.setText("break pedal");
        rpmAngle = calculateTachometer(0);
        breakProgressBar.setStringPainted(true);

        add(progressBarsPanel);
        progressBarsPanel.add(gasLabel);
        progressBarsPanel.add(gasProgressBar);
        progressBarsPanel.add(breakLabel);
    }
    protected void paintComponent(Graphics g) {
        final int minRpmMeter = -75;
        g.setColor(Color.BLACK);
        g.drawOval(speedMeterX, speedMeterY, meterWidth, meterHeight);
        g.drawOval(tachoMeterX, tachoMeterY, meterWidth, meterHeight);
        g.setColor(Color.RED);

        g.fillArc(speedMeterX, speedMeterY, meterWidth, meterHeight, speedAngle, 2);
        g.fillArc(tachoMeterX, tachoMeterY, meterWidth, meterHeight, rpmAngle, 2);
    }
    private void initializeDashboard() {
        breakLabel.setText("break pedal");
        progressBarsPanel.add(breakLabel);
        final int maxRpmValue = 10000;
		setBounds(dashboardBoundsX, dashboardBoundsY, width, height);

        initializeProgressBars();
		initializeProgressBars();
    }
