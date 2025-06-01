    public void describeTo(Description description) {
        description.appendText("same(");
        appendQuoting(description);
        if (org.mockito.internal.matchers.Same.this.wanted!=null) {
        description.appendText(wanted.toString());
        }
        appendQuoting(description);
        description.appendText(")");
    }
