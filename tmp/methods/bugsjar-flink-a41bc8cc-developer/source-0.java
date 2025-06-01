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
					LOG.debug("Class "+clazz+" does not contain a getter for field "+f.getName() );
				}
				if(!hasSetter) {
					LOG.debug("Class "+clazz+" does not contain a setter for field "+f.getName() );
				}
				return false;
			}
		}
	}
	public static List<Field> getAllDeclaredFields(Class<?> clazz) {
		List<Field> result = new ArrayList<Field>();
		while (clazz != null) {
			Field[] fields = clazz.getDeclaredFields();
			for (Field field : fields) {
				if(Modifier.isTransient(field.getModifiers()) || Modifier.isStatic(field.getModifiers())) {
					continue; // we have no use for transient or static fields
				}
				if(hasFieldWithSameName(field.getName(), result)) {
					throw new RuntimeException("The field "+field+" is already contained in the hierarchy of the class "+clazz+"."
							+ "Please use unique field names through your classes hierarchy");
				}
				result.add(field);
			}
			clazz = clazz.getSuperclass();
		}
		return result;
	}
	protected <OUT, IN1, IN2> TypeInformation<OUT> analyzePojo(Class<OUT> clazz, ArrayList<Type> typeHierarchy,
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
		if(fields.size() == 0) {
			LOG.info("No fields detected for class " + clazz + ". Cannot be used as a PojoType. Will be handled as GenericType");
			return new GenericTypeInfo<OUT>(clazz);
		}

		List<PojoField> pojoFields = new ArrayList<PojoField>();
		for (Field field : fields) {
			Type fieldType = field.getGenericType();
			if(!isValidPojoField(field, clazz, typeHierarchy)) {
				LOG.info("Class " + clazz + " is not a valid POJO type");
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
				LOG.info("Class "+clazz+" contains custom serialization methods we do not call.");
				return null;
			}
		}

		// Try retrieving the default constructor, if it does not have one
		// we cannot use this because the serializer uses it.
		try {
			clazz.getDeclaredConstructor();
		} catch (NoSuchMethodException e) {
			if (clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers())) {
				LOG.info("Class " + clazz + " is abstract or an interface, having a concrete " +
						"type can increase performance.");
			} else {
				LOG.info("Class " + clazz + " must have a default constructor to be used as a POJO.");
				return null;
			}
		}
		
		// everything is checked, we return the pojo
		return pojoType;
	}
