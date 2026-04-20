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
					LOG.debug(clazz+" does not contain a getter for field "+f.getName() );
				}
				if(!hasSetter) {
					LOG.debug(clazz+" does not contain a setter for field "+f.getName() );
				}
				return false;
			}
		}
	}
