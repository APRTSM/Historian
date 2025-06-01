	private PersistResult persistFirstPass(DataProvider dataProvider, MetadataMode metadataMode, DataMode dataMode,
			List<EntityType> topologicalSortedEntityTypes)
	{
		ImmutableMap.Builder<String, Long> persistResultBuilder = ImmutableMap.builder();
		topologicalSortedEntityTypes.forEach(entityType ->
		{
			EntityType persistedEntityType = persistEntityTypeFirstPass(entityType, metadataMode);
			if (dataProvider.hasEntities(entityType))
			{
				if (entityType.isAbstract())
				{
					throw new MolgenisDataException(
							format("Abstract entity type '%s' with id '%s' cannot contain entities",
									entityType.getLabel(), entityType.getId()));
				}
				Stream<Entity> entities = dataProvider.getEntities(entityType);
				long nrPersistedEntities = persistEntitiesFirstPass(persistedEntityType, entities, dataMode);
				persistResultBuilder.put(entityType.getId(), nrPersistedEntities);
			}
		});
		return PersistResult.create(persistResultBuilder.build());
	}
