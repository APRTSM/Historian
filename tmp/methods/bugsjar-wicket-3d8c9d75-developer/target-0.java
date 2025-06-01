	void initialize()
	{
		if (!getFlag(FLAG_INITIALIZED))
		{
			setFlag(FLAG_INITIALIZED, true);
			onInitialize();
		}
	}
