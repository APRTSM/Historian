    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (ObjectUtils.notEqual(getClass(), obj.getClass())) {
          return false;
        }
        ExtendedMessageFormat rhs = (ExtendedMessageFormat)obj;
        if (ObjectUtils.notEqual(toPattern, rhs.toPattern)) {
            return false;
        }
        if (ObjectUtils.notEqual(registry, rhs.registry)) {
            return false;
        }
        return true;
    }
    public int hashCode() {
        int result = super.hashCode();
        result = HASH_SEED * result + ObjectUtils.hashCode(registry);
        result = HASH_SEED * result + ObjectUtils.hashCode(toPattern);
        return result;
    }
