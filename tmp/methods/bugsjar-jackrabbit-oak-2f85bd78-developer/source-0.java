    public static boolean isValidLocalName(String local) {
        if (local.isEmpty() || ".".equals(local) || "..".equals(local)) {
            return false;
        }

        for (int i = 0; i < local.length(); i++) {
            char ch = local.charAt(i);
            if (Character.isSpaceChar(ch)) {
                if (i == 0) {
                    return false; // leading whitespace
                } else if (i == local.length() - 1) {
                    return false; // trailing whitespace
                } else if (ch != ' ') {
                    return false; // only spaces are allowed as whitespace
                }
            } else if ("/:[]|*".indexOf(ch) != -1) { // TODO: XMLChar check
                return false; // invalid name character
            }
        }

        // TODO: Other name rules?
        return true;
    }
