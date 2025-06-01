	public <T> Page<T> queryAll(Class<T> entityClass, Pageable pageable) {
		return queryAll(entityClass, pageable, new SpannerQueryOptions()
				.setOffset(pageable.getOffset()).setLimit(pageable.getPageSize()));
	}
