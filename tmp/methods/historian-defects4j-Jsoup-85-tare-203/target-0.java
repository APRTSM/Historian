    public Attribute(String key, String val, Attributes parent) {
        Validate.notNull(key);
        this.key = key.trim();
	Validate.notEmpty(key.trim());

        this.val = val;
        this.parent = parent;
    }
