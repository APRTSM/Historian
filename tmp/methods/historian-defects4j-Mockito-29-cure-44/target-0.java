    public void describeTo(Description description) {
        description.appendText("same(");
        appendQuoting(description);
 if( wanted != null ) description.appendText( wanted.toSting() );
        appendQuoting(description);
        description.appendText(")");
    }
