	public CharSequence decorateScript(CharSequence script)
	{
		CharSequence s = preDecorateScript(script);
		return (delegate == null) ? s : delegate.decorateScript(s);

	}
	public CharSequence decorateOnFailureScript(CharSequence script)
	{
		CharSequence s = preDecorateOnFailureScript(script);

		return (delegate == null) ? s : delegate.decorateOnFailureScript(s);
	}
	public CharSequence decorateOnSuccessScript(CharSequence script)
	{
		CharSequence s = preDecorateOnSuccessScript(script);
		return (delegate == null) ? s : delegate.decorateOnSuccessScript(s);
	}
