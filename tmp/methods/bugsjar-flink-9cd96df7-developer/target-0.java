	private static TypeInformation<?> parse(StringBuilder sb) throws ClassNotFoundException {
		String infoString = sb.toString();
		final Matcher tupleMatcher = tuplePattern.matcher(infoString);

		final Matcher writableMatcher = writablePattern.matcher(infoString);
		final Matcher enumMatcher = enumPattern.matcher(infoString);

		final Matcher basicTypeMatcher = basicTypePattern.matcher(infoString);
		final Matcher basicTypeDateMatcher = basicTypeDatePattern.matcher(infoString);
		final Matcher basicType2Matcher = basicType2Pattern.matcher(infoString);

		final Matcher valueTypeMatcher = valueTypePattern.matcher(infoString);

		final Matcher basicArrayTypeMatcher = basicArrayTypePattern.matcher(infoString);
		final Matcher basicArrayType2Matcher = basicArrayType2Pattern.matcher(infoString);

		final Matcher pojoGenericMatcher = pojoGenericObjectPattern.matcher(infoString);

		if (infoString.length() == 0) {
			return null;
		}

		TypeInformation<?> returnType = null;

		// tuples
		if (tupleMatcher.find()) {
			String className = tupleMatcher.group(1);
			sb.delete(0, className.length() + 1);
			int arity = Integer.parseInt(className.replaceAll("\\D", ""));

			Class<?> clazz;
			// check if fully qualified
			if (className.startsWith(TUPLE_PACKAGE)) {
				clazz = Class.forName(className);
			} else {
				clazz = Class.forName(TUPLE_PACKAGE + "." + className);
			}

			TypeInformation<?>[] types = new TypeInformation<?>[arity];
			for (int i = 0; i < arity; i++) {
				types[i] = parse(sb);
				if (types[i] == null) {
					throw new IllegalArgumentException("Tuple arity does not match given parameters.");
				}
			}
			if (sb.charAt(0) != '>') {
				throw new IllegalArgumentException("Tuple arity does not match given parameters.");
			}
			// remove '>'
			sb.deleteCharAt(0);

			// tuple arrays
			if (sb.length() > 0) {
				if (sb.length() >= 2 && sb.charAt(0) == '[' && sb.charAt(1) == ']') {
					Class<?> arrayClazz;
					// check if fully qualified
					if (className.startsWith(TUPLE_PACKAGE)) {
						arrayClazz = Class.forName("[L" + className + ";");
					} else {
						arrayClazz = Class.forName("[L" + TUPLE_PACKAGE + "." + className + ";");
					}
					returnType = ObjectArrayTypeInfo.getInfoFor(arrayClazz, new TupleTypeInfo(clazz, types));
				} else if (sb.length() < 1 || sb.charAt(0) != '[') {
					returnType = new TupleTypeInfo(clazz, types);
				}
			} else {
				returnType = new TupleTypeInfo(clazz, types);
			}
		}
		// writable types
		else if (writableMatcher.find()) {
			String className = writableMatcher.group(1);
			String fullyQualifiedName = writableMatcher.group(3);
			sb.delete(0, className.length() + 1 + fullyQualifiedName.length() + 1);
			Class<?> clazz = loadClass(fullyQualifiedName);
			returnType = WritableTypeInfo.getWritableTypeInfo((Class) clazz);
		}
		// enum types
		else if (enumMatcher.find()) {
			String className = enumMatcher.group(1);
			String fullyQualifiedName = enumMatcher.group(3);
			sb.delete(0, className.length() + 1 + fullyQualifiedName.length() + 1);
			Class<?> clazz = loadClass(fullyQualifiedName);
			returnType = new EnumTypeInfo(clazz);
		}
		// basic types of classes
		else if (basicTypeMatcher.find()) {
			String className = basicTypeMatcher.group(1);
			sb.delete(0, className.length());
			Class<?> clazz;
			// check if fully qualified
			if (className.startsWith("java.lang")) {
				clazz = Class.forName(className);
			} else {
				clazz = Class.forName("java.lang." + className);
			}
			returnType = BasicTypeInfo.getInfoFor(clazz);
		}
		// special basic type "Date"
		else if (basicTypeDateMatcher.find()) {
			String className = basicTypeDateMatcher.group(1);
			sb.delete(0, className.length());
			Class<?> clazz;
			// check if fully qualified
			if (className.startsWith("java.util")) {
				clazz = Class.forName(className);
			} else {
				clazz = Class.forName("java.util." + className);
			}
			returnType = BasicTypeInfo.getInfoFor(clazz);
		}
		// basic type of primitives
		else if (basicType2Matcher.find()) {
			String className = basicType2Matcher.group(1);
			sb.delete(0, className.length());

			Class<?> clazz = null;
			if (className.equals("int")) {
				clazz = Integer.class;
			} else if (className.equals("byte")) {
				clazz = Byte.class;
			} else if (className.equals("short")) {
				clazz = Short.class;
			} else if (className.equals("char")) {
				clazz = Character.class;
			} else if (className.equals("double")) {
				clazz = Double.class;
			} else if (className.equals("float")) {
				clazz = Float.class;
			} else if (className.equals("long")) {
				clazz = Long.class;
			} else if (className.equals("boolean")) {
				clazz = Boolean.class;
			} else if (className.equals("void")) {
				clazz = Void.class;
			}
			returnType = BasicTypeInfo.getInfoFor(clazz);
		}
		// values
		else if (valueTypeMatcher.find()) {
			String className = valueTypeMatcher.group(1);
			sb.delete(0, className.length() + 5);

			Class<?> clazz;
			// check if fully qualified
			if (className.startsWith(VALUE_PACKAGE)) {
				clazz = Class.forName(className + "Value");
			} else {
				clazz = Class.forName(VALUE_PACKAGE + "." + className + "Value");
			}
			returnType = ValueTypeInfo.getValueTypeInfo((Class<Value>) clazz);
		}
		// array of basic classes
		else if (basicArrayTypeMatcher.find()) {
			String className = basicArrayTypeMatcher.group(1);
			sb.delete(0, className.length() + 2);

			Class<?> clazz;
			if (className.startsWith("java.lang")) {
				clazz = Class.forName("[L" + className + ";");
			} else {
				clazz = Class.forName("[Ljava.lang." + className + ";");
			}
			returnType = BasicArrayTypeInfo.getInfoFor(clazz);
		}
		// array of primitives
		else if (basicArrayType2Matcher.find()) {
			String className = basicArrayType2Matcher.group(1);
			sb.delete(0, className.length() + 2);

			Class<?> clazz = null;
			if (className.equals("int")) {
				clazz = int[].class;
			} else if (className.equals("byte")) {
				clazz = byte[].class;
			} else if (className.equals("short")) {
				clazz = short[].class;
			} else if (className.equals("char")) {
				clazz = char[].class;
			} else if (className.equals("double")) {
				clazz = double[].class;
			} else if (className.equals("float")) {
				clazz = float[].class;
			} else if (className.equals("long")) {
				clazz = long[].class;
			} else if (className.equals("boolean")) {
				clazz = boolean[].class;
			}
			returnType = PrimitiveArrayTypeInfo.getInfoFor(clazz);
		}
		// pojo objects or generic types
		else if (pojoGenericMatcher.find()) {
			String fullyQualifiedName = pojoGenericMatcher.group(1);
			sb.delete(0, fullyQualifiedName.length());

			boolean isPojo = pojoGenericMatcher.group(2) != null;

			if (isPojo) {
				sb.deleteCharAt(0);
				Class<?> clazz = loadClass(fullyQualifiedName);

				ArrayList<PojoField> fields = new ArrayList<PojoField>();
				while (sb.charAt(0) != '>') {
					final Matcher fieldMatcher = fieldPattern.matcher(sb);
					if (!fieldMatcher.find()) {
						throw new IllegalArgumentException("Field name missing.");
					}
					String fieldName = fieldMatcher.group(1);
					sb.delete(0, fieldName.length() + 1);

					Field field = null;
					try {
						field = clazz.getDeclaredField(fieldName);
					} catch (Exception e) {
						throw new IllegalArgumentException("Field '" + fieldName + "'could not be accessed.");
					}
					fields.add(new PojoField(field, parse(sb)));
				}
				returnType = new PojoTypeInfo(clazz, fields);
			}
			else {
				// custom object array
				if (fullyQualifiedName.endsWith("[]")) {
					fullyQualifiedName = fullyQualifiedName.substring(0, fullyQualifiedName.length() - 2);
					returnType = ObjectArrayTypeInfo.getInfoFor(loadClass("[L" + fullyQualifiedName + ";"));
				} else {
					returnType = new GenericTypeInfo(loadClass(fullyQualifiedName));
				}
			}
		}

		if (returnType == null) {
			throw new IllegalArgumentException("Error at '" + infoString + "'");
		} else {
			// remove possible ','
			if (sb.length() > 0 && sb.charAt(0) == ',') {
				sb.deleteCharAt(0);
			}
			return returnType;
		}
	}
