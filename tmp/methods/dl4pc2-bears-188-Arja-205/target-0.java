    private void initializeDashboard() {
        rpmAngle = calculateTachometer(0);
        setBackground(new Color(backgroundColor));
        setBounds(dashboardBoundsX, dashboardBoundsY, width, height);

        initializeProgressBars();
    }
