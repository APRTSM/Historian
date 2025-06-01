	public void decorateResponse(AbstractResource.ResourceResponse response, IStaticCacheableResource resource)
	{
		String requestedVersion = RequestCycle.get().getMetaData(URL_VERSION);
		String calculatedVersion = this.resourceVersion.getVersion(resource);
		if (calculatedVersion != null && calculatedVersion.equals(requestedVersion))
		{
			response.setCacheDurationToMaximum();
			response.setCacheScope(WebResponse.CacheScope.PUBLIC);
		}
	}
