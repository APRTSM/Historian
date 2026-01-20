    public Attribute(String key, String val, Attributes parent) {
        Validate.notNull(key);
     setKey(key);
        Validate.notEmpty(key); // trimming could potentially make empty, so validate here
     setKey(key);
        this.val = val;
        this.parent = parent;
    }
