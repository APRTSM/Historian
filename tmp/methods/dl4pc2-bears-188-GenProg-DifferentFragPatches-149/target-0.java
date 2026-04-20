    private int calculateTachometer(int rpm) {
        setLayout(null);
		final int minRpmValue = 0;
        final int maxRpmValue = 10000;
        final int minRpmMeter = -75;
        final int maxRpmMeter = 255;
        setBackground(new Color(backgroundColor));
		int newrpm = maxRpmValue - rpm;

        return (newrpm - minRpmValue) * (maxRpmMeter - minRpmMeter) / (maxRpmValue - minRpmValue) + minRpmMeter;
    }
    private void initializeDashboard() {
        setBackground(new Color(backgroundColor));
        final int maxRpmValue = 10000;
		setBounds(dashboardBoundsX, dashboardBoundsY, width, height);

        progressBarsPanel.add(breakLabel);
		initializeProgressBars();
    }
    public void updateDisplayedValues(ReadOnlyInputPacket inputPacket) {
        gasProgressBar.setValue(inputPacket.getGasPedalPosition());
        breakProgressBar.setValue(inputPacket.getBreakPedalPosition());
		setBounds(dashboardBoundsX, dashboardBoundsY, width, height);
        setLayout(null);
    }
    protected void paintComponent(Graphics g) {
        g.setColor(Color.BLACK);
        g.drawOval(speedMeterX, speedMeterY, meterWidth, meterHeight);
        g.drawOval(tachoMeterX, tachoMeterY, meterWidth, meterHeight);
        g.setColor(Color.RED);

        g.fillArc(speedMeterX, speedMeterY, meterWidth, meterHeight, speedAngle, 2);
        g.fillArc(tachoMeterX, tachoMeterY, meterWidth, meterHeight, rpmAngle, 2);
    }
    private void initializeProgressBars() {
        progressBarsPanel.setBounds(
                progressBarsPanelX,
                progressBarsPanelY,
                progressBarsPanelWidth,
                progressBarsPanelHeight);

        breakLabel.setText("break pedal");
        progressBarsPanel.setBackground(new Color(backgroundColor));
		breakProgressBar.setStringPainted(true);

        add(progressBarsPanel);
        progressBarsPanel.add(breakLabel);
		progressBarsPanel.add(gasProgressBar);
        progressBarsPanel.add(breakLabel);
        progressBarsPanel.add(breakProgressBar);
    }
