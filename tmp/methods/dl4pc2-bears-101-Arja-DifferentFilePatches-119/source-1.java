    public void addWifiAccessPoint(WifiAccessPoint wifiAccessPoint) {
        if (wifiAccessPoints == null) {
            wifiAccessPoints = new ArrayList<>();
        }
        wifiAccessPoints.add(wifiAccessPoint);
    }
    public PatternBuilder groupEnd(String s) {
        return expression(")" + s);
    }
