	private Entity get()
	{
		if (getNext)
		{
			try
			{
				String[] values = csvReader.readNext();

				if (values != null && values.length == colNamesMap.size())
				{
					List<String> valueList = Arrays.asList(values);
					for (int i = 0; i < values.length; ++i)
					{
						// subsequent separators indicate
						// null
						// values instead of empty strings
						String value = values[i].isEmpty() ? null : values[i];
						values[i] = processCell(value, false);
					}

					next = new DynamicEntity(entityType);

					for (String name : colNamesMap.keySet())
					{
						next.set(name, valueList.get(colNamesMap.get(name)));
					}
				}
				else if (values != null && (values.length > 1 || (values.length == 1 && values[0].length() > 0))
						&& values.length < colNamesMap.size())
				{
					throw new MolgenisDataException(
							format("Number of values (%d) doesn't match the number of headers (%d): [%s]",
									values.length, colNamesMap.size(), stream(values).collect(joining(","))));
				}
				else
				{
					next = null;
				}

				getNext = false;
			}
			catch (IOException e)
			{
				throw new MolgenisDataException(format("Exception reading line of csv file [%s]", repositoryName), e);
			}
		}

		return next;
	}
