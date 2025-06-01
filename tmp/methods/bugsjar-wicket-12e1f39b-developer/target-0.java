	private boolean isLengthAndPrefixCorrect(String creditCardNumber)
	{
		if (creditCardNumber != null)
		{
			// strip spaces and dashes
			creditCardNumber = creditCardNumber.replaceAll("[ -]", "");
		}

		// the length of the credit card number has to be between 12 and 19.
		// else the number is invalid.
		if (creditCardNumber != null && creditCardNumber.length() >= 12 &&
			creditCardNumber.length() <= 19)
		{
			if (isAmericanExpress(creditCardNumber))
			{
				return true;
			}
			else if (isChinaUnionPay(creditCardNumber))
			{
				return true;
			}
			else if (isDinersClubCarteBlanche(creditCardNumber))
			{
				return true;
			}
			else if (isDinersClubInternational(creditCardNumber))
			{
				return true;
			}
			else if (isDinersClubUsAndCanada(creditCardNumber))
			{
				return true;
			}
			else if (isDiscoverCard(creditCardNumber))
			{
				return true;
			}
			else if (isJCB(creditCardNumber))
			{
				return true;
			}
			else if (isLaser(creditCardNumber))
			{
				return true;
			}
			else if (isMaestro(creditCardNumber))
			{
				return true;
			}
			else if (isMastercard(creditCardNumber))
			{
				return true;
			}
			else if (isSolo(creditCardNumber))
			{
				return true;
			}
			else if (isSwitch(creditCardNumber))
			{
				return true;
			}
			else if (isVisa(creditCardNumber))
			{
				return true;
			}
			else if (isVisaElectron(creditCardNumber))
			{
				return true;
			}
			else if (isUnknown(creditCardNumber))
			{
				return true;
			}
		}

		return false;
	}
	private boolean isLaser(String creditCardNumber)
	{
		cardId = CreditCardValidator.INVALID;
		boolean returnValue = false;

		if (creditCardNumber.length() >= 16 && creditCardNumber.length() <= 19 &&
			isChecksumCorrect(creditCardNumber))
		{
			if (creditCardNumber.startsWith("6304") || creditCardNumber.startsWith("6706") ||
				creditCardNumber.startsWith("6771") || creditCardNumber.startsWith("6709"))
			{
				cardId = CreditCardValidator.LASER;
				returnValue = true;
			}
		}

		return returnValue;
	}
	private boolean isDiscoverCard(String creditCardNumber)
	{
		cardId = CreditCardValidator.INVALID;
		boolean returnValue = false;

		if (creditCardNumber.length() == 16 && creditCardNumber.startsWith("6") &&
			isChecksumCorrect(creditCardNumber))
		{
			int firstThreeDigits = Integer.parseInt(creditCardNumber.substring(0, 3));
			int firstSixDigits = Integer.parseInt(creditCardNumber.substring(0, 6));
			if (creditCardNumber.startsWith("6011") || creditCardNumber.startsWith("65") ||
				(firstThreeDigits >= 644 && firstThreeDigits <= 649) ||
				(firstSixDigits >= 622126 && firstSixDigits <= 622925))
			{
				cardId = CreditCardValidator.DISCOVER_CARD;
				returnValue = true;
			}
		}

		return returnValue;
	}
	private boolean isVisa(String creditCardNumber)
	{
		cardId = CreditCardValidator.INVALID;
		boolean returnValue = false;

		if (creditCardNumber.length() == 13 || creditCardNumber.length() == 16)
		{
			if (creditCardNumber.startsWith("4"))
			{
				cardId = CreditCardValidator.SWITCH;
				returnValue = true;
			}
		}

		return returnValue;
	}
	private boolean isSolo(String creditCardNumber)
	{
		cardId = CreditCardValidator.INVALID;
		boolean returnValue = false;

		if ((creditCardNumber.length() == 16 || creditCardNumber.length() == 18 || creditCardNumber.length() == 19) &&
			isChecksumCorrect(creditCardNumber))
		{
			if (creditCardNumber.startsWith("6334") || creditCardNumber.startsWith("6767"))
			{
				cardId = CreditCardValidator.SOLO;
				returnValue = true;
			}
		}

		return returnValue;
	}
	private boolean isJCB(String creditCardNumber)
	{
		cardId = CreditCardValidator.INVALID;
		boolean returnValue = false;

		if (creditCardNumber.length() == 16 && isChecksumCorrect(creditCardNumber))
		{
			int firstFourDigits = Integer.parseInt(creditCardNumber.substring(0, 4));
			if (firstFourDigits >= 3528 && firstFourDigits <= 3589)
			{
				cardId = CreditCardValidator.JCB;
				returnValue = true;
			}
		}

		return returnValue;
	}
	private boolean isDinersClubCarteBlanche(String creditCardNumber)
	{
		cardId = CreditCardValidator.INVALID;
		boolean returnValue = false;

		if (creditCardNumber.length() == 14 && creditCardNumber.startsWith("30"))
		{
			int firstDigits = Integer.parseInt(creditCardNumber.substring(0, 3));
			if (firstDigits >= 300 && firstDigits <= 305 && isChecksumCorrect(creditCardNumber))
			{
				cardId = CreditCardValidator.DINERS_CLUB_CARTE_BLANCHE;
				returnValue = true;
			}
		}

		return returnValue;
	}
	private boolean isSwitch(String creditCardNumber)
	{
		cardId = CreditCardValidator.INVALID;
		boolean returnValue = false;

		if ((creditCardNumber.length() == 16 || creditCardNumber.length() == 18 || creditCardNumber.length() == 19) &&
			isChecksumCorrect(creditCardNumber))
		{
			if (creditCardNumber.startsWith("4903") || creditCardNumber.startsWith("4905") ||
				creditCardNumber.startsWith("4911") || creditCardNumber.startsWith("4936") ||
				creditCardNumber.startsWith("564182") || creditCardNumber.startsWith("633110") ||
				creditCardNumber.startsWith("6333") || creditCardNumber.startsWith("6759"))
			{
				cardId = CreditCardValidator.SWITCH;
				returnValue = true;
			}
		}

		return returnValue;
	}
	private boolean isDinersClubInternational(String creditCardNumber)
	{
		cardId = CreditCardValidator.INVALID;
		boolean returnValue = false;

		if (creditCardNumber.length() == 14 && creditCardNumber.startsWith("36") &&
			isChecksumCorrect(creditCardNumber))
		{
			cardId = CreditCardValidator.DINERS_CLUB_INTERNATIONAL;
			returnValue = true;
		}

		return returnValue;
	}
	private boolean isChecksumCorrect(String creditCardNumber)
	{
		String input = creditCardNumber;
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

		return (sum % 10) == 0;
	}
	private boolean isVisaElectron(String creditCardNumber)
	{
		cardId = CreditCardValidator.INVALID;
		boolean returnValue = false;

		if (creditCardNumber.length() == 16 &&
			(creditCardNumber.startsWith("417500") || creditCardNumber.startsWith("4917") ||
				creditCardNumber.startsWith("4913") || creditCardNumber.startsWith("4508") || creditCardNumber.startsWith("4844")))
		{
			cardId = CreditCardValidator.VISA_ELECTRON;
			returnValue = true;
		}

		return returnValue;
	}
	private boolean isMastercard(String creditCardNumber)
	{
		cardId = CreditCardValidator.INVALID;
		boolean returnValue = false;

		if (creditCardNumber.length() == 16 && isChecksumCorrect(creditCardNumber))
		{
			int firstTwoDigits = Integer.parseInt(creditCardNumber.substring(0, 2));
			if (firstTwoDigits >= 51 && firstTwoDigits <= 55)
			{
				cardId = CreditCardValidator.MASTERCARD;
				returnValue = true;
			}
		}

		return returnValue;
	}
	private String getCardName()
	{
		return (cardId > -1 && cardId < creditCardNames.length ? creditCardNames[cardId] : "");
	}
	private boolean isDinersClubUsAndCanada(String creditCardNumber)
	{
		cardId = CreditCardValidator.INVALID;
		boolean returnValue = false;

		if (creditCardNumber.length() == 16 &&
			(creditCardNumber.startsWith("54") || creditCardNumber.startsWith("55")) &&
			isChecksumCorrect(creditCardNumber))
		{
			cardId = CreditCardValidator.DINERS_CLUB_US_AND_CANADA;
			returnValue = true;
		}

		return returnValue;
	}
	private boolean isChinaUnionPay(String creditCardNumber)
	{
		cardId = CreditCardValidator.INVALID;
		boolean returnValue = false;

		if ((creditCardNumber.length() >= 16 && creditCardNumber.length() <= 19) &&
			(creditCardNumber.startsWith("622")))
		{
			int firstDigits = Integer.parseInt(creditCardNumber.substring(0, 5));
			if (firstDigits >= 622126 && firstDigits <= 622925)
			{
				cardId = CreditCardValidator.CHINA_UNIONPAY;
				returnValue = true;
			}
		}

		return returnValue;
	}
	private boolean isAmericanExpress(String creditCardNumber)
	{
		cardId = CreditCardValidator.INVALID;
		boolean returnValue = false;

		if (creditCardNumber.length() == 15 &&
			(creditCardNumber.startsWith("34") || creditCardNumber.startsWith("37")))
		{
			if (isChecksumCorrect(creditCardNumber))
			{
				cardId = CreditCardValidator.AMERICAN_EXPRESS;
				returnValue = true;
			}
		}

		return returnValue;
	}
	private boolean isMaestro(String creditCardNumber)
	{
		cardId = CreditCardValidator.INVALID;
		boolean returnValue = false;

		if (creditCardNumber.length() >= 12 && creditCardNumber.length() <= 19 &&
			isChecksumCorrect(creditCardNumber))
		{
			if (creditCardNumber.startsWith("5018") || creditCardNumber.startsWith("5020") ||
				creditCardNumber.startsWith("5038") || creditCardNumber.startsWith("6304") ||
				creditCardNumber.startsWith("6759") || creditCardNumber.startsWith("6761") ||
				creditCardNumber.startsWith("6763"))
			{
				cardId = CreditCardValidator.MAESTRO;
				returnValue = true;
			}
		}

		return returnValue;
	}
	protected boolean isUnknown(String creditCardNumber)
	{
		return false;
	}
	protected void onValidate(IValidatable<String> validatable)
	{
		creditCardNumber = validatable.getValue();
		if (!isLengthAndPrefixCorrect(creditCardNumber))
		{
			error(validatable);
		}
	}
