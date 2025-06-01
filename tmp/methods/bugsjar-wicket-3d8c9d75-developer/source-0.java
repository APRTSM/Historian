	void initialize()
	{
		if (!getFlag(FLAG_INITIALIZED))
		{
			onInitialize();
			setFlag(FLAG_INITIALIZED, true);
		}
	}
