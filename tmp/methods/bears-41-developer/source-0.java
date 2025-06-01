	private <T> CtTypeReference<T> getTypeReference(String name) {
		CtTypeReference<T> main = null;
		if (name.matches(".*(<.+>)")) {
			Pattern pattern = Pattern.compile("([^<]+)<(.+)>");
			Matcher m = pattern.matcher(name);
			if (name.startsWith("?")) {
				main = (CtTypeReference) this.jdtTreeBuilder.getFactory().Core().createWildcardReference();
			} else {
				main = this.jdtTreeBuilder.getFactory().Core().createTypeReference();
			}
			if (m.find()) {
				main.setSimpleName(m.group(1));
				final String[] split = m.group(2).split(",");
				for (String parameter : split) {
					((CtTypeReference) main).addActualTypeArgument(getTypeParameterReference(parameter.trim()));
				}
			}
		} else if (Character.isUpperCase(name.charAt(0))) {
			main = this.jdtTreeBuilder.getFactory().Core().createTypeReference();
			main.setSimpleName(name);
		} else if (name.startsWith("?")) {
			return (CtTypeReference) this.jdtTreeBuilder.getFactory().Core().createWildcardReference();
		}
		return main;
	}
	<T> CtTypeReference<T> getTypeReference(TypeReference ref) {
		CtTypeReference<T> res = null;
		CtTypeReference inner = null;
		final String[] namesParameterized = CharOperation.charArrayToStringArray(ref.getParameterizedTypeName());
		int index = namesParameterized.length - 1;
		for (; index >= 0; index--) {
			// Start at the end to get the class name first.
			CtTypeReference main = getTypeReference(namesParameterized[index]);
			if (main == null) {
				break;
			}
			if (res == null) {
				res = (CtTypeReference<T>) main;
			} else {
				inner.setDeclaringType((CtTypeReference<?>) main);
			}
			inner = main;
		}
		if (res == null) {
			return this.jdtTreeBuilder.getFactory().Type().createReference(CharOperation.toString(ref.getParameterizedTypeName()));
		}
		CtPackageReference packageReference = index >= 0
				? this.jdtTreeBuilder.getFactory().Package().getOrCreate(concatSubArray(namesParameterized, index)).getReference()
				: this.jdtTreeBuilder.getFactory().Package().topLevel();
		inner.setPackage(packageReference);
		return res;
	}
