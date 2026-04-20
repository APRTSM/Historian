    public void describeTo(Description description) {
        description.appendText("same(");
        appendQuoting(description);
 if (wanted instanceof Object) description.appendText(wanted.toString());
        appendQuoting(description);
        description.appendText(")");
    }
