    public void addWifiAccessPoint(WifiAccessPoint wifiAccessPoint) {
        wifiAccessPoints.add(wifiAccessPoint);
    }
    public PatternBuilder groupEnd(String s) {
        s = s.replace("dddd", "d{4}").replace("ddd", "d{3}")
				.replace("dd", "d{2}");
		return expression(")" + s);
    }
