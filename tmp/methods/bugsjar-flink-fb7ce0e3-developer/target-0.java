	public void copy(DataInputView source, DataOutputView target) throws IOException {
		// copy the Non-Null/Null tag
		target.writeBoolean(source.readBoolean());
		for (int i = 0; i < numFields; i++) {
			boolean isNull = source.readBoolean();
			target.writeBoolean(isNull);
			if (!isNull) {
				fieldSerializers[i].copy(source, target);
			}
		}
	}
	public T copy(T from, T reuse) {
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
			throw new RuntimeException("Error during POJO copy, this should not happen since we check the fields" +
					"before.");
		}
		return reuse;
	}
	public T copy(T from) {
		T target;
		try {
			target = clazz.newInstance();
		}
		catch (Throwable t) {
			throw new RuntimeException("Cannot instantiate class.", t);
		}
		
		try {
			for (int i = 0; i < numFields; i++) {
				Object value = fields[i].get(from);
				if (value != null) {
					Object copy = fieldSerializers[i].copy(value);
					fields[i].set(target, copy);
				}
				else {
					fields[i].set(target, null);
				}
			}
		}
		catch (IllegalAccessException e) {
			throw new RuntimeException("Error during POJO copy, this should not happen since we check the fields before.");
		}
		return target;
	}
