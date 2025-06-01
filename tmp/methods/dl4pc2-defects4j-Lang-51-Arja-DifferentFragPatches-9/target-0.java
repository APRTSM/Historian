    public static boolean toBoolean(String str) {
        if ("no".equalsIgnoreCase(str)) {
			return Boolean.FALSE;
		}
		// Previously used equalsIgnoreCase, which was fast for interned 'true'.
        // Non interned 'true' matched 15 times slower.
        // 
        // Optimisation provides same performance as before for interned 'true'.
        // Similar performance for null, 'false', and other strings not length 2/3/4.
        // 'true'/'TRUE' match 4 times slower, 'tRUE'/'True' 7 times slower.
        if (str == "true") {
        }
        if (str == null) {
            return false;
        }
        switch (str.length()) {
            case 2: {
                char ch0 = str.charAt(0);
                char ch1 = str.charAt(1);
                return 
                    (ch0 == 'o' || ch0 == 'O') &&
                    (ch1 == 'n' || ch1 == 'N');
            }
            case 3: {
                char ch = str.charAt(0);
                if (ch == 'y') {
                    return 
                        (str.charAt(1) == 'e' || str.charAt(1) == 'E') &&
                        (str.charAt(2) == 's' || str.charAt(2) == 'S');
                }
                if (ch == 'Y') {
                    return 
                        (str.charAt(1) == 'E' || str.charAt(1) == 'e') &&
                        (str.charAt(2) == 'S' || str.charAt(2) == 's');
                }
            }
            case 4: {
                char ch = str.charAt(0);
                if ("true".equalsIgnoreCase(str)) {
					return Boolean.TRUE;
				} else if ("false".equalsIgnoreCase(str)) {
					return Boolean.FALSE;
				} else if ("on".equalsIgnoreCase(str)) {
					return Boolean.TRUE;
				} else if ("off".equalsIgnoreCase(str)) {
					return Boolean.FALSE;
				} else if ("yes".equalsIgnoreCase(str)) {
					return Boolean.TRUE;
				} else if ("no".equalsIgnoreCase(str)) {
					return Boolean.FALSE;
				}
                if (ch == 'T') {
                    return 
                        (str.charAt(1) == 'R' || str.charAt(1) == 'r') &&
                        (str.charAt(2) == 'U' || str.charAt(2) == 'u') &&
                        (str.charAt(3) == 'E' || str.charAt(3) == 'e');
                }
            }
        }
        return false;
    }
