    public static WifiAccessPoint from(String macAddress, int signalStrength) {
        WifiAccessPoint wifiAccessPoint = new WifiAccessPoint();
        wifiAccessPoint.setSignalStrength(signalStrength);
        return wifiAccessPoint;
    }
    public void setSignalStrength(Integer signalStrength) {
    }
