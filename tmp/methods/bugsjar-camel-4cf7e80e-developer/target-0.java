    public static String encode(String s) {
        int n = s == null ? 0 : s.length();
        if (n == 0) {
            return s;
        }

        // First check whether we actually need to encode
        char chars[] = s.toCharArray();
        for (int i = 0;;) {
            // just deal with the ascii character
            if (chars[i] > 0 && chars[i] < 128) {
                if (unsafeCharacters.get(chars[i])) {
                    break;
                }
            }
            if (++i >= chars.length) {
                return s;
            }
        }

        // okay there are some unsafe characters so we do need to encode
        // see details at: http://en.wikipedia.org/wiki/Url_encode
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < chars.length; i++) {
            char ch = chars[i];
            if (ch > 0 && ch < 128 && unsafeCharacters.get(ch)) {
                // special for % sign as it may be a decimal encoded value
                if (ch == '%') {
                    char next = i + 1 < chars.length ? chars[i + 1] : ' ';
                    char next2 = i + 2 < chars.length ? chars[i + 2] : ' ';

                    if (isHexDigit(next) && isHexDigit(next2)) {
                        // its already encoded (decimal encoded) so just append as is
                        sb.append(ch);
                    } else {
                        // must escape then, as its an unsafe character
                        appendEscape(sb, (byte)ch);
                    }
                } else {
                    // must escape then, as its an unsafe character
                    appendEscape(sb, (byte)ch);
                }
            } else {
                sb.append(ch);
            }
        }
        return sb.toString();
    }
    private static boolean isHexDigit(char ch) {
        for (char hex : HEX_DIGITS) {
            if (hex == ch) {
                return true;
            }
        }
        return false;
    }
