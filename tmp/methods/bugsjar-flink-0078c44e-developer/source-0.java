	public T deserialize(T reuse, DataInputView source) throws IOException {

		// handle null values
		int flags = source.readByte();
		if((flags & IS_NULL) != 0) {
			return null;
		}

		Class<?> subclass = null;
		TypeSerializer subclassSerializer = null;
		if ((flags & IS_SUBCLASS) != 0) {
			String subclassName = source.readUTF();
			try {
				subclass = Class.forName(subclassName, true, cl);
			} catch (ClassNotFoundException e) {
				throw new RuntimeException("Cannot instantiate class.", e);
			}
			subclassSerializer = getSubclassSerializer(subclass);

			if (reuse == null || subclass != reuse.getClass()) {
				// cannot reuse
				reuse = (T) subclassSerializer.createInstance();
				// also initialize fields for which the subclass serializer is not responsible
				initializeFields(reuse);
			}
		} else if ((flags & IS_TAGGED_SUBCLASS) != 0) {
			int subclassTag = source.readByte();
			subclassSerializer = registeredSerializers[subclassTag];

			if (reuse == null || ((PojoSerializer)subclassSerializer).clazz != reuse.getClass()) {
				// cannot reuse
				reuse = (T) subclassSerializer.createInstance();
				// also initialize fields for which the subclass serializer is not responsible
				initializeFields(reuse);
			}
		} else {
			if (reuse == null || clazz != reuse.getClass()) {
				reuse = createInstance();
			}
		}

		if ((flags & NO_SUBCLASS) != 0) {
			try {
				for (int i = 0; i < numFields; i++) {
					boolean isNull = source.readBoolean();
					if (isNull) {
						fields[i].set(reuse, null);
					} else {
						Object field = fieldSerializers[i].deserialize(fields[i].get(reuse), source);

						fields[i].set(reuse, field);
					}
				}
			} catch (IllegalAccessException e) {
				throw new RuntimeException(
						"Error during POJO copy, this should not happen since we check the fields before.");
			}
		} else {
			if (subclassSerializer != null) {
				reuse = (T) subclassSerializer.deserialize(reuse, source);
			}
		}

		return reuse;
	}
	public T copy(T from, T reuse) {
		if (from == null) {
			return null;
		}

		Class<?> actualType = from.getClass();
		if (reuse == null || actualType != reuse.getClass()) {
			// cannot reuse, do a non-reuse copy
			return copy(from);
		}

		if (actualType == clazz) {
			try {
				for (int i = 0; i < numFields; i++) {
					Object value = fields[i].get(from);
					if (value != null) {
						Object copy = fieldSerializers[i].copy(fields[i].get(from), fields[i].get(reuse));
						fields[i].set(reuse, copy);
					}
					else {
						fields[i].set(reuse, null);
					}
				}
			} catch (IllegalAccessException e) {
				throw new RuntimeException("Error during POJO copy, this should not happen since we check the fields" + "before.");
			}
		} else {
			TypeSerializer subclassSerializer = getSubclassSerializer(actualType);
			reuse = (T) subclassSerializer.copy(from, reuse);
		}

		return reuse;
	}
