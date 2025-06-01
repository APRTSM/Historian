	private static <T> PageMetadata asPageMetadata(Page<T> page) {

		Assert.notNull(page, "Page must not be null!");
		return new PageMetadata(page.getSize(), page.getNumber(), page.getTotalElements(), page.getTotalPages());
	}
