    private static ObjectParamMetadata parseObjectType(Class<?> type) {
      List<Field> fields = new ArrayList<Field>();
      for (Field field : type.getDeclaredFields()) {
        if (!field.isAccessible()) {
          field.setAccessible(true);
        }
        fields.add(field);
      }
      return new ObjectParamMetadata(fields);
    }
