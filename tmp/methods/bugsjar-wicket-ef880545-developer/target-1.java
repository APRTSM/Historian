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
		public final void setValue(final Object object, final Object value,
			PropertyResolverConverter converter)
		{
			Class type = null;
			if (setMethod != null)
			{
				// getMethod is always there and if the value will be set through a setMethod then
				// the getMethod return type will be its type. Else we have to look at the
				// parameters if the setter but getting the return type is quicker
				type = getMethod.getReturnType();
			}
			else if (field != null)
			{
				type = field.getType();
			}

			Object converted = null;
			if (type != null)
			{
				converted = converter.convert(value, type);
				if (converted == null)
				{
					if (value != null)
					{
						throw new ConversionException("Can't convert value: " + value +
							" to class: " + getMethod.getReturnType() + " for setting it on " +
							object);
					}
					else if (getMethod.getReturnType().isPrimitive())
					{
						throw new ConversionException(
							"Can't convert null value to a primitive class: " +
								getMethod.getReturnType() + " for setting it on " + object);
					}
				}
			}

			if (setMethod != null)
			{
				try
				{
					setMethod.invoke(object, new Object[] { converted });
				}
				catch (InvocationTargetException ex)
				{
					throw new WicketRuntimeException("Error calling method: " + setMethod +
						" on object: " + object, ex.getCause());
				}
				catch (Exception ex)
				{
					throw new WicketRuntimeException("Error calling method: " + setMethod +
						" on object: " + object, ex);
				}
			}
			else if (field != null)
			{
				try
				{
					field.set(object, converted);
				}
				catch (Exception ex)
				{
					throw new WicketRuntimeException("Error setting field: " + field +
						" on object: " + object, ex);
				}
			}
			else
			{
				throw new WicketRuntimeException("no set method defined for value: " + value +
					" on object: " + object + " while respective getMethod being " +
					getMethod.getName());
			}
		}
