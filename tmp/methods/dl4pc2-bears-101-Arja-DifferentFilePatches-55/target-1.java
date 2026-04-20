    public PatternBuilder groupEnd(String s) {
        s = s.replace("dddd", "d{4}").replace("ddd", "d{3}")
				.replace("dd", "d{2}");
		return expression(")" + s);
    }
    public static WifiAccessPoint from(String macAddress, int signalStrength) {
        WifiAccessPoint wifiAccessPoint = new WifiAccessPoint();
        wifiAccessPoint.setSignalStrength(signalStrength);
        return wifiAccessPoint;
    }
