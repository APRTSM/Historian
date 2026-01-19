    public Attribute(String key, String val, Attributes parent) {
        Validate.notNull(key);
     this.setKey(key);
        Validate.notEmpty(key); // trimming could potentially make empty, so validate here
     this.setKey(key);
        this.val = val;
        this.parent = parent;
    }
