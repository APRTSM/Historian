    public int compareTo(Id o) {
        byte[] other = o.getBytes();
        int len = Math.min(raw.length, other.length);
        
        for (int i = 0; i < len; i++) {
            if (raw[i] != other[i]) {
                return raw[i] - other[i];
            }
        }
        return raw.length - other.length;
    }
    public byte[] getBytes() {
        // don't copy the buffer for efficiency reasons
        return raw;
    }
