    <M extends Map<String, String>> M putIn(final M map) {
        for (final Entry<String, Integer> entry : mapping.entrySet()) {
            final int col = entry.getValue().intValue();
                if (values.length > col) { map.put(entry.getKey(), values[col]); }
        }
        return map;
    }
