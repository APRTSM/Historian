    public void asXML(final StringBuilder sb) {
        sb.append("<Map>\n");
        for (final Map.Entry<String, String> entry : data.entrySet()) {
            sb.append("  <Entry key=").append(entry.getKey()).append(">").append(entry.getValue()).append("</Entry>\n");
        }
        sb.append("</Map>");
    }
