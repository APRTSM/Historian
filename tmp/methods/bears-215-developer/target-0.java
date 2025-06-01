	private boolean matchesTypeName(String javadoc, CtTypeReference<?> typeRef) {
		Matcher m = tagRE.matcher(javadoc);
		while (m.find()) {
			String bracket = m.group(1);
			String tag = m.group(2);
			if ("{".equals(bracket)) {
				if (inlineTags.contains(tag) == false) {
					continue;
				}
			} else {
				if (mainTags.contains(tag) == false) {
					continue;
				}
			}
			String type = m.group(3);
//			String methodName = m.group(4);
			String params = m.group(5);

			if (isTypeMatching(type, typeRef)) {
				return true;
			}
			if (params != null) {
				String[] paramTypes = params.split("\\s*,\\s*");
				for (String paramType : paramTypes) {
					if (isTypeMatching(paramType, typeRef)) {
						return true;
					}
				}
			}
		}
		return false;
	}
	private boolean isTypeMatching(String typeName, CtTypeReference<?> typeRef) {
		if (typeName != null) {
			if (typeName.equals(typeRef.getQualifiedName())) {
				return true;
			}
			if (typeName.equals(typeRef.getSimpleName())) {
				return true;
			}
		}
		return false;
	}
	public void visitCtJavaDoc(CtJavaDoc ctJavaDoc) {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(ctJavaDoc.getContent());

		for (CtJavaDocTag ctJavaDocTag : ctJavaDoc.getTags()) {
			stringBuilder.append("\n").append(ctJavaDocTag.getType()).append(" ").append(ctJavaDocTag.getContent());
		}

		String javadoc = stringBuilder.toString();
		for (CtImport ctImport : this.usedImport.keySet()) {
			switch (ctImport.getImportKind()) {
				case TYPE:
					if (javadoc.contains(ctImport.getReference().getSimpleName()) && ctImport.getReference() instanceof CtTypeReference) {
						//assure that it is not just any occurrence of same substring, but it is real javadoc link to the same type
						if (matchesTypeName(javadoc, (CtTypeReference<?>) ctImport.getReference())) {
							this.setImportUsed(ctImport);
						}
					}
					break;
			}
		}
	}
