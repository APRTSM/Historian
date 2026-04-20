	private boolean isValidPojoField(Field f, Class<?> clazz, ArrayList<Type> typeHierarchy) {
		if(Modifier.isPublic(f.getModifiers())) {
			return true;
		} else {
			boolean hasGetter = false, hasSetter = false;
			final String fieldNameLow = f.getName().toLowerCase().replaceAll("_", "");

			Type fieldType = f.getGenericType();
			Class<?> fieldTypeWrapper = ClassUtils.primitiveToWrapper(f.getType());

			TypeVariable<?> fieldTypeGeneric = null;
			if(fieldType instanceof TypeVariable) {
				fieldTypeGeneric = (TypeVariable<?>) fieldType;
				fieldType = materializeTypeVariable(typeHierarchy, (TypeVariable<?>)fieldType);
			}
			for(Method m : clazz.getMethods()) {
				final String methodNameLow = m.getName().toLowerCase().replaceAll("_", "");

				// check for getter
				if(	// The name should be "get<FieldName>" or "<fieldName>" (for scala) or "is<fieldName>" for boolean fields.
					(methodNameLow.equals("get"+fieldNameLow) || methodNameLow.equals("is"+fieldNameLow) || methodNameLow.equals(fieldNameLow)) &&
					// no arguments for the getter
					m.getParameterTypes().length == 0 &&
					// return type is same as field type (or the generic variant of it)
					(m.getGenericReturnType().equals( fieldType ) || (fieldTypeWrapper != null && m.getReturnType().equals( fieldTypeWrapper )) || (fieldTypeGeneric != null && m.getGenericReturnType().equals(fieldTypeGeneric)) )
				) {
					if(hasGetter) {
						throw new IllegalStateException("Detected more than one getter");
					}
					hasGetter = true;
				}
				// check for setters (<FieldName>_$eq for scala)
				if((methodNameLow.equals("set"+fieldNameLow) || methodNameLow.equals(fieldNameLow+"_$eq")) &&
					m.getParameterTypes().length == 1 && // one parameter of the field's type
					(m.getGenericParameterTypes()[0].equals( fieldType ) || (fieldTypeWrapper != null && m.getParameterTypes()[0].equals( fieldTypeWrapper )) || (fieldTypeGeneric != null && m.getGenericParameterTypes()[0].equals(fieldTypeGeneric) ) )&&
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
					LOG.debug(clazz+" does not contain a getter for field "+f.getName() );
				}
				if(!hasSetter) {
					LOG.debug(clazz+" does not contain a setter for field "+f.getName() );
				}
				return false;
			}
		}
	}
