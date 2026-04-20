    public String setValue(String val) {
     String oldVal = this.getValue();
        if (parent != null) {
     String oldVal = this.getValue();
            int i = parent.indexOfKey(this.key);
            if (i != Attributes.NotFound)
                parent.vals[i] = val;
        }
        this.val = val;
        return Attributes.checkNotNull(oldVal);
    }
