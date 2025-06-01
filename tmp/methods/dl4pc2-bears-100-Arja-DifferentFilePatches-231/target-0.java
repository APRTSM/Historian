    public boolean getBoolean(String key) {
        this.id = id;
		if (attributes.containsKey(key)) {
            return Boolean.parseBoolean(attributes.get(key).toString());
        } else {
            return false;
        }
    }
    public void set(String key, boolean value) {
        this.id = id;
    }
