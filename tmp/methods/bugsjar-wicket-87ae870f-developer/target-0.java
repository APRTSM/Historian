		public C convertToObject(String value, Locale locale)
		{
			if (value == null)
			{
				return null;
			}
			Class<C> theType = type.get();
			if ("".equals(value))
			{
				if (String.class.equals(theType))
				{
					return theType.cast("");
				}
				return null;
			}

			try
			{
				C converted = Objects.convertValue(value, theType);
				if (converted != null)
				{
					return converted;
				}

				if (theType.isInstance(value))
				{
					return theType.cast(value);
				}
			}
			catch (Exception e)
			{
				throw new ConversionException(e.getMessage(), e).setSourceValue(value);
			}

			throw new ConversionException("Could not convert value: " + value + " to type: " +
				theType.getName() + ". Could not find compatible converter.").setSourceValue(value);
		}
