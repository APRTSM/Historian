	private <IN1, IN2> TypeInformation<?> createTypeInfoFromInput(TypeVariable<?> returnTypeVar, ArrayList<Type> returnTypeHierarchy, 
			TypeInformation<IN1> in1TypeInfo, TypeInformation<IN2> in2TypeInfo) {
		
		Type matReturnTypeVar = materializeTypeVariable(returnTypeHierarchy, returnTypeVar);
		
		// variable could be resolved
		if (!(matReturnTypeVar instanceof TypeVariable)) {
			return createTypeInfoWithTypeHierarchy(returnTypeHierarchy, matReturnTypeVar, in1TypeInfo, in2TypeInfo);
		}
		else {
			returnTypeVar = (TypeVariable<?>) matReturnTypeVar;
		}
		
		TypeInformation<?> info = null;
		if (in1TypeInfo != null) {
			// find the deepest type variable that describes the type of input 1
			ParameterizedType baseClass = (ParameterizedType) returnTypeHierarchy.get(returnTypeHierarchy.size() - 1 );
			Type in1Type = baseClass.getActualTypeArguments()[0];
			if (in1Type instanceof TypeVariable) {
				in1Type = materializeTypeVariable(returnTypeHierarchy, (TypeVariable<?>) in1Type);
				info = findCorrespondingInfo(returnTypeVar, in1Type, in1TypeInfo);
			}
		}
		
		if (info == null && in2TypeInfo != null) {
			// find the deepest type variable that describes the type of input 2
			ParameterizedType baseClass = (ParameterizedType) returnTypeHierarchy.get(returnTypeHierarchy.size() - 1 );
			Type in2Type = baseClass.getActualTypeArguments()[1];
			if (in2Type instanceof TypeVariable) {
				in2Type = materializeTypeVariable(returnTypeHierarchy, (TypeVariable<?>) in2Type);
				info = findCorrespondingInfo(returnTypeVar, in2Type, in2TypeInfo);
			}
		}
		
		if (info != null) {
			return info;
		}
		
		return null;
	}
	private <IN1, IN2, OUT> TypeInformation<OUT> createTypeInfoWithTypeHierarchy(ArrayList<Type> typeHierarchy, Type t,
			TypeInformation<IN1> in1Type, TypeInformation<IN2> in2Type) {
		
		// check if type is a subclass of tuple
		if ((t instanceof Class<?> && Tuple.class.isAssignableFrom((Class<?>) t))
				|| (t instanceof ParameterizedType && Tuple.class.isAssignableFrom((Class<?>) ((ParameterizedType) t).getRawType()))) {
			
			Type curT = t;
			
			// do not allow usage of Tuple as type
			if (curT instanceof Class<?> && ((Class<?>) curT).equals(Tuple.class)) {
				throw new InvalidTypesException(
						"Usage of class Tuple as a type is not allowed. Use a concrete subclass (e.g. Tuple1, Tuple2, etc.) instead.");
			}
			
			// go up the hierarchy until we reach immediate child of Tuple (with or without generics)
			// collect the types while moving up for a later top-down 
			while (!(curT instanceof ParameterizedType && ((Class<?>) ((ParameterizedType) curT).getRawType()).getSuperclass().equals(
					Tuple.class))
					&& !(curT instanceof Class<?> && ((Class<?>) curT).getSuperclass().equals(Tuple.class))) {
				typeHierarchy.add(curT);
				
				// parameterized type
				if (curT instanceof ParameterizedType) {
					curT = ((Class<?>) ((ParameterizedType) curT).getRawType()).getGenericSuperclass();
				}
				// class
				else {
					curT = ((Class<?>) curT).getGenericSuperclass();
				}
			}
			
			// check if immediate child of Tuple has generics
			if (curT instanceof Class<?>) {
				throw new InvalidTypesException("Tuple needs to be parameterized by using generics.");
			}
			
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
				// sub type could not be determined with materializing
				// try to derive the type info of the TypeVariable from the immediate base child input as a last attempt
				if (subtypes[i] instanceof TypeVariable<?>) {
					tupleSubTypes[i] = createTypeInfoFromInput((TypeVariable<?>) subtypes[i], typeHierarchy, in1Type, in2Type);
					
					// variable could not be determined
					if (tupleSubTypes[i] == null) {
						throw new InvalidTypesException("Type of TypeVariable '" + ((TypeVariable<?>) subtypes[i]).getName() + "' in '"
								+ ((TypeVariable<?>) subtypes[i]).getGenericDeclaration()
								+ "' could not be determined. This is most likely a type erasure problem. "
								+ "The type extraction currently supports types with generic variables only in cases where "
								+ "all variables in the return type can be deduced from the input type(s).");
					}
				} else {
					tupleSubTypes[i] = createTypeInfoWithTypeHierarchy(new ArrayList<Type>(typeHierarchy), subtypes[i], in1Type, in2Type);
				}
			}
			
			// TODO: Check that type that extends Tuple does not have additional fields.
			// Right now, these fields are not be serialized by the TupleSerializer. 
			// We might want to add an ExtendedTupleSerializer for that. 
			
			if (t instanceof Class<?>) {
				return new TupleTypeInfo(((Class<? extends Tuple>) t), tupleSubTypes);
			} else if (t instanceof ParameterizedType) {
				return new TupleTypeInfo(((Class<? extends Tuple>) ((ParameterizedType) t).getRawType()), tupleSubTypes);
			}
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
				TypeInformation<OUT> typeInfo = (TypeInformation<OUT>) createTypeInfoFromInput((TypeVariable<?>) t, typeHierarchy, in1Type, in2Type);
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
				
				Class<OUT> classArray = null;
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
		// objects with generics are treated as raw type
		else if (t instanceof ParameterizedType) {
			return privateGetForClass((Class<OUT>) ((ParameterizedType) t).getRawType());
		}
		// no tuple, no TypeVariable, no generic type
		else if (t instanceof Class) {
			return privateGetForClass((Class<OUT>) t);
		}
		
		throw new InvalidTypesException("Type Information could not be created.");
	}
	private <IN1, IN2, OUT> TypeInformation<OUT> privateCreateTypeInfo(Class<?> baseClass, Class<?> clazz, int returnParamPos,
			TypeInformation<IN1> in1Type, TypeInformation<IN2> in2Type) {
		ArrayList<Type> typeHierarchy = new ArrayList<Type>();
		Type returnType = getParameterType(baseClass, typeHierarchy, clazz, returnParamPos);
		
		TypeInformation<OUT> typeInfo = null;
		
		// return type is a variable -> try to get the type info from the input directly
		if (returnType instanceof TypeVariable<?>) {
			typeInfo = (TypeInformation<OUT>) createTypeInfoFromInput((TypeVariable<?>) returnType, typeHierarchy, in1Type, in2Type);
			
			if (typeInfo != null) {
				return typeInfo;
			}
		}
		
		// get info from hierarchy
		return (TypeInformation<OUT>) createTypeInfoWithTypeHierarchy(typeHierarchy, returnType, in1Type, in2Type);
	}
