	public static boolean isParameterSource(CtFieldReference<?> ref) {
		CtField<?> field = ref.getDeclaration();
		if (field == null) {
			// we must have the source of this fieldref, otherwise we cannot use it as template parameter
			return false;
		}
		if (field.getAnnotation(Parameter.class) != null) {
			//it is the template field which represents template parameter, because of "Parameter" annotation
			return true;
		}
		if (ref.getType() instanceof CtTypeParameterReference) {
			//the template fields, which are using generic type like <T>, are not template parameters
			return false;
		}
		if (ref.getSimpleName().equals("this")) {
			//the reference to this is not template parameter
			return false;
		}
		if (getTemplateParameterType(ref.getFactory()).isSubtypeOf(ref.getType())) {
			//the type of template field is or extends from class TemplateParameter.
			return true;
		}
		return false;
	}
