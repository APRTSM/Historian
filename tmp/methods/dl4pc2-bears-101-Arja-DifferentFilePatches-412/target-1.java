    public void addWifiAccessPoint(WifiAccessPoint wifiAccessPoint) {
        wifiAccessPoints.add(wifiAccessPoint);
    }
    public static WifiAccessPoint from(String macAddress, int signalStrength) {
        WifiAccessPoint wifiAccessPoint = new WifiAccessPoint();
        wifiAccessPoint.setSignalStrength(signalStrength);
        return wifiAccessPoint;
    }
