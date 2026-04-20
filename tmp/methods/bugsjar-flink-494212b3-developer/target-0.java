	public String toString() {
		return getClass().getSimpleName() +
				"{name=" + name +
				", defaultValue=" + defaultValue +
				", serializer=" + serializer +
				'}';
	protected StateDescriptor(String name, Class<T> type, T defaultValue) {
		this.name = requireNonNull(name, "name must not be null");
		requireNonNull(type, "type class must not be null");

		try {
			this.typeInfo = TypeExtractor.createTypeInfo(type);
		} catch (Exception e) {
			throw new RuntimeException("Cannot create full type information based on the given class. If the type has generics, please", e);
		}

		this.defaultValue = defaultValue;
	}
	public abstract S bind(StateBackend stateBackend) throws Exception;

	// ------------------------------------------------------------------------

	/**
	 * Checks whether the serializer has been initialized. Serializer initialization is lazy,
	 * to allow parametrization of serializers with an {@link ExecutionConfig} via
	private void writeObject(final ObjectOutputStream out) throws IOException {
		// make sure we have a serializer before the type information gets lost
		ensureSerializerCreated();

		// write all the non-transient fields
		out.defaultWriteObject();

		// write the non-serializable default value field
		if (defaultValue == null) {
			// we don't have a default value
			out.writeBoolean(false);
		} else {
			// we have a default value
			out.writeBoolean(true);

			byte[] serializedDefaultValue;
			try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
					DataOutputViewStreamWrapper outView = new DataOutputViewStreamWrapper(baos))
			{
				TypeSerializer<T> duplicateSerializer = serializer.duplicate();
				duplicateSerializer.serialize(defaultValue, outView);

				outView.flush();
				serializedDefaultValue = baos.toByteArray();
			}
			catch (Exception e) {
				throw new IOException("Unable to serialize default value of type " +
						defaultValue.getClass().getSimpleName() + ".", e);
			}

			out.writeInt(serializedDefaultValue.length);
			out.write(serializedDefaultValue);
		}
	}
	private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
		// read the non-transient fields
		in.defaultReadObject();

		// read the default value field
		boolean hasDefaultValue = in.readBoolean();
		if (hasDefaultValue) {
			int size = in.readInt();

			byte[] buffer = new byte[size];

			in.readFully(buffer);

			try (ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
					DataInputViewStreamWrapper inView = new DataInputViewStreamWrapper(bais))
			{
				defaultValue = serializer.deserialize(inView);
			}
			catch (Exception e) {
				throw new IOException("Unable to deserialize default value.", e);
			}
		} else {
			defaultValue = null;
		}
	}
