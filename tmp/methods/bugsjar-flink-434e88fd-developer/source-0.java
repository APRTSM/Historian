	private static void validateInfo(ArrayList<Type> typeHierarchy, Type type, TypeInformation<?> typeInfo) {
		if (type == null) {
			throw new InvalidTypesException("Unknown Error. Type is null.");
		}
		
		if (typeInfo == null) {
			throw new InvalidTypesException("Unknown Error. TypeInformation is null.");
		}
		
		if (!(type instanceof TypeVariable<?>)) {
			// check for basic type
			if (typeInfo.isBasicType()) {
				
				TypeInformation<?> actual;
				// check if basic type at all
				if (!(type instanceof Class<?>) || (actual = BasicTypeInfo.getInfoFor((Class<?>) type)) == null) {
					throw new InvalidTypesException("Basic type expected.");
				}
				// check if correct basic type
				if (!typeInfo.equals(actual)) {
					throw new InvalidTypesException("Basic type '" + typeInfo + "' expected but was '" + actual + "'.");
				}
				
			}
			// check for tuple
			else if (typeInfo.isTupleType()) {
				// check if tuple at all
				if (!(isClassType(type) && Tuple.class.isAssignableFrom(typeToClass(type)))) {
					throw new InvalidTypesException("Tuple type expected.");
				}
				
				// do not allow usage of Tuple as type
				if (isClassType(type) && typeToClass(type).equals(Tuple.class)) {
					throw new InvalidTypesException("Concrete subclass of Tuple expected.");
				}
				
				// go up the hierarchy until we reach immediate child of Tuple (with or without generics)
				while (!(isClassType(type) && typeToClass(type).getSuperclass().equals(Tuple.class))) {
					typeHierarchy.add(type);
					type = typeToClass(type).getGenericSuperclass();
				}
				
				if(type == Tuple0.class) {
					return;
				}
				
				// check if immediate child of Tuple has generics
				if (type instanceof Class<?>) {
					throw new InvalidTypesException("Parameterized Tuple type expected.");
				}
				
				TupleTypeInfo<?> tti = (TupleTypeInfo<?>) typeInfo;
				
				Type[] subTypes = ((ParameterizedType) type).getActualTypeArguments();
				
				if (subTypes.length != tti.getArity()) {
					throw new InvalidTypesException("Tuple arity '" + tti.getArity() + "' expected but was '"
							+ subTypes.length + "'.");
				}
				
				for (int i = 0; i < subTypes.length; i++) {
					validateInfo(new ArrayList<Type>(typeHierarchy), subTypes[i], tti.getTypeAt(i));
				}
			}
			// check for Either
			else if (typeInfo instanceof EitherTypeInfo) {
				// check if Either at all
				if (!(isClassType(type) && Either.class.isAssignableFrom(typeToClass(type)))) {
					throw new InvalidTypesException("Either type expected.");
				}

				// go up the hierarchy until we reach Either (with or without generics)
				while (!(isClassType(type) && typeToClass(type).equals(Either.class))) {
					typeHierarchy.add(type);
					type = typeToClass(type).getGenericSuperclass();
				}

				// check if Either has generics
				if (type instanceof Class<?>) {
					throw new InvalidTypesException("Parameterized Either type expected.");
				}

				EitherTypeInfo<?, ?> eti = (EitherTypeInfo<?, ?>) typeInfo;
				Type[] subTypes = ((ParameterizedType) type).getActualTypeArguments();
				validateInfo(new ArrayList<Type>(typeHierarchy), subTypes[0], eti.getLeftType());
				validateInfo(new ArrayList<Type>(typeHierarchy), subTypes[1], eti.getRightType());
			}
			// check for Writable
			else if (typeInfo instanceof WritableTypeInfo<?>) {
				// check if writable at all
				if (!(type instanceof Class<?> && Writable.class.isAssignableFrom((Class<?>) type))) {
					throw new InvalidTypesException("Writable type expected.");
				}
				
				// check writable type contents
				Class<?> clazz;
				if (((WritableTypeInfo<?>) typeInfo).getTypeClass() != (clazz = (Class<?>) type)) {
					throw new InvalidTypesException("Writable type '"
							+ ((WritableTypeInfo<?>) typeInfo).getTypeClass().getCanonicalName() + "' expected but was '"
							+ clazz.getCanonicalName() + "'.");
				}
			}
			// check for primitive array
			else if (typeInfo instanceof PrimitiveArrayTypeInfo) {
				Type component;
				// check if array at all
				if (!(type instanceof Class<?> && ((Class<?>) type).isArray() && (component = ((Class<?>) type).getComponentType()) != null)
						&& !(type instanceof GenericArrayType && (component = ((GenericArrayType) type).getGenericComponentType()) != null)) {
					throw new InvalidTypesException("Array type expected.");
				}
				if (component instanceof TypeVariable<?>) {
					component = materializeTypeVariable(typeHierarchy, (TypeVariable<?>) component);
					if (component instanceof TypeVariable) {
						return;
					}
				}
				if (!(component instanceof Class<?> && ((Class<?>)component).isPrimitive())) {
					throw new InvalidTypesException("Primitive component expected.");
				}
			}
			// check for basic array
			else if (typeInfo instanceof BasicArrayTypeInfo<?, ?>) {
				Type component;
				// check if array at all
				if (!(type instanceof Class<?> && ((Class<?>) type).isArray() && (component = ((Class<?>) type).getComponentType()) != null)
						&& !(type instanceof GenericArrayType && (component = ((GenericArrayType) type).getGenericComponentType()) != null)) {
					throw new InvalidTypesException("Array type expected.");
				}
				
				if (component instanceof TypeVariable<?>) {
					component = materializeTypeVariable(typeHierarchy, (TypeVariable<?>) component);
					if (component instanceof TypeVariable) {
						return;
					}
				}
				
				validateInfo(typeHierarchy, component, ((BasicArrayTypeInfo<?, ?>) typeInfo).getComponentInfo());
				
			}
			// check for object array
			else if (typeInfo instanceof ObjectArrayTypeInfo<?, ?>) {
				// check if array at all
				if (!(type instanceof Class<?> && ((Class<?>) type).isArray()) && !(type instanceof GenericArrayType)) {
					throw new InvalidTypesException("Object array type expected.");
				}
				
				// check component
				Type component;
				if (type instanceof Class<?>) {
					component = ((Class<?>) type).getComponentType();
				} else {
					component = ((GenericArrayType) type).getGenericComponentType();
				}
				
				if (component instanceof TypeVariable<?>) {
					component = materializeTypeVariable(typeHierarchy, (TypeVariable<?>) component);
					if (component instanceof TypeVariable) {
						return;
					}
				}
				
				validateInfo(typeHierarchy, component, ((ObjectArrayTypeInfo<?, ?>) typeInfo).getComponentInfo());
			}
			// check for value
			else if (typeInfo instanceof ValueTypeInfo<?>) {
				// check if value at all
				if (!(type instanceof Class<?> && Value.class.isAssignableFrom((Class<?>) type))) {
					throw new InvalidTypesException("Value type expected.");
				}
				
				TypeInformation<?> actual;
				// check value type contents
				if (!((ValueTypeInfo<?>) typeInfo).equals(actual = ValueTypeInfo.getValueTypeInfo((Class<? extends Value>) type))) {
					throw new InvalidTypesException("Value type '" + typeInfo + "' expected but was '" + actual + "'.");
				}
			}
			// check for POJO
			else if (typeInfo instanceof PojoTypeInfo) {
				Class<?> clazz = null;
				if (!(isClassType(type) && ((PojoTypeInfo<?>) typeInfo).getTypeClass() == (clazz = typeToClass(type)))) {
					throw new InvalidTypesException("POJO type '"
							+ ((PojoTypeInfo<?>) typeInfo).getTypeClass().getCanonicalName() + "' expected but was '"
							+ clazz.getCanonicalName() + "'.");
				}
			}
			// check for Enum
			else if (typeInfo instanceof EnumTypeInfo) {
				if (!(type instanceof Class<?> && Enum.class.isAssignableFrom((Class<?>) type))) {
					throw new InvalidTypesException("Enum type expected.");
				}
				// check enum type contents
				if (!(typeInfo.getTypeClass() == type)) {
					throw new InvalidTypesException("Enum type '" + typeInfo.getTypeClass().getCanonicalName() + "' expected but was '"
							+ typeToClass(type).getCanonicalName() + "'.");
				}
			}
			// check for generic object
			else if (typeInfo instanceof GenericTypeInfo<?>) {
				Class<?> clazz = null;
				if (!(isClassType(type) && ((GenericTypeInfo<?>) typeInfo).getTypeClass() == (clazz = typeToClass(type)))) {
					throw new InvalidTypesException("Generic object type '"
							+ ((GenericTypeInfo<?>) typeInfo).getTypeClass().getCanonicalName() + "' expected but was '"
							+ clazz.getCanonicalName() + "'.");
				}
			}
		} else {
			type = materializeTypeVariable(typeHierarchy, (TypeVariable<?>) type);
			if (!(type instanceof TypeVariable)) {
				validateInfo(typeHierarchy, type, typeInfo);
			}
		}
	}
