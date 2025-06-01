    public int compareTo(Id o) {
        byte[] other = o.getBytes();
        int len = Math.min(raw.length, other.length);
        
        for (int i = 0; i < len; i++) {
            if (raw[i] != other[i]) {
                final int rawValue = raw[i] & 0xFF; // unsigned value
                final int otherValue = other[i] & 0xFF; // unsigned value
                return rawValue - otherValue;
            }
        }
        return raw.length - other.length;
    }
