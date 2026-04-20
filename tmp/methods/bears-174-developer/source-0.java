		private Object mapValueOneToMany(Array arrayValue, Attribute attr) throws SQLException
		{
			EntityType entityType = attr.getRefEntity();
			Object value;
			String[] postgreSqlMrefIds = (String[]) arrayValue.getArray();
			if (postgreSqlMrefIds.length > 0 && postgreSqlMrefIds[0] != null)
			{
				Attribute idAttr = entityType.getIdAttribute();
				Object[] mrefIds = new Object[postgreSqlMrefIds.length];
				for (int i = 0; i < postgreSqlMrefIds.length; ++i)
				{
					String mrefIdStr = postgreSqlMrefIds[i];
					Object mrefId = mrefIdStr != null ? convertMrefIdValue(mrefIdStr, idAttr) : null;
					mrefIds[i] = mrefId;
				}

				// convert ids to (lazy) entities
				value = entityManager.getReferences(entityType, asList(mrefIds));
			}
			else
			{
				value = null;
			}
			return value;
		}
