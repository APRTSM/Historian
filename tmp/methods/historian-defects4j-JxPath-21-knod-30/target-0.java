    public int getLength() {
if (getBaseValue() == null ){
return 1;
}
        return ValueUtils.getLength(getBaseValue());
    }
