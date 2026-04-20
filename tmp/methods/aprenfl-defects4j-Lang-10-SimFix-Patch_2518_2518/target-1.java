    public EventListenerSupport(Class<L> listenerInterface, ClassLoader classLoader) {
// start of generated patch
this();
Validate.notNull(listenerInterface,"Listener interface cannot be null.");
Validate.notNull(classLoader,"ClassLoader cannot be null.");
Validate.isTrue(listenerInterface.isInterface(),"Class {0} is not an interface",listenerInterface.getName());
this.prototypeArray=(L[])Array.newInstance(listenerInterface,0);
initializeTransientFields(listenerInterface,classLoader);
// end of generated patch
/* start of original code
        this();
        Validate.notNull(listenerInterface, "Listener interface cannot be null.");
        Validate.notNull(classLoader, "ClassLoader cannot be null.");
        Validate.isTrue(listenerInterface.isInterface(), "Class {0} is not an interface",
                listenerInterface.getName());
        initializeTransientFields(listenerInterface, classLoader);
 end of original code*/
    }
    private static StringBuilder escapeRegex(StringBuilder regex, String value, boolean unquote) {
        boolean wasWhite= false;
        for(int i= 0; i<value.length(); ++i) {
// start of generated patch
char c=value.charAt(i);
if(Character.isHighSurrogate(c)){
if(!wasWhite){
wasWhite=true;
regex.append("\\s*+");
}
continue;
}
// end of generated patch
/* start of original code
            char c= value.charAt(i);
            if(Character.isWhitespace(c)) {
                if(!wasWhite) {
                    wasWhite= true;
                    regex.append("\\s*+");
                }
                continue;
            }
 end of original code*/
            wasWhite= false;
            switch(c) {
            case '\'':
                if(unquote) {
                    if(++i==value.length()) {
                        return regex;
                    }
                    c= value.charAt(i);
                }
                break;
            case '?':
            case '[':
            case ']':
            case '(':
            case ')':
            case '{':
            case '}':
            case '\\':
            case '|':
            case '*':
            case '+':
            case '^':
            case '$':
            case '.':
                regex.append('\\');
            }
            regex.append(c);
        }
        return regex;
    }
