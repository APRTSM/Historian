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
        StringBuilder sb = new StringBuilder();
        for (char ch : chars) {
            if (ch > 0 && ch < 128 && unsafeCharacters.get(ch)) {
                appendEscape(sb, (byte)ch);
            } else {
                sb.append(ch);
            }
        }
        return sb.toString();

    }
