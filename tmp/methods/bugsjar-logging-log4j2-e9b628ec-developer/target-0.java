    protected int[] getEventIndices(final String text, final int beginIndex) {
        // Scan the text for the end of the next JSON object.
        final int start = text.indexOf(EVENT_START_MARKER, beginIndex);
        if (start == END) {
            return END_PAIR;
        }
        final char[] charArray = text.toCharArray();
        int stack = 0;
        boolean inStr = false;
        boolean inEsc = false;
        for (int i = start; i < charArray.length; i++) {
            final char c = charArray[i];
            if (inEsc) {
		// Skip this char and continue
		inEsc = false;
            } else {
                switch (c) {
                case EVENT_START_MARKER:
                    if (!inStr) {
                        stack++;
                    }
                    break;
                case EVENT_END_MARKER:
                    if (!inStr) {
                        stack--;
                    }
                    break;
                case JSON_STR_DELIM:
                    inStr = !inStr;
                    break;
                case JSON_ESC:
                    inEsc = true;
                    break;
                }
                if (stack == 0) {
                    return new int[] { start, i };
                }
            }
        }
        return END_PAIR;
    }
