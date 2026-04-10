Object[] args = invocation.getArguments();
Object mock = invocation.getMock();
if (args[0] == mock) {
    return 0;
} else {
    return 1;
}