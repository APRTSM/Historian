		public Object convertToObject(String value, Locale locale)
		{
			if (value == null)
			{
				return null;
			}
			Class<?> theType = type.get();
			if ("".equals(value))
			{
				if (theType.equals(String.class))
				{
					return theType.cast("");
				}
				return null;
			}

			try
			{
				Object converted = Objects.convertValue(value, theType);
				if (theType.isAssignableFrom(converted.getClass()))
				{
					return theType.cast(converted);
				}
				else
				{
					throw new ConversionException("Could not convert value: " + value +
						" to type: " + theType.getName() + ". Could not find compatible converter.").setSourceValue(value);
				}
			}
			catch (Exception e)
			{
				throw new ConversionException(e.getMessage(), e).setSourceValue(value);
			}
		}
