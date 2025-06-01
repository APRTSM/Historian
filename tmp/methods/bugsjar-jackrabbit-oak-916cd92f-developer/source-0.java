    public static int decodeType(int split, String jsonString) {
        if (split == -1 || split > jsonString.length()) {
            return PropertyType.UNDEFINED;
        }
        else {
            Integer type = CODE2TYPE.get(jsonString.substring(0, split));
            return type == null
                ? PropertyType.UNDEFINED
                : type;
        }
    }
    public static int split(String jsonString) {
        if (jsonString.length() >= 4 && jsonString.charAt(3) == ':') {
            return 3;
        }
        else {
            return -1;
        }
    }
