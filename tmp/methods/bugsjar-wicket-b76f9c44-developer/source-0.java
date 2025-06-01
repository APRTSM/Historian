	private CreditCard isVisa(String creditCardNumber)
	{
		if (creditCardNumber.length() == 13 || creditCardNumber.length() == 16)
		{
			if (creditCardNumber.startsWith("4"))
			{
				return CreditCard.SWITCH;
			}
		}

		return CreditCard.INVALID;
	}
