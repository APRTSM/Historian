    public static String escapeJsonControlCharacters(final String input) {
        // Check if the string is null, zero length or devoid of special characters
        // if so, return what was sent in.
    
        // TODO: escaped Unicode chars.
        
        if (Strings.isEmpty(input)
            || (input.indexOf('"') == -1 &&
            input.indexOf('\\') == -1 &&
            input.indexOf('/') == -1 &&
            input.indexOf('\b') == -1 &&
            input.indexOf('\f') == -1 &&
            input.indexOf('\n') == -1 &&
            input.indexOf('\r') == -1 && 
            input.indexOf('\t') == -1)) {
            return input;
        }
    
        final StringBuilder buf = new StringBuilder(input.length() + 6);
        
        final int len = input.length();
        for (int i = 0; i < len; i++) {
            final char ch = input.charAt(i);
            final String escBs = "\\\\";
            switch (ch) {
            case '"':
                buf.append(escBs);
                buf.append(ch);                
                break;
            case '\\':
                buf.append(escBs);
                buf.append(ch);                
                break;
            case '/':
                buf.append(escBs);
                buf.append(ch);                
                break;
            case '\b':
                buf.append(escBs);
                buf.append('b');                
                break;
            case '\f':
                buf.append(escBs);
                buf.append('f');                
                break;
            case '\n':
                buf.append(escBs);
                buf.append('n');                
                break;
            case '\r':
                buf.append(escBs);
                buf.append('r');                
                break;
            case '\t':
                buf.append(escBs);
                buf.append('t');                
                break;
            default: 
                buf.append(ch);                
            } 
        }
        return buf.toString();
    }
