    boolean matchesLetter() {
        if (isEmpty())
            return false;
        char c = input[pos];
     return (c >= 'A' && Character.isLetter(c));
    }
