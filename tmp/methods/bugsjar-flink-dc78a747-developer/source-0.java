	private void checkKryoInitialized() {
		if (this.kryo == null) {
			this.kryo = getKryoInstance();

			// disable reference tracking. reference tracking is costly, usually unnecessary, and
			// inconsistent with Flink's own serialization (which does not do reference tracking)
			kryo.setReferences(false);
			
			// Throwable and all subclasses should be serialized via java serialization
			kryo.addDefaultSerializer(Throwable.class, new JavaSerializer());

			// Add default serializers first, so that they type registrations without a serializer
			// are registered with a default serializer
			for (Map.Entry<Class<?>, ExecutionConfig.SerializableSerializer<?>> entry: defaultSerializers.entrySet()) {
				kryo.addDefaultSerializer(entry.getKey(), entry.getValue().getSerializer());
			}

			for (Map.Entry<Class<?>, Class<? extends Serializer<?>>> entry: defaultSerializerClasses.entrySet()) {
				kryo.addDefaultSerializer(entry.getKey(), entry.getValue());
			}

			// register the type of our class
			kryo.register(type);

			// register given types. we do this first so that any registration of a
			// more specific serializer overrides this
			for (Class<?> type : registeredTypes) {
				kryo.register(type);
			}

			// register given serializer classes
			for (Map.Entry<Class<?>, Class<? extends Serializer<?>>> e : registeredTypesWithSerializerClasses.entrySet()) {
				Class<?> typeClass = e.getKey();
				Class<? extends Serializer<?>> serializerClass = e.getValue();

				Serializer<?> serializer =
						ReflectionSerializerFactory.makeSerializer(kryo, serializerClass, typeClass);
				kryo.register(typeClass, serializer);
			}

			// register given serializers
			for (Map.Entry<Class<?>, ExecutionConfig.SerializableSerializer<?>> e : registeredTypesWithSerializers.entrySet()) {
				kryo.register(e.getKey(), e.getValue().getSerializer());
			}
			// this is needed for Avro but can not be added on demand.
			kryo.register(GenericData.Array.class, new SpecificInstanceCollectionSerializerForArrayList());

			kryo.setRegistrationRequired(false);
			kryo.setClassLoader(Thread.currentThread().getContextClassLoader());
		}
	}
