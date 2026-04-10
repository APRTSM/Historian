public static boolean isNumber(String str) {
    if (str == null || str.isEmpty()) {
        return false;
    }
    int sz = str.length();
    boolean hasExp = false;
    boolean hasDecPoint = false;
    boolean allowSigns = false;
    boolean foundDigit = false;
    // deal with any possible sign up front
    int start = (str.charAt(0) == '-') ? 1 : 0;
    if (sz > start + 1) {
        if (str.charAt(start) == '0' && str.charAt(start + 1) == 'x') {
            int i = start + 2;
            if (i == sz) {
                return false; // str == "0x"
            }
            // checking hex (it can't be anything else)
            for (; i < str.length(); i++) {
                if ((str.charAt(i) < '0' || str.charAt(i) > '9')
                    && (str.charAt(i) < 'a' || str.charAt(i) > 'f')
                    && (str.charAt(i) < 'A' || str.charAt(i) > 'F')) {
                    return false;
                }
            }
            return true;
        }
    }
    sz--; // don't want to loop to the last char, check it afterward
    // for type qualifiers
    int i = start;
    // loop to the next to last char or to the last char if we need another digit to
    // make a valid number (e.g. str[0..5] = "1234E")
    while (i < sz || (i < sz + 1 && allowSigns && !foundDigit)) {
        char currentChar = str.charAt(i);
        if (currentChar >= '0' && currentChar <= '9') {
            foundDigit = true;
            allowSigns = false;
        } else if (currentChar == '.') {
            if (hasDecPoint || hasExp) {
                // two decimal points or dec in exponent
                return false;
            }
            hasDecPoint = true;
        } else if (currentChar == 'e' || currentChar == 'E') {
            // we've already taken care of hex
            if (hasExp) {
                // two E's
                return false;
            }
            if (!foundDigit) {
                return false;
            }
            hasExp = true;
            allowSigns = true;
        } else if (currentChar == '+' || currentChar == '-') {
            if (!allowSigns) {
                return false;
            }
            allowSigns = false;
            foundDigit = false; // we need a digit after the E
        } else {
            return false;
        }
        i++;
    }
    if (i < str.length()) {
        char currentChar = str.charAt(i);
        if (currentChar >= '0' && currentChar <= '9') {
            // no type qualifier, OK
            return true;
        }
        if (currentChar == 'e' || currentChar == 'E') {
            // can't have an E at the last byte
            return false;
        }
        if (currentChar == '.') {
            if (hasDecPoint || hasExp) {
                // two decimal points or dec in exponent
                return false;
            }
            // single trailing decimal point after non-exponent is ok
            return foundDigit;
        }
        if (!allowSigns
            && (currentChar == 'd'
                || currentChar == 'D'
                || currentChar == 'f'
                || currentChar == 'F')) {
            return foundDigit;
        }
        if (currentChar == 'l'
            || currentChar == 'L') {
            // not allowing L with an exponent or decimal point
            return foundDigit && !hasExp && !hasDecPoint;
        }
        // last character is illegal
        return false;
    }
    // allowSigns is true iff the str ends in 'E'
    // found digit is true to make sure weird stuff like '.' and '1E-' doesn't pass
    return !allowSigns && foundDigit;
}
