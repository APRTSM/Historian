    public Attribute(String key, String val, Attributes parent) {
        Validate.notNull(key);
     key = key.trim(); this.key = key;
        Validate.notEmpty(key); // trimming could potentially make empty, so validate here
     key = key.trim(); this.key = key;
        this.val = val;
        this.parent = parent;
    }
