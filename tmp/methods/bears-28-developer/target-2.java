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
	public List<CtComment> getComments(CtElement element, CommentOffset offset) {
		List<CtComment> commentsToPrint = new ArrayList<>();
		if (!env.isCommentsEnabled() || element == null) {
			return commentsToPrint;
		}
		for (CtComment comment : element.getComments()) {
			if (comment.getCommentType() == CtComment.CommentType.FILE && offset == CommentOffset.TOP_FILE) {
				commentsToPrint.add(comment);
				continue;
			}
			if (comment.getCommentType() == CtComment.CommentType.FILE) {
				continue;
			}
			if (comment.getPosition() == null || element.getPosition() == null) {
				if (offset == CommentOffset.BEFORE) {
					commentsToPrint.add(comment);
				}
				continue;
			}
			final int line = element.getPosition().getLine();
			final int sourceEnd = element.getPosition().getSourceEnd();
			final int sourceStart = element.getPosition().getSourceStart();
			if (offset == CommentOffset.BEFORE && (comment.getPosition().getLine() < line || (sourceStart <= comment.getPosition().getSourceStart() && sourceEnd >= comment.getPosition().getSourceEnd()))) {
				commentsToPrint.add(comment);
			} else if (offset == CommentOffset.AFTER && comment.getPosition().getSourceStart() > sourceEnd) {
				commentsToPrint.add(comment);
			} else {
				final int endLine = element.getPosition().getEndLine();
				if (offset == CommentOffset.INSIDE && comment.getPosition().getLine() >= line && comment.getPosition().getEndLine() <= endLine) {
					commentsToPrint.add(comment);
				}
			}
		}
		return commentsToPrint;
	}
	private CtElement addCommentToNear(final CtComment comment, final Collection<CtElement> elements) {
		CtElement best = null;
		int smallDistance = Integer.MAX_VALUE;

		for (CtElement element : elements) {
			if (element.getPosition() == null) {
				continue;
			}
			if (element.isImplicit()) {
				continue;
			}
			if (element instanceof CtComment) {
				continue;
			}
			final boolean isAfter = element.getPosition().getSourceEnd() < comment.getPosition().getSourceStart();
			int distance = Math.abs(element.getPosition().getSourceStart() - comment.getPosition().getSourceEnd());
			if (isAfter) {
				distance = Math.abs(element.getPosition().getSourceEnd() - comment.getPosition().getSourceStart());
			}

			int elementEndLine = element.getPosition().getEndLine();
			int commentLine = comment.getPosition().getLine();

			if (distance < smallDistance && (!isAfter || elementEndLine == commentLine)) {
				best = element;
				smallDistance = distance;
			}
		}
		// adds the comment to the nearest element
		if (best != null) {
			best.addComment(comment);
		}
		return best;
	}
