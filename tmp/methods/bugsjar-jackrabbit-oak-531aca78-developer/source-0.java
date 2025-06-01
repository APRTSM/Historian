    public Value[] getValues() throws RepositoryException {
        PropertyValue[] values = row.getValues();
        int len = values.length;
        Value[] v2 = new Value[values.length];
        for (int i = 0; i < len; i++) {
            if(values[i].isArray()){
                v2[i] = result.createValue(mvpToString(values[i]));
            }else{
                v2[i] = result.createValue(values[i]);
            }
        }
        return v2;
    }
