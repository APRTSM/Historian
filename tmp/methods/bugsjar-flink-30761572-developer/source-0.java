		private static int countNestedElementsBefore(CompositeType<?> compositeType, int pos) {
			if( pos == 0) {
				return 0;
			}
			int ret = 0;
			for (int i = 0; i < pos; i++) {
				TypeInformation<?> fieldType = compositeType.getTypeAt(i);
				ret += fieldType.getTotalFields() -1;
			}
			return ret;
		}
		public ExpressionKeys(int[] groupingFields, TypeInformation<T> type, boolean allowEmpty) {
			if (!type.isTupleType()) {
				throw new InvalidProgramException("Specifying keys via field positions is only valid " +
						"for tuple data types. Type: " + type);
			}

			if (!allowEmpty && (groupingFields == null || groupingFields.length == 0)) {
				throw new IllegalArgumentException("The grouping fields must not be empty.");
			}
			// select all fields. Therefore, set all fields on this tuple level and let the logic handle the rest
			// (makes type assignment easier).
			if (groupingFields == null || groupingFields.length == 0) {
				groupingFields = new int[type.getArity()];
				for (int i = 0; i < groupingFields.length; i++) {
					groupingFields[i] = i;
				}
			} else {
				groupingFields = rangeCheckFields(groupingFields, type.getArity() -1);
			}
			CompositeType<?> compositeType = (CompositeType<?>) type;
			Preconditions.checkArgument(groupingFields.length > 0, "Grouping fields can not be empty at this point");
			
			keyFields = new ArrayList<FlatFieldDescriptor>(type.getTotalFields());
			// for each key, find the field:
			for(int j = 0; j < groupingFields.length; j++) {
				for(int i = 0; i < type.getArity(); i++) {
					TypeInformation<?> fieldType = compositeType.getTypeAt(i);
					
					if(groupingFields[j] == i) { // check if user set the key
						int keyId = countNestedElementsBefore(compositeType, i) + i;
						if(fieldType instanceof TupleTypeInfoBase) {
							TupleTypeInfoBase<?> tupleFieldType = (TupleTypeInfoBase<?>) fieldType;
							tupleFieldType.addAllFields(keyId, keyFields);
						} else {
							Preconditions.checkArgument(fieldType instanceof AtomicType, "Wrong field type");
							keyFields.add(new FlatFieldDescriptor(keyId, fieldType));
						}
						
					}
				}
			}
			keyFields = removeNullElementsFromList(keyFields);
		}
