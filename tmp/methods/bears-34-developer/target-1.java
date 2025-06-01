	public <T> void visitCtConditional(CtConditional<T> conditional) {
		enterCtExpression(conditional);
		CtExpression<Boolean> condition = conditional.getCondition();
		boolean parent;
		try {
			parent = conditional.getParent() instanceof CtAssignment || conditional.getParent() instanceof CtVariable;
		} catch (ParentNotInitializedException ex) {
			// nothing if we have no parent
			parent = false;
		}
		if (parent) {
			printer.write("(");
		}
		scan(condition);
		if (parent) {
			printer.write(")");
		}
		printer.write(" ? ");
		CtExpression<T> thenExpression = conditional.getThenExpression();
		scan(thenExpression);
		printer.write(" : ");

		CtExpression<T> elseExpression = conditional.getElseExpression();
		boolean isAssign = false;
		if ((isAssign = elseExpression instanceof CtAssignment)) {
			printer.write("(");
		}
		scan(elseExpression);
		if (isAssign) {
			printer.write(")");
		}
		exitCtExpression(conditional);
	}
	public <T> void visitCtNewArray(CtNewArray<T> newArray) {
		enterCtExpression(newArray);
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
					scan(e);
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
				scan(e);
				printer.write(" , ");
				if (i + 1 == l_elements.size()) {
					printer.removeLastChar();
					// if the last element c
					List<CtComment> comments = elementPrinterHelper.getComments(e, CommentOffset.AFTER);
					// if the last element contains an inline comment, print a new line before closing the array
					if (!comments.isEmpty() && comments.get(comments.size() - 1).getCommentType() == CtComment.CommentType.INLINE) {
						printer.insertLine();
					}
				}
			}

			elementPrinterHelper.writeComment(newArray, CommentOffset.INSIDE);
			printer.write(" }");
		}
		elementPrinterHelper.writeComment(newArray, CommentOffset.AFTER);
		exitCtExpression(newArray);
	}
	protected void enterCtExpression(CtExpression<?> e) {
		if (!(e instanceof CtStatement)) {
			elementPrinterHelper.writeComment(e, CommentOffset.BEFORE);
		}
		printer.mapLine(e, sourceCompilationUnit);
		if (shouldSetBracket(e)) {
			context.parenthesedExpression.push(e);
			printer.write("(");
		}
		if (!e.getTypeCasts().isEmpty()) {
			for (CtTypeReference<?> r : e.getTypeCasts()) {
				printer.write("(");
				DefaultJavaPrettyPrinter.this.scan(r);
				printer.write(") ");
				printer.write("(");
				context.parenthesedExpression.push(e);
			}
		}
	}
	protected void exitCtExpression(CtExpression<?> e) {
		while ((context.parenthesedExpression.size() > 0) && e == context.parenthesedExpression.peek()) {
			context.parenthesedExpression.pop();
			printer.write(")");
		}
		if (!(e instanceof CtStatement)) {
			elementPrinterHelper.writeComment(e, CommentOffset.AFTER);
		}
	}
	public <T> void visitCtBinaryOperator(CtBinaryOperator<T> operator) {
		if (child instanceof CtExpression) {
			if (operator.getLeftHandOperand() == null) {
				operator.setLeftHandOperand((CtExpression<?>) child);
				return;
			} else if (operator.getRightHandOperand() == null) {
				operator.setRightHandOperand((CtExpression<?>) child);
				return;
			} else if (jdtTreeBuilder.getContextBuilder().stack.peek().node instanceof StringLiteralConcatenation) {
				CtBinaryOperator<?> op = operator.getFactory().Core().createBinaryOperator();
				op.setKind(BinaryOperatorKind.PLUS);
				op.setLeftHandOperand(operator.getRightHandOperand());
				op.setRightHandOperand((CtExpression<?>) child);
				operator.setRightHandOperand(op);
				int[] lineSeparatorPositions = this.jdtTreeBuilder.getContextBuilder().compilationunitdeclaration.compilationResult.lineSeparatorPositions;
				SourcePosition leftPosition = op.getLeftHandOperand().getPosition();
				SourcePosition rightPosition = op.getRightHandOperand().getPosition();
				op.setPosition(op.getFactory().createSourcePosition(leftPosition.getCompilationUnit(), leftPosition.getSourceStart(), rightPosition.getSourceEnd(), lineSeparatorPositions));
				return;
			}
		}
		super.visitCtBinaryOperator(operator);
	}
