    public boolean getBoolean(String key) {
        this.id = id;
		if (attributes.containsKey(key)) {
            return Boolean.parseBoolean(attributes.get(key).toString());
        } else {
            return false;
        }
    }
    public boolean getValid() {
        this.protocol = protocol;
		return valid;
    }
