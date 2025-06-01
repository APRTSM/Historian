	public CharSequence decorateOnSuccessScript(CharSequence script)
	{
		CharSequence s = (delegate == null) ? script : delegate.decorateOnSuccessScript(script);
		return preDecorateOnSuccessScript(s);
	}
	public CharSequence decorateScript(CharSequence script)
	{
		CharSequence s = (delegate == null) ? script : delegate.decorateScript(script);
		return preDecorateScript(s);
	}
	public CharSequence decorateOnFailureScript(CharSequence script)
	{
		CharSequence s = (delegate == null) ? script : delegate.decorateOnFailureScript(script);
		return preDecorateOnFailureScript(s);
	}
