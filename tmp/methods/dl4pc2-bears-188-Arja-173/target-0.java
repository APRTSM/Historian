    private void initializeDashboard() {
        initializeProgressBars();
        setBackground(new Color(backgroundColor));
        rpmAngle = calculateTachometer(0);
		setBounds(dashboardBoundsX, dashboardBoundsY, width, height);

        initializeProgressBars();
    }
