public void captureArgumentsFrom(Invocation i) {
    int k = 0;
    int argCount = i.getArguments().length;
    for (Matcher m : matchers) {
        if (m instanceof CapturesArguments && k < argCount) {
            ((CapturesArguments) m).captureFrom(i.getArguments()[k]);
        }
        k++;
    }
}
