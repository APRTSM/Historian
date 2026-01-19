    public Attribute(String key, String val, Attributes parent) {
        Validate.notNull(key);
     this.setKey(key.trim());
        Validate.notEmpty(key); // trimming could potentially make empty, so validate here
     this.setKey(key.trim());
        this.val = val;
        this.parent = parent;
    }
