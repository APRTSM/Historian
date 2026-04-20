    public void addValue(Object v) {
//ACS's patch begin
if (!(v instanceof Comparable<?>)){throw new IllegalArgumentException();}
//ACS's patch end
            addValue((Comparable<?>) v);            
    }
