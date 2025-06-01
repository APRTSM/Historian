    public String[] getColumnTypes(EntityType<?> entityType, String attributeName) {
        AbstractEntityPersister entityPersister = (AbstractEntityPersister) entityPersisters.get(entityType.getJavaType().getName());
        SessionFactoryImplementor sfi = entityPersister.getFactory();
        String[] columnNames = entityPersister.getPropertyColumnNames(attributeName);
        Database database = sfi.getServiceRegistry().locateServiceBinding(Database.class).getService();
        Table[] tables;

        if (entityPersister instanceof JoinedSubclassEntityPersister) {
            tables = new Table[((JoinedSubclassEntityPersister) entityPersister).getSubclassTableSpan()];
            for (int i = 0; i < tables.length; i++) {
                tables[i] = database.getTable(entityPersister.getSubclassTableName(i));
            }
        } else if (entityPersister instanceof UnionSubclassEntityPersister) {
            tables = new Table[((UnionSubclassEntityPersister) entityPersister).getSubclassTableSpan()];
            for (int i = 0; i < tables.length; i++) {
                tables[i] = database.getTable(entityPersister.getSubclassTableName(i));
            }
        } else if (entityPersister instanceof SingleTableEntityPersister) {
            tables = new Table[((SingleTableEntityPersister) entityPersister).getSubclassTableSpan()];
            for (int i = 0; i < tables.length; i++) {
                tables[i] = database.getTable(entityPersister.getSubclassTableName(i));
            }
        } else {
            tables = new Table[] { database.getTable(entityPersister.getTableName()) };
        }

        // In this case, the property might represent a formula
        boolean isFormula = columnNames.length == 1 && columnNames[0] == null;
        boolean isSubselect = tables.length == 1 && tables[0] == null;

        if (isFormula || isSubselect) {
            Type propertyType = entityPersister.getPropertyType(attributeName);
            long length;
            int precision;
            int scale;
            try {
                if (propertyType instanceof org.hibernate.type.EntityType) {
                    propertyType = ((org.hibernate.type.EntityType) propertyType).getIdentifierOrUniqueKeyType(sfi);
                }

                Method m = Type.class.getMethod("dictatedSizes", Mapping.class);
                Object size = ((Object[]) m.invoke(propertyType, sfi))[0];
                length =    (long) size.getClass().getMethod("getLength").invoke(size);
                precision = (int)  size.getClass().getMethod("getPrecision").invoke(size);
                scale =     (int)  size.getClass().getMethod("getScale").invoke(size);
            } catch (Exception ex) {
                throw new RuntimeException("Could not determine the column type of the attribute: " + attributeName + " of the entity: " + entityType.getName());
            }

            return new String[] {
                    sfi.getDialect().getTypeName(
                            propertyType.sqlTypes(sfi)[0],
                            length,
                            precision,
                            scale
                    )
            };
        }

        String[] columnTypes = new String[columnNames.length];
        for (int i = 0; i < columnNames.length; i++) {
            Column column = null;
            for (int j = 0; j < tables.length; j++) {
                column = tables[j].getColumn(new Column(columnNames[i]));
                if (column != null) {
                    break;
                }
            }

            if (column == null) {
                throw new IllegalArgumentException("Could not find column '" + columnNames[i] + "' in for entity: " + entityType.getName());
            }

            columnTypes[i] = column.getSqlType();
        }

        return columnTypes;
    }
