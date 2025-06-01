	private <IN1, IN2, OUT> TypeInformation<OUT> createTypeInfoWithTypeHierarchy(ArrayList<Type> typeHierarchy, Type t,
			TypeInformation<IN1> in1Type, TypeInformation<IN2> in2Type) {
		
		// check if type is a subclass of tuple
		if (isClassType(t) && Tuple.class.isAssignableFrom(typeToClass(t))) {
			Type curT = t;
			
			// do not allow usage of Tuple as type
			if (typeToClass(t).equals(Tuple.class)) {
				throw new InvalidTypesException(
						"Usage of class Tuple as a type is not allowed. Use a concrete subclass (e.g. Tuple1, Tuple2, etc.) instead.");
			}
						
			// go up the hierarchy until we reach immediate child of Tuple (with or without generics)
			// collect the types while moving up for a later top-down 
			while (!(isClassType(curT) && typeToClass(curT).getSuperclass().equals(Tuple.class))) {
				typeHierarchy.add(curT);
				curT = typeToClass(curT).getGenericSuperclass();
			}
			
			// check if immediate child of Tuple has generics
			if (curT instanceof Class<?>) {
				throw new InvalidTypesException("Tuple needs to be parameterized by using generics.");
			}
			
			typeHierarchy.add(curT);
			
			ParameterizedType tupleChild = (ParameterizedType) curT;
			
			Type[] subtypes = new Type[tupleChild.getActualTypeArguments().length];
			
			// materialize possible type variables
			for (int i = 0; i < subtypes.length; i++) {
				// materialize immediate TypeVariables
				if (tupleChild.getActualTypeArguments()[i] instanceof TypeVariable<?>) {
					subtypes[i] = materializeTypeVariable(typeHierarchy, (TypeVariable<?>) tupleChild.getActualTypeArguments()[i]);
				}
				// class or parameterized type
				else {
					subtypes[i] = tupleChild.getActualTypeArguments()[i];
				}
			}
			
			TypeInformation<?>[] tupleSubTypes = new TypeInformation<?>[subtypes.length];
			for (int i = 0; i < subtypes.length; i++) {
				ArrayList<Type> subTypeHierarchy = new ArrayList<Type>(typeHierarchy);
				subTypeHierarchy.add(subtypes[i]);
				// sub type could not be determined with materializing
				// try to derive the type info of the TypeVariable from the immediate base child input as a last attempt
				if (subtypes[i] instanceof TypeVariable<?>) {
					tupleSubTypes[i] = createTypeInfoFromInputs((TypeVariable<?>) subtypes[i], subTypeHierarchy, in1Type, in2Type);
					
					// variable could not be determined
					if (tupleSubTypes[i] == null) {
						throw new InvalidTypesException("Type of TypeVariable '" + ((TypeVariable<?>) subtypes[i]).getName() + "' in '"
								+ ((TypeVariable<?>) subtypes[i]).getGenericDeclaration()
								+ "' could not be determined. This is most likely a type erasure problem. "
								+ "The type extraction currently supports types with generic variables only in cases where "
								+ "all variables in the return type can be deduced from the input type(s).");
					}
				} else {
					tupleSubTypes[i] = createTypeInfoWithTypeHierarchy(subTypeHierarchy, subtypes[i], in1Type, in2Type);
				}
			}
			
			Class<?> tAsClass = null;
			if (isClassType(t)) {
				tAsClass = typeToClass(t);
			}
			Preconditions.checkNotNull(tAsClass, "t has a unexpected type");
			// check if the class we assumed to be a Tuple so far is actually a pojo because it contains additional fields.
			// check for additional fields.
			int fieldCount = countFieldsInClass(tAsClass);
			if(fieldCount != tupleSubTypes.length) {
				// the class is not a real tuple because it contains additional fields. treat as a pojo
				if (t instanceof ParameterizedType) {
					return (TypeInformation<OUT>) analyzePojo(tAsClass, new ArrayList<Type>(typeHierarchy), (ParameterizedType) t, in1Type, in2Type);
				}
				else {
					return (TypeInformation<OUT>) analyzePojo(tAsClass, new ArrayList<Type>(typeHierarchy), null, in1Type, in2Type);
				}
			}
			
			return new TupleTypeInfo(tAsClass, tupleSubTypes);
			
		}
		// type depends on another type
		// e.g. class MyMapper<E> extends MapFunction<String, E>
		else if (t instanceof TypeVariable) {
			Type typeVar = materializeTypeVariable(typeHierarchy, (TypeVariable<?>) t);
			
			if (!(typeVar instanceof TypeVariable)) {
				return createTypeInfoWithTypeHierarchy(typeHierarchy, typeVar, in1Type, in2Type);
			}
			// try to derive the type info of the TypeVariable from the immediate base child input as a last attempt
			else {
				TypeInformation<OUT> typeInfo = (TypeInformation<OUT>) createTypeInfoFromInputs((TypeVariable<?>) t, typeHierarchy, in1Type, in2Type);
				if (typeInfo != null) {
					return typeInfo;
				} else {
					throw new InvalidTypesException("Type of TypeVariable '" + ((TypeVariable<?>) t).getName() + "' in '"
							+ ((TypeVariable<?>) t).getGenericDeclaration() + "' could not be determined. This is most likely a type erasure problem. "
							+ "The type extraction currently supports types with generic variables only in cases where "
							+ "all variables in the return type can be deduced from the input type(s).");
				}
			}
		}
		// arrays with generics 
		else if (t instanceof GenericArrayType) {
			GenericArrayType genericArray = (GenericArrayType) t;
			
			Type componentType = genericArray.getGenericComponentType();
			
			// due to a Java 6 bug, it is possible that the JVM classifies e.g. String[] or int[] as GenericArrayType instead of Class
			if (componentType instanceof Class) {
				
				Class<?> componentClass = (Class<?>) componentType;
				String className;
				// for int[], double[] etc.
				if(componentClass.isPrimitive()) {
					className = encodePrimitiveClass(componentClass);
				}
				// for String[], Integer[] etc.
				else {
					className = "L" + componentClass.getName() + ";";
				}
				
				Class<OUT> classArray;
				try {
					classArray = (Class<OUT>) Class.forName("[" + className);
				} catch (ClassNotFoundException e) {
					throw new InvalidTypesException("Could not convert GenericArrayType to Class.");
				}
				return getForClass(classArray);
			}
			
			TypeInformation<?> componentInfo = createTypeInfoWithTypeHierarchy(typeHierarchy, genericArray.getGenericComponentType(),
					in1Type, in2Type);
			return ObjectArrayTypeInfo.getInfoFor(t, componentInfo);
		}
		// objects with generics are treated as Class first
		else if (t instanceof ParameterizedType) {
			return (TypeInformation<OUT>) privateGetForClass(typeToClass(t), typeHierarchy, (ParameterizedType) t, in1Type, in2Type);
		}
		// no tuple, no TypeVariable, no generic type
		else if (t instanceof Class) {
			return privateGetForClass((Class<OUT>) t, typeHierarchy);
		}
		
		throw new InvalidTypesException("Type Information could not be created.");
	}
	private <OUT,IN1,IN2> TypeInformation<OUT> privateGetForClass(Class<OUT> clazz, ArrayList<Type> typeHierarchy,
			ParameterizedType parameterizedType, TypeInformation<IN1> in1Type, TypeInformation<IN2> in2Type) {
		Preconditions.checkNotNull(clazz);
		
		if (clazz.equals(Object.class)) {
			return new GenericTypeInfo<OUT>(clazz);
		}
		
		// check for arrays
		if (clazz.isArray()) {

			// primitive arrays: int[], byte[], ...
			PrimitiveArrayTypeInfo<OUT> primitiveArrayInfo = PrimitiveArrayTypeInfo.getInfoFor(clazz);
			if (primitiveArrayInfo != null) {
				return primitiveArrayInfo;
			}
			
			// basic type arrays: String[], Integer[], Double[]
			BasicArrayTypeInfo<OUT, ?> basicArrayInfo = BasicArrayTypeInfo.getInfoFor(clazz);
			if (basicArrayInfo != null) {
				return basicArrayInfo;
			}
			
			// object arrays
			else {
				return ObjectArrayTypeInfo.getInfoFor(clazz);
			}
		}
		
		// check for writable types
		if(Writable.class.isAssignableFrom(clazz) && !Writable.class.equals(clazz)) {
			return (TypeInformation<OUT>) WritableTypeInfo.getWritableTypeInfo((Class<? extends Writable>) clazz);
		}
		
		// check for basic types
		TypeInformation<OUT> basicTypeInfo = BasicTypeInfo.getInfoFor(clazz);
		if (basicTypeInfo != null) {
			return basicTypeInfo;
		}
		
		// check for subclasses of Value
		if (Value.class.isAssignableFrom(clazz)) {
			Class<? extends Value> valueClass = clazz.asSubclass(Value.class);
			return (TypeInformation<OUT>) ValueTypeInfo.getValueTypeInfo(valueClass);
		}
		
		// check for subclasses of Tuple
		if (Tuple.class.isAssignableFrom(clazz)) {
			throw new InvalidTypesException("Type information extraction for tuples cannot be done based on the class.");
		}

		// check for Enums
		if(Enum.class.isAssignableFrom(clazz)) {
			return (TypeInformation<OUT>) new EnumTypeInfo(clazz);
		}

		// special case for POJOs generated by Avro.
		if(SpecificRecordBase.class.isAssignableFrom(clazz)) {
			return (TypeInformation<OUT>) new AvroTypeInfo(clazz);
		}

		if (countTypeInHierarchy(typeHierarchy, clazz) > 1) {
			return new GenericTypeInfo<OUT>(clazz);
		}

		if (Modifier.isInterface(clazz.getModifiers())) {
			// Interface has no members and is therefore not handled as POJO
			return new GenericTypeInfo<OUT>(clazz);
		}

		if (clazz.equals(Class.class)) {
			// special case handling for Class, this should not be handled by the POJO logic
			return new GenericTypeInfo<OUT>(clazz);
		}

		try {
			TypeInformation<OUT> pojoType = analyzePojo(clazz, new ArrayList<Type>(typeHierarchy), parameterizedType, in1Type, in2Type);
			if (pojoType != null) {
				return pojoType;
			}
		} catch (InvalidTypesException e) {
			if(LOG.isDebugEnabled()) {
				LOG.debug("Unable to handle type "+clazz+" as POJO. Message: "+e.getMessage(), e);
			}
			// ignore and create generic type info
		}

		// return a generic type
		return new GenericTypeInfo<OUT>(clazz);
	}
	private static int countTypeInHierarchy(ArrayList<Type> typeHierarchy, Type type) {
		int count = 0;
		for (Type t : typeHierarchy) {
			if (t == type || (isClassType(type) && t == typeToClass(type))) {
				count++;
			}
		}
		return count;
	}
	protected TypeExtractor() {
		// only create instances for special use cases
	}
