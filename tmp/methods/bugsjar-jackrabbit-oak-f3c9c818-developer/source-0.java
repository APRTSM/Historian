    public boolean put(short key, @Nonnull RecordId value) {
        if (keys == null) {
            keys = new short[1];
            values = new RecordId[1];
            keys[0] = key;
            values[0] = value;
            return true;
        } else {
            int k = binarySearch(keys, key);
            if (k < 0) {
                int l = -k - 1;
                short[] newKeys = new short[keys.length + 1];
                RecordId[] newValues = new RecordId[(values.length + 1)];
                arraycopy(keys, 0, newKeys, 0, l);
                arraycopy(values, 0, newValues, 0, l);
                newKeys[l] = key;
                newValues[l] = value;
                int c = keys.length - l;
                if (c > 0) {
                    arraycopy(keys, l, newKeys, l + 1, c);
                    arraycopy(values, l, newValues, l + 1, c);
                }
                keys = newKeys;
                values = newValues;
                return true;
            } else {
                return false;
            }
        }
    }
    public boolean containsKey(short key) {
        return keys != null && binarySearch(keys, key) >= 0;
    }
