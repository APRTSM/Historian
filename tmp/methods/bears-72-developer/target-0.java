	private static String getPath(CtElement ele) {
		StringBuilder sb = new StringBuilder();
		addParentPath(sb, ele);
		if (ele instanceof CtVariableAccess) {
			sb.append(':').append(((CtVariableAccess) ele).getVariable().getSimpleName());
		}
		return sb.toString();
	}
	public DefaultJavaPrettyPrinter scan(CtElement e) {
		if (e != null) {
			context.elementStack.push(e);
			if (env.isPreserveLineNumbers()) {
				if (!(e instanceof CtNamedElement)) {
					printer.adjustStartPosition(e);
				}
			}
			try {
				e.accept(this);
			} catch (SpoonException ex) {
				throw ex;
			} catch (Exception ex) {
				String elementInfo = e.getClass().getName();
				elementInfo += " on path " + getPath(e) + "\n";
				if (e.getPosition() != null) {
					elementInfo += "at position " + e.getPosition().toString() + " ";
				}
				throw new SpoonException("Printing of " + elementInfo + "failed", ex);
			}
			context.elementStack.pop();
		}
		return this;
	}
	private static void addParentPath(StringBuilder sb, CtElement ele) {
		if (ele == null || (ele instanceof CtPackage && ((CtPackage) ele).isUnnamedPackage())) {
			return;
		}
		if (ele.isParentInitialized()) {
			addParentPath(sb, ele.getParent());
		}
		sb.append("\n\t").append(ele.getClass().getSimpleName());
		if (ele instanceof CtNamedElement) {
			sb.append(":").append(((CtNamedElement) ele).getSimpleName());
		} else if (ele instanceof CtReference) {
			sb.append(":").append(((CtReference) ele).getSimpleName());
		}
	}
