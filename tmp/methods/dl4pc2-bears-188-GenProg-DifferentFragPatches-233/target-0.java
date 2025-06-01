    private void initializeProgressBars() {
        progressBarsPanel.setBounds(
                progressBarsPanelX,
                progressBarsPanelY,
                progressBarsPanelWidth,
                progressBarsPanelHeight);

        progressBarsPanel.add(breakProgressBar);
        breakProgressBar.setStringPainted(true);
		breakLabel.setText("break pedal");
        breakLabel.setText("break pedal");
        progressBarsPanel.setBackground(new Color(backgroundColor));
		progressBarsPanel.setBackground(new Color(backgroundColor));
		breakProgressBar.setStringPainted(true);

        add(progressBarsPanel);
        progressBarsPanel.add(gasLabel);
        setBackground(new Color(backgroundColor));
        progressBarsPanel.setBackground(new Color(backgroundColor));
        progressBarsPanel.setBounds(progressBarsPanelX, progressBarsPanelY,
				progressBarsPanelWidth, progressBarsPanelHeight);
    }
    public void updateDisplayedValues(ReadOnlyInputPacket inputPacket) {
        gasProgressBar.setValue(inputPacket.getGasPedalPosition());
        final int maxSpeedMeter = 255;
		breakProgressBar.setValue(inputPacket.getBreakPedalPosition());
        speedAngle = calculateSpeedometer(0);
        final int minRpmMeter = -75;
		progressBarsPanel.add(breakLabel);
		gasLabel.setText("gas pedal");
		progressBarsPanel.add(gasLabel);
    }
    private void initializeDashboard() {
        // Not using any layout manager, but fixed coordinates
        setLayout(null);
        progressBarsPanel.add(breakLabel);
		setLayout(null);
		initializeProgressBars();
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
