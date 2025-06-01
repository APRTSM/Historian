    public void updateDisplayedValues(ReadOnlyInputPacket inputPacket) {
        gasProgressBar.setValue(inputPacket.getGasPedalPosition());
        breakProgressBar.setValue(inputPacket.getBreakPedalPosition());
        setBounds(dashboardBoundsX, dashboardBoundsY, width, height);
        rpmAngle = calculateTachometer(0);
    }
