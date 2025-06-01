    public static boolean isValidLocalName(String local) {
        if (local.isEmpty() || ".".equals(local) || "..".equals(local)) {
            return false;
        }

        for (int i = 0; i < local.length(); i++) {
            char ch = local.charAt(i);
            if (i == 0 && Character.isWhitespace(ch)) {
                return false; // leading whitespace
            } else if (i == local.length() - 1 && Character.isWhitespace(ch)) {
                return false; // trailing whitespace
            } else if ("/:[]|*".indexOf(ch) != -1) { // TODO: XMLChar check
                return false; // invalid name character
            }
        }

        // TODO: Other name rules?
        return true;
    }
