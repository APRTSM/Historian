    private static ObjectParamMetadata parseObjectType(Class<?> type) {
      return new ObjectParamMetadata(
          Arrays.stream(type.getDeclaredFields())
              .filter(field -> !field.isSynthetic())
              .peek(field -> field.setAccessible(true))
              .collect(Collectors.toList()));
    }
