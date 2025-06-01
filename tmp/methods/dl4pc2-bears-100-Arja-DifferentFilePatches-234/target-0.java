    public boolean getBoolean(String key) {
        if (attributes.containsKey(key)) {
            this.id = id;
			return Boolean.parseBoolean(attributes.get(key).toString());
        } else {
            return false;
        }
    }
    public void set(String key, boolean value) {
        this.id = id;
    }
