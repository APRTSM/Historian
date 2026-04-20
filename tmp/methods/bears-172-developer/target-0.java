	public E findOne(Query<E> q)
	{
		return findAllPermitted(q, Action.READ).findFirst().orElse(null);
	}
