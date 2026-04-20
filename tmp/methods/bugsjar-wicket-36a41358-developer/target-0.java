		public Object getValue(Object object)
		{
			if (Array.getLength(object) > index)
			{
				return Array.get(object, index);
			}
			return null;
		}
		public Object getValue(Object object)
		{
			if (((List<?>)object).size() <= index)
				return null;
			return ((List<?>)object).get(index);
		}
	private static ObjectAndGetSetter getObjectAndGetSetter(final String expression,
		final Object object, int tryToCreateNull)
	{
		final String expressionBracketsSeperated = Strings.replaceAll(expression, "[", ".[")
			.toString();
		int index = getNextDotIndex(expressionBracketsSeperated, 0);
		int lastIndex = 0;
		Object value = object;
		Class<?> clz = value.getClass();
		String exp = expressionBracketsSeperated;
		while (index != -1)
		{
			exp = expressionBracketsSeperated.substring(lastIndex, index);
			if (exp.length() == 0)
			{
				exp = expressionBracketsSeperated.substring(index + 1);
				break;
			}

			IGetAndSet getAndSetter = null;
			try
			{
				getAndSetter = getGetAndSetter(exp, clz);
			}
			catch (WicketRuntimeException ex)
			{

				// expression by it self can't be found. try to find a
				// setPropertyByIndex(int,value) method
				index = getNextDotIndex(expressionBracketsSeperated, index + 1);
				if (index != -1)
				{
					String indexExpression = expressionBracketsSeperated.substring(lastIndex, index);
					getAndSetter = getGetAndSetter(indexExpression, clz);
				}
				else
				{
					exp = expressionBracketsSeperated.substring(lastIndex);
					break;
				}
			}
			Object newValue = null;
			if (value != null)
			{
				newValue = getAndSetter.getValue(value);
			}
			if (newValue == null)
			{
				if (tryToCreateNull == CREATE_NEW_VALUE)
				{
					newValue = getAndSetter.newValue(value);
					if (newValue == null)
					{
						return null;
					}
				}
				else if (tryToCreateNull == RESOLVE_CLASS)
				{
					clz = getAndSetter.getTargetClass();
				}
				else
				{
					return null;
				}
			}
			value = newValue;
			if (value != null)
			{
				// value can be null if we are in the RESOLVE_CLASS
				clz = value.getClass();
			}

			lastIndex = index + 1;
			index = getNextDotIndex(expressionBracketsSeperated, lastIndex);
			if (index == -1)
			{
				exp = expressionBracketsSeperated.substring(lastIndex);
				break;
			}
		}
		IGetAndSet getAndSetter = getGetAndSetter(exp, clz);
		return new ObjectAndGetSetter(getAndSetter, value);
	}
