	public void removeStatement(CtStatement statement) {
		if (this.statements != CtElementImpl.<CtStatement>emptyList()) {
			this.statements.remove(statement);
			if (isImplicit() && statements.size() == 0) {
				setImplicit(false);
			}
		}
	}
