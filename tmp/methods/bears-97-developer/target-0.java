	private PageMetadata asPageMetadata(Page<?> page) {

		Assert.notNull(page, "Page must not be null!");

		int number = pageableResolver.isOneIndexedParameters() ? page.getNumber() + 1 : page.getNumber();

		return new PageMetadata(page.getSize(), number, page.getTotalElements(), page.getTotalPages());
	}
