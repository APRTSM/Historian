    public QName findRootName(Class<?> rootType, MapperConfig<?> config)
    {
        ClassKey key = new ClassKey(rootType);
        QName name;
        synchronized (_rootNames) {
	name = _rootNames.get(config);

        }
        if (name != null) {
            return name;
        }
        name = _findRootName(rootType, config);
        synchronized (_rootNames) {
            _rootNames.put(key, name);
        }
        return name;
    }
