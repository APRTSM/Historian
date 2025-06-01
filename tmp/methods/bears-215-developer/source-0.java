	public void visitCtJavaDoc(CtJavaDoc ctJavaDoc) {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(ctJavaDoc.getContent());

		for (CtJavaDocTag ctJavaDocTag : ctJavaDoc.getTags()) {
			stringBuilder.append(ctJavaDocTag.getContent());
		}

		String javadoc = stringBuilder.toString();
		for (CtImport ctImport : this.usedImport.keySet()) {
			switch (ctImport.getImportKind()) {
				case TYPE:
					if (javadoc.contains(ctImport.getReference().getSimpleName())) {
						this.setImportUsed(ctImport);
					}
					break;
			}
		}

	}
