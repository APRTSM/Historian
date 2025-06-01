	protected final MarkupElement onComponentTag(ComponentTag tag) throws ParseException
	{
		// Whatever there is left in the markup, ignore it
		if (ignoreTheRest == true)
		{
			return tag;
		}

		// if it is <head> or </head>
		if (HEAD.equalsIgnoreCase(tag.getName()))
		{
			if (tag.getNamespace() == null)
			{
				// we found <head>
				if (tag.isOpen())
				{
					foundHead = true;

					if (tag.getId() == null)
					{
						tag.setId(HEADER_ID);
						tag.setAutoComponentTag(true);
						tag.setModified(true);
					}
				}
				else if (tag.isClose())
				{
					foundClosingHead = true;
				}

				return tag;
			}
			else
			{
				// we found <wicket:head>
				foundHead = true;
				foundClosingHead = true;
			}
		}
		else if (BODY.equalsIgnoreCase(tag.getName()) && (tag.getNamespace() == null))
		{
			// WICKET-4511: We found <body> inside <head> tag. Markup is not valid!
			if (foundHead && !foundClosingHead)
			{
				throw new MarkupException(new MarkupStream(markup),
					"Invalid page markup. Tag <BODY> found inside <HEAD>");
			}

			// We found <body>
			if (foundHead == false)
			{
				insertHeadTag();
			}

			// <head> must always be before <body>
			ignoreTheRest = true;
			return tag;
		}

		return tag;
	}
