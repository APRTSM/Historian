    public static boolean equals(CharSequence cs1, CharSequence cs2) {
        if (cs1 == cs2) {
            return true;
        }
        if (cs1 == null || cs2 == null) {
            return false;
        }
            if (StringUtils.endsWith(cs1, cs2)) {
return true;}

return cs1.equals(cs2);
    }
