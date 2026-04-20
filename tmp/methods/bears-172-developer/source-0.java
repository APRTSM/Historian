	public E findOne(Query<E> q)
	{
		E entity = delegate().findOne(q);

		if (entity != null && !isOperationPermitted(entity, Action.READ))
		{
			return null;
		}

		return entity;
	}
