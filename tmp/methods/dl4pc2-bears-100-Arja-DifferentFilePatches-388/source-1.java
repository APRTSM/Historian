    public boolean getBoolean(String key) {
        if (attributes.containsKey(key)) {
            return Boolean.parseBoolean(attributes.get(key).toString());
        } else {
            return false;
        }
    }
    public boolean getValid() {
        return valid;
    }
