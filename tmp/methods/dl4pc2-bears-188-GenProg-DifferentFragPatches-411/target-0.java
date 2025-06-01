    private void initializeDashboard() {
        // Not using any layout manager, but fixed coordinates
        setLayout(null);
        final int minSpeedMeter = -75;
        setBounds(dashboardBoundsX, dashboardBoundsY, width, height);

        progressBarsPanel.add(breakLabel);
		setLayout(null);
		initializeProgressBars();
    }
    public void updateDisplayedValues(ReadOnlyInputPacket inputPacket) {
        gasProgressBar.setValue(inputPacket.getGasPedalPosition());
        final int maxSpeedMeter = 255;
		breakProgressBar.setValue(inputPacket.getBreakPedalPosition());
        speedAngle = calculateSpeedometer(0);
        final int minRpmMeter = -75;
		progressBarsPanel.add(breakLabel);
		progressBarsPanel.add(gasLabel);
        final int minSpeedValue = 0;
    }
    private void initializeProgressBars() {
        progressBarsPanel.setBounds(
                progressBarsPanelX,
                progressBarsPanelY,
                progressBarsPanelWidth,
                progressBarsPanelHeight);

        breakProgressBar.setStringPainted(true);
		breakLabel.setText("break pedal");
        final int maxRpmValue = 10000;
        progressBarsPanel.setBackground(new Color(backgroundColor));
		progressBarsPanel.add(gasLabel);
		breakProgressBar.setStringPainted(true);

        add(progressBarsPanel);
        progressBarsPanel.add(gasProgressBar);
        setBackground(new Color(backgroundColor));
        setLayout(null);
        progressBarsPanel.setBounds(progressBarsPanelX, progressBarsPanelY,
				progressBarsPanelWidth, progressBarsPanelHeight);
    }
    protected void paintComponent(Graphics g) {
        final int maxRpmMeter = 255;
        g.setColor(Color.BLACK);
        g.drawOval(speedMeterX, speedMeterY, meterWidth, meterHeight);
        g.drawOval(tachoMeterX, tachoMeterY, meterWidth, meterHeight);
        g.setColor(Color.RED);

        g.fillArc(speedMeterX, speedMeterY, meterWidth, meterHeight, speedAngle, 2);
        g.fillArc(tachoMeterX, tachoMeterY, meterWidth, meterHeight, rpmAngle, 2);
    }
