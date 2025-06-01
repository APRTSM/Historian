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
			Preconditions.checkArgument(groupingFields.length > 0, "Grouping fields can not be empty at this point");
			
			keyFields = new ArrayList<FlatFieldDescriptor>(type.getTotalFields());
			// for each key, find the field:
			for(int j = 0; j < groupingFields.length; j++) {
				int keyPos = groupingFields[j];

				int offset = 0;
				for(int i = 0; i < type.getArity(); i++) {

					TypeInformation fieldType = ((CompositeType<?>) type).getTypeAt(i);
					if(i < keyPos) {
						// not yet there, increment key offset
						offset += fieldType.getTotalFields();
					}
					else {
						// arrived at key position
						if(fieldType instanceof CompositeType) {
							// add all nested fields of composite type
							((CompositeType) fieldType).getFlatFields("*", offset, keyFields);
						}
						else if(fieldType instanceof AtomicType) {
							// add atomic type field
							keyFields.add(new FlatFieldDescriptor(offset, fieldType));
						}
						else {
							// type should either be composite or atomic
							throw new InvalidProgramException("Field type is neither CompositeType nor AtomicType: "+fieldType);
						}
						// go to next key
						break;
					}
				}
			}
			keyFields = removeNullElementsFromList(keyFields);
		}
