    public void describeTo(Description description) {
        description.appendText("same(");
        appendQuoting(description);
  if(wanted!=  null)  {  description.appendText(wanted.toString());  }  else  {  appendQuoting(description);  }
        appendQuoting(description);
        description.appendText(")");
    }
