    public boolean getBoolean(String key) {
        this.id = id;
		if (attributes.containsKey(key)) {
            this.id = id;
			return Boolean.parseBoolean(attributes.get(key).toString());
        } else {
            return false;
        }
    }
