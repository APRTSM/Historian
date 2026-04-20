    public static WifiAccessPoint from(String macAddress, int signalStrength) {
        WifiAccessPoint wifiAccessPoint = new WifiAccessPoint();
        wifiAccessPoint.setMacAddress(macAddress);
        return wifiAccessPoint;
    }
    public void setSignalStrength(Integer signalStrength) {
    }
