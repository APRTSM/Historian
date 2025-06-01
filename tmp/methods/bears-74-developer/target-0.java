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
		if (inner.getPackage() == null) {
			PackageFactory packageFactory = this.jdtTreeBuilder.getFactory().Package();
			CtPackageReference packageReference = index >= 0 ? packageFactory.getOrCreate(concatSubArray(namesParameterized, index)).getReference() : packageFactory.topLevel();
			inner.setPackage(packageReference);
		}
		if (!res.toString().replace(", ?", ",?").endsWith(CharOperation.toString(ref.getParameterizedTypeName()))) {
			// verify that we did not match a class that have the same name in a different package
			return this.jdtTreeBuilder.getFactory().Type().createReference(CharOperation.toString(ref.getParameterizedTypeName()));
		}
		return res;
	}
