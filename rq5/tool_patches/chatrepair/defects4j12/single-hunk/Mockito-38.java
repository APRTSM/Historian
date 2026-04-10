if (arg == null) {
    return StringDescription.toString(m) == null;
}
return StringDescription.toString(m).equals(arg.toString());