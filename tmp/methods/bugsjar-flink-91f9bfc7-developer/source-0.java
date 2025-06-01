	private <OUT,IN1,IN2> TypeInformation<OUT> privateGetForClass(Class<OUT> clazz, ArrayList<Type> typeHierarchy,
			ParameterizedType parameterizedType, TypeInformation<IN1> in1Type, TypeInformation<IN2> in2Type) {
		Validate.notNull(clazz);
		
		// check for abstract classes or interfaces
		if (!clazz.isPrimitive() && (Modifier.isInterface(clazz.getModifiers()) || (Modifier.isAbstract(clazz.getModifiers()) && !clazz.isArray()))) {
			throw new InvalidTypesException("Interfaces and abstract classes are not valid types: " + clazz);
		}

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
		if(Writable.class.isAssignableFrom(clazz)) {
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

		if (alreadySeen.contains(clazz)) {
			return new GenericTypeInfo<OUT>(clazz);
		}

		alreadySeen.add(clazz);

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
	private boolean isValidPojoField(Field f, Class<?> clazz, ArrayList<Type> typeHierarchy) {
		if(Modifier.isPublic(f.getModifiers())) {
			return true;
		} else {
			boolean hasGetter = false, hasSetter = false;
			final String fieldNameLow = f.getName().toLowerCase();
			
			Type fieldType = f.getGenericType();
			TypeVariable<?> fieldTypeGeneric = null;
			if(fieldType instanceof TypeVariable) {
				fieldTypeGeneric = (TypeVariable<?>) fieldType;
				fieldType = materializeTypeVariable(typeHierarchy, (TypeVariable<?>)fieldType);
			}
			for(Method m : clazz.getMethods()) {
				// check for getter
				if(	// The name should be "get<FieldName>" or "<fieldName>" (for scala) or "is<fieldName>" for boolean fields.
					(m.getName().toLowerCase().equals("get"+fieldNameLow) || m.getName().toLowerCase().equals("is"+fieldNameLow) || m.getName().toLowerCase().equals(fieldNameLow)) &&
					// no arguments for the getter
					m.getParameterTypes().length == 0 &&
					// return type is same as field type (or the generic variant of it)
					(m.getGenericReturnType().equals( fieldType ) || (fieldTypeGeneric != null && m.getGenericReturnType().equals(fieldTypeGeneric)) )
				) {
					if(hasGetter) {
						throw new IllegalStateException("Detected more than one getter");
					}
					hasGetter = true;
				}
				// check for setters (<FieldName>_$eq for scala)
				if((m.getName().toLowerCase().equals("set"+fieldNameLow) || m.getName().toLowerCase().equals(fieldNameLow+"_$eq")) &&
					m.getParameterTypes().length == 1 && // one parameter of the field's type
					( m.getGenericParameterTypes()[0].equals( fieldType ) || (fieldTypeGeneric != null && m.getGenericParameterTypes()[0].equals(fieldTypeGeneric) ) )&&
					// return type is void.
					m.getReturnType().equals(Void.TYPE)
				) {
					if(hasSetter) {
						throw new IllegalStateException("Detected more than one setter");
					}
					hasSetter = true;
				}
			}
			if(hasGetter && hasSetter) {
				return true;
			} else {
				if(!hasGetter) {
					LOG.warn("Class "+clazz+" does not contain a getter for field "+f.getName() );
				}
				if(!hasSetter) {
					LOG.warn("Class "+clazz+" does not contain a setter for field "+f.getName() );
				}
				return false;
			}
		}
	}
	private <OUT, IN1, IN2> TypeInformation<OUT> analyzePojo(Class<OUT> clazz, ArrayList<Type> typeHierarchy,
			ParameterizedType parameterizedType, TypeInformation<IN1> in1Type, TypeInformation<IN2> in2Type) {
		// add the hierarchy of the POJO itself if it is generic
		if (parameterizedType != null) {
			getTypeHierarchy(typeHierarchy, parameterizedType, Object.class);
		}
		// create a type hierarchy, if the incoming only contains the most bottom one or none.
		else if(typeHierarchy.size() <= 1) {
			getTypeHierarchy(typeHierarchy, clazz, Object.class);
		}
		
		List<Field> fields = getAllDeclaredFields(clazz);
		List<PojoField> pojoFields = new ArrayList<PojoField>();
		for (Field field : fields) {
			Type fieldType = field.getGenericType();
			if(!isValidPojoField(field, clazz, typeHierarchy)) {
				LOG.warn("Class "+clazz+" is not a valid POJO type");
				return null;
			}
			try {
				ArrayList<Type> fieldTypeHierarchy = new ArrayList<Type>(typeHierarchy);
				fieldTypeHierarchy.add(fieldType);
				TypeInformation<?> ti = createTypeInfoWithTypeHierarchy(fieldTypeHierarchy, fieldType, in1Type, in2Type);
				pojoFields.add(new PojoField(field, ti));
			} catch (InvalidTypesException e) {
				Class<?> genericClass = Object.class;
				if(isClassType(fieldType)) {
					genericClass = typeToClass(fieldType);
				}
				pojoFields.add(new PojoField(field, new GenericTypeInfo<OUT>((Class<OUT>) genericClass)));
			}
		}

		CompositeType<OUT> pojoType = new PojoTypeInfo<OUT>(clazz, pojoFields);

		//
		// Validate the correctness of the pojo.
		// returning "null" will result create a generic type information.
		//
		List<Method> methods = getAllDeclaredMethods(clazz);
		for (Method method : methods) {
			if (method.getName().equals("readObject") || method.getName().equals("writeObject")) {
				LOG.warn("Class "+clazz+" contains custom serialization methods we do not call.");
				return null;
			}
		}

		// Try retrieving the default constructor, if it does not have one
		// we cannot use this because the serializer uses it.
		try {
			clazz.getDeclaredConstructor();
		} catch (NoSuchMethodException e) {
			LOG.warn("Class " + clazz + " must have a default constructor to be used as a POJO.");
			return null;
		}
		
		// everything is checked, we return the pojo
		return pojoType;
	}
	private static Type getTypeHierarchy(ArrayList<Type> typeHierarchy, Type curT, Class<?> stopAtClass) {
		// skip first one
		if (typeHierarchy.size() > 0 && typeHierarchy.get(0) == curT && isClassType(curT)) {
			curT = typeToClass(curT).getGenericSuperclass();
		}
		while (!(isClassType(curT) && typeToClass(curT).equals(stopAtClass))) {
			typeHierarchy.add(curT);
			curT = typeToClass(curT).getGenericSuperclass();
		}
		return curT;
	}
