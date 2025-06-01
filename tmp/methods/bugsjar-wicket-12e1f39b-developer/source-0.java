	protected void onValidate(IValidatable<String> validatable)
	{
		String input = (validatable.getValue());
		String numberToCheck = input.replaceAll("[ -]", "");
		int nulOffset = '0';
		int sum = 0;
		for (int i = 1; i <= numberToCheck.length(); i++)
		{
			int currentDigit = numberToCheck.charAt(numberToCheck.length() - i) - nulOffset;
			if ((i % 2) == 0)
			{
				currentDigit *= 2;
				currentDigit = currentDigit > 9 ? currentDigit - 9 : currentDigit;
				sum += currentDigit;
			}
			else
			{
				sum += currentDigit;
			}
		}
		if (!((sum % 10) == 0))
		{
			error(validatable);
		}
	}
