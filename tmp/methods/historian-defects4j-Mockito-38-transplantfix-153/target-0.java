    private boolean toStringEquals(Matcher m, Object arg) {
        if (!(arg instanceof Comparable)) {
return false;
}

return StringDescription.toString(m).equals(arg.toString());
    }
