	public boolean visit(SingleTypeReference singleTypeReference, BlockScope scope) {
		if (skipTypeInAnnotation) {
			return true;
		}
		if (context.stack.peekFirst().node instanceof UnionTypeReference) {
			if (singleTypeReference.resolvedType == null) {
				CtTypeReference typeReference = factory.Type().createReference(singleTypeReference.toString());
				CtReference ref = references.getDeclaringReferenceFromImports(singleTypeReference.getLastToken());
				references.setPackageOrDeclaringType(typeReference, ref);
				context.enter(typeReference, singleTypeReference);
			} else {
				context.enter(references.<Throwable>getTypeReference(singleTypeReference.resolvedType), singleTypeReference);
			}

			return true;
		} else if (context.stack.peekFirst().element instanceof CtCatch) {
			context.enter(helper.createCatchVariable(singleTypeReference), singleTypeReference);
			return true;
		}
		context.enter(factory.Code().createTypeAccessWithoutCloningReference(references.buildTypeReference(singleTypeReference, scope)), singleTypeReference);
		return true;
	}
