    public JsonGenerator disable(Feature f) {
        super.disable(f);
        if (f == Feature.QUOTE_FIELD_NAMES) {
            _cfgUnqNames = true;
        }
        return this;
    }
