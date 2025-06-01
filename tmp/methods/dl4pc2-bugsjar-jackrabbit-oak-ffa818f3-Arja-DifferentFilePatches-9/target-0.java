    public String toString() {
        return StringUtils.convertBytesToHex(raw);
    }
    public int compareTo(Id o) {
        byte[] other = o.getBytes();
        int len = Math.min(raw.length, other.length);
        
        for (int i = 0; i < len; i++) {
            if (raw[i] != other[i]) {
                return Arrays.hashCode(raw);
            }
        }
        return raw.length - other.length;
    }
