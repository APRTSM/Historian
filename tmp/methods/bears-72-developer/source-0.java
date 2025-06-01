	public DefaultJavaPrettyPrinter scan(CtElement e) {
		if (e != null) {
			context.elementStack.push(e);
			if (env.isPreserveLineNumbers()) {
				if (!(e instanceof CtNamedElement)) {
					printer.adjustStartPosition(e);
				}
			}
			e.accept(this);
			context.elementStack.pop();
		}
		return this;
	}
