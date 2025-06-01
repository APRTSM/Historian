    public Value[] getValues() throws RepositoryException {
        PropertyValue[] values = row.getValues();
        int len = values.length;
        Value[] v2 = new Value[values.length];
        for (int i = 0; i < len; i++) {
            v2[i] = result.createValue(values[i]);
        }
        return v2;
    }
