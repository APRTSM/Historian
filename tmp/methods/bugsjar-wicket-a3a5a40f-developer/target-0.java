	void internalMarkRendering(boolean setRenderingFlag)
	{
		// WICKET-5460 no longer prepared for render
		setFlag(FLAG_PREPARED_FOR_RENDER, false);

		setFlag(FLAG_RENDERING, setRenderingFlag);
	}
