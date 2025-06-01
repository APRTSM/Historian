	private static <T> void checkTemplateContracts(CtClass<T> c) {
		for (CtField f : c.getFields()) {
			Parameter templateParamAnnotation = f.getAnnotation(Parameter.class);
			if (templateParamAnnotation != null && !templateParamAnnotation.value().equals("")) {
				String proxyName = templateParamAnnotation.value();
				// contract: if value, then the field type must be String
				if (!f.getType().equals(c.getFactory().Type().STRING)) {
					throw new TemplateException("proxy template parameter must be typed as String " +  f.getType().getQualifiedName());
				}

				// contract: the name of the template parameter must correspond to the name of the field
				// as found, by Pavel, this is not good contract because it prevents easy refactoring of templates
				// we remove it but keep th commented code in case somebody would come up with this bad idae
//				if (!f.getSimpleName().equals("_" + f.getAnnotation(Parameter.class).value())) {
//					throw new TemplateException("the field name of a proxy template parameter must be called _" + f.getSimpleName());
//				}

				// contract: if a proxy parameter is declared and named "x" (@Parameter("x")), then a type member named "x" must exist.
				boolean found = false;
				for (CtTypeMember member: c.getTypeMembers()) {
					if (member.getSimpleName().equals(proxyName)) {
						found = true;
					}
				}
				if (!found) {
					throw new TemplateException("if a proxy parameter is declared and named \"" + proxyName + "\", then a type member named \"\" + proxyName + \"\" must exist.");
				}

			}
		}
	}
