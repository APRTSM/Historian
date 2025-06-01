	public void removeStatement(CtStatement statement) {
		if (this.statements != CtElementImpl.<CtStatement>emptyList()) {

			boolean hasBeenRemoved = false;
			// we cannot use a remove(statement) as it uses the equals
			// and a block can have twice exactly the same statement.
			for (int i = 0; i < this.statements.size(); i++) {
				if (this.statements.get(i) == statement) {
					this.statements.remove(i);
					hasBeenRemoved = true;
					break;
				}
			}

			// in case we use it with a statement manually built
			if (!hasBeenRemoved) {
				this.statements.remove(statement);
			}

			if (isImplicit() && statements.size() == 0) {
				setImplicit(false);
			}
		}
	}
