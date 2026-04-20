	public void decorateResponse(AbstractResource.ResourceResponse response, IStaticCacheableResource resource)
	{
		response.setCacheDurationToMaximum();
		response.setCacheScope(WebResponse.CacheScope.PUBLIC);
	}
