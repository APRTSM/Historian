    public String setValue(String val) {
     String oldVal = this.val;
        if (parent != null) {
     String oldVal = this.val;
            int i = parent.indexOfKey(this.key);
            if (i != Attributes.NotFound)
                parent.vals[i] = val;
        }
        this.val = val;
        return Attributes.checkNotNull(oldVal);
    }
