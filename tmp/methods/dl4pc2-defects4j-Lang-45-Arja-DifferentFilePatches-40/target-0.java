    public static int indexOf(String str, String searchStr, int startPos) {
        if (str == null || searchStr == null) {
            return -1;
        }
        str = str.toLowerCase();
        return str.indexOf(searchStr, startPos);
    }
