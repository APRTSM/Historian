		private Object mapValueOneToMany(Array arrayValue, Attribute attr) throws SQLException
		{
			EntityType entityType = attr.getRefEntity();
			Object value;
			Object[] postgreSqlMrefIds = (Object[]) arrayValue.getArray();
			if (postgreSqlMrefIds.length > 0 && postgreSqlMrefIds[0] != null)
			{
				Attribute idAttr = entityType.getIdAttribute();
				Object[] mrefIds = new Object[postgreSqlMrefIds.length];
				for (int i = 0; i < postgreSqlMrefIds.length; ++i)
				{
					Object mrefIdRaw = postgreSqlMrefIds[i];
					Object mrefId = mrefIdRaw != null ? convertMrefIdValue(mrefIdRaw.toString(), idAttr) : null;
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
