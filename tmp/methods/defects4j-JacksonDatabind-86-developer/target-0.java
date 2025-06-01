    public JavaType getSuperClass() {
	if (_referencedType != null) {
		return _referencedType.getSuperClass();
	}
	return super.getSuperClass();
    }
