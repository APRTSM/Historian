	void internalMarkRendering(boolean setRenderingFlag)
	{
		if (setRenderingFlag)
		{
			setFlag(FLAG_PREPARED_FOR_RENDER, false);
			setFlag(FLAG_RENDERING, true);
		}
		else
		{
			setFlag(FLAG_RENDERING, false);
		}
	}
