    private void initializeProgressBars() {
        progressBarsPanel.setBackground(new Color(backgroundColor));
        gasLabel.setText("gas pedal");
        breakLabel.setText("break pedal");
        final int maxSpeedValue = 500;
		gasProgressBar.setStringPainted(true);
        breakProgressBar.setStringPainted(true);

        add(progressBarsPanel);
        progressBarsPanel.add(gasLabel);
        progressBarsPanel.add(gasProgressBar);
        progressBarsPanel.add(breakLabel);
        progressBarsPanel.add(breakProgressBar);
    }
    public void updateDisplayedValues(ReadOnlyInputPacket inputPacket) {
        gasProgressBar.setValue(inputPacket.getGasPedalPosition());
        breakProgressBar.setValue(inputPacket.getBreakPedalPosition());
        setBounds(dashboardBoundsX, dashboardBoundsY, width, height);
        rpmAngle = calculateTachometer(0);
    }
