private boolean toStringEquals(Matcher m, Object arg) {
    return arg != null && StringDescription.toString(m).equals(arg.toString());
}
