	private CtExpression convertValueToExpression(Object value) {
		CtExpression res;
		if (value.getClass().isArray()) {
			// Value should be converted to a CtNewArray.
			res = getFactory().Core().createNewArray();
			Object[] values = (Object[]) value;

			res.setType(getFactory().Type().createArrayReference(getFactory().Type().createReference(value.getClass().getComponentType())));
			for (Object o : values) {
				((CtNewArray) res).addElement(convertValueToExpression(o));
			}
		} else if (value instanceof Collection) {
			// Value should be converted to a CtNewArray.
			res = getFactory().Core().createNewArray();
			Collection values = (Collection) value;
			res.setType(getFactory().Type().createArrayReference(getFactory().Type().createReference(values.toArray()[0].getClass())));
			for (Object o : values) {
				((CtNewArray) res).addElement(convertValueToExpression(o));
			}
		} else if (value instanceof Class) {
			// Value should be a field access to a .class.
			res = getFactory().Code().createClassAccess(getFactory().Type().createReference((Class) value));
		} else if (value instanceof Field) {
			// Value should be a field access to a field.
			CtFieldReference<Object> variable = getFactory().Field().createReference((Field) value);
			variable.setStatic(true);
			CtTypeAccess target = getFactory().Code().createTypeAccess(getFactory().Type().createReference(((Field) value).getDeclaringClass()));
			CtFieldRead fieldRead = getFactory().Core().createFieldRead();
			fieldRead.setVariable(variable);
			fieldRead.setTarget(target);
			fieldRead.setType(target.getAccessedType());
			res = fieldRead;
		} else if (isPrimitive(value.getClass()) || value instanceof String) {
			// Value should be a literal.
			res = getFactory().Code().createLiteral(value);
		} else if (value.getClass().isEnum()) {
			final CtTypeReference declaringClass = getFactory().Type().createReference(((Enum) value).getDeclaringClass());
			final CtFieldReference variableRef = getFactory().Field().createReference(declaringClass, declaringClass, ((Enum) value).name());
			CtTypeAccess target = getFactory().Code().createTypeAccess(declaringClass);
			CtFieldRead fieldRead = getFactory().Core().createFieldRead();
			fieldRead.setVariable(variableRef);
			fieldRead.setTarget(target);
			fieldRead.setType(declaringClass);
			res = fieldRead;
		} else {
			throw new SpoonException("Please, submit a valid value.");
		}
		return res;
	}
