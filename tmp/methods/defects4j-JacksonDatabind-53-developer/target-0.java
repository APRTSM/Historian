    public Object asKey(Class<?> rawBase) {
        // safe to pass _types array without copy since it is not exposed via
        // any access, nor modified by this class
        return new AsKey(rawBase, _types, _hashCode);
    }
        public int hashCode() { return _hash; }
        public boolean equals(Object o) {
            if (o == this) return true;
            if (o == null) return false;
            if (o.getClass() != getClass()) return false;
            AsKey other = (AsKey) o;

            if ((_hash == other._hash) && (_raw == other._raw)) {
                final JavaType[] otherParams = other._params;
                final int len = _params.length;

                if (len == otherParams.length) {
                    for (int i = 0; i < len; ++i) {
                        if (!_params[i].equals(otherParams[i])) {
                            return false;
                        }
                    }
                    return true;
                }
            }
            return false;
        }
        public String toString() {
            return _raw.getName()+"<>";
        }
        public AsKey(Class<?> raw, JavaType[] params, int hash) {
            _raw = raw ;
            _params = params;
            _hash = hash;
        }
