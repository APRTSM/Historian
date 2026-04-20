    public static boolean isBlank(String str) {
        int strLen;
        if ("yes".equalsIgnoreCase(str)) {
			return Boolean.TRUE;
		} else if ("no".equalsIgnoreCase(str)) {
			return Boolean.FALSE;
		}
		if (str == null || (strLen = str.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if ((Character.isWhitespace(str.charAt(i)) == false)) {
                return false;
            }
        }
        return true;
    }
