	private <IN1> TypeInformation<?> createTypeInfoFromInput(TypeVariable<?> returnTypeVar, ArrayList<Type> returnTypeHierarchy, 
			Type inType, TypeInformation<IN1> inTypeInfo) {
		TypeInformation<?> info = null;
		// the input is a type variable
		if (inType instanceof TypeVariable) {
			inType = materializeTypeVariable(returnTypeHierarchy, (TypeVariable<?>) inType);
			info = findCorrespondingInfo(returnTypeVar, inType, inTypeInfo);
		}
		// the input is a tuple that may contains type variables
		else if (inType instanceof ParameterizedType && Tuple.class.isAssignableFrom(((Class<?>)((ParameterizedType) inType).getRawType()))) {
			Type[] tupleElements = ((ParameterizedType) inType).getActualTypeArguments();
			// go thru all tuple elements and search for type variables
			for(int i = 0; i < tupleElements.length; i++) {
				if(tupleElements[i] instanceof TypeVariable) {
					inType = materializeTypeVariable(returnTypeHierarchy, (TypeVariable<?>) tupleElements[i]);
					info = findCorrespondingInfo(returnTypeVar, inType, ((TupleTypeInfo<?>) inTypeInfo).getTypeAt(i));
					if(info != null) {
						break;
					}
				}
			}
		}
		return info;
	}
