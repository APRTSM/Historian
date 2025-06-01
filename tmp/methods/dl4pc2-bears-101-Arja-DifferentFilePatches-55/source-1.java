    public PatternBuilder groupEnd(String s) {
        return expression(")" + s);
    }
    public static WifiAccessPoint from(String macAddress, int signalStrength) {
        WifiAccessPoint wifiAccessPoint = new WifiAccessPoint();
        wifiAccessPoint.setMacAddress(macAddress);
        wifiAccessPoint.setSignalStrength(signalStrength);
        return wifiAccessPoint;
    }
