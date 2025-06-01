	public <T> void visitCtNewArray(CtNewArray<T> newArray) {
		enterCtExpression(newArray);
		elementPrinterHelper.writeComment(newArray, CommentOffset.BEFORE);
		boolean isNotInAnnotation;
		try {
			isNotInAnnotation = (newArray.getParent(CtAnnotationType.class) == null) && (newArray.getParent(CtAnnotation.class) == null);
		} catch (ParentNotInitializedException e) {
			isNotInAnnotation = true;
		}

		if (isNotInAnnotation) {
			CtTypeReference<?> ref = newArray.getType();

			if (ref != null) {
				printer.write("new ");
			}

			try (Writable _context = context.modify().skipArray(true)) {
				scan(ref);
			}
			for (int i = 0; ref instanceof CtArrayTypeReference; i++) {
				printer.write("[");
				if (newArray.getDimensionExpressions().size() > i) {
					CtExpression<Integer> e = newArray.getDimensionExpressions().get(i);
					if (!(e instanceof CtStatement)) {
						elementPrinterHelper.writeComment(e, CommentOffset.BEFORE);
					}
					scan(e);
					if (!(e instanceof CtStatement)) {
						elementPrinterHelper.writeComment(e, CommentOffset.AFTER);
					}
				}
				printer.write("]");
				ref = ((CtArrayTypeReference) ref).getComponentType();
			}
		}
		if (newArray.getDimensionExpressions().size() == 0) {
			printer.write("{ ");
			List<CtExpression<?>> l_elements = newArray.getElements();
			for (int i = 0; i < l_elements.size(); i++) {
				CtExpression e = l_elements.get(i);
				if (!(e instanceof CtStatement)) {
					elementPrinterHelper.writeComment(e, CommentOffset.BEFORE);
				}
				scan(e);
				printer.write(" , ");
				if (i + 1 == l_elements.size()) {
					/*
					 * we have to remove last char before we writeComment.
					 * We cannot simply skip adding of " , ",
					 * because it influences formatting and EOL too
					 */
					printer.removeLastChar();
				}
				if (!(e instanceof CtStatement)) {
					elementPrinterHelper.writeComment(e, CommentOffset.AFTER);
				}
			}

			elementPrinterHelper.writeComment(newArray, CommentOffset.INSIDE);
			printer.write(" }");
		}
		elementPrinterHelper.writeComment(newArray, CommentOffset.AFTER);
		exitCtExpression(newArray);
	}
