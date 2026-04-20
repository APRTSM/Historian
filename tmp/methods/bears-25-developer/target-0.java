        protected JsonSerializer<Object> _findAndAddDynamic(PropertySerializerMap map,
                Class<?> type, SerializerProvider provider) throws JsonMappingException
        {
            // 27-Jun-2017, tatu: [databind#1679] Need to avoid StackOverflowError...
            if (type == Object.class) {
                // basically just need to call `toString()`, easiest way:
                JsonSerializer<Object> ser = new Default(Default.TYPE_TO_STRING, type);
                _dynamicSerializers = map.newWith(type, ser);
                return ser;
            }
            PropertySerializerMap.SerializerAndMapResult result =
                    // null -> for now we won't keep ref or pass BeanProperty; could change
                    map.findAndAddKeySerializer(type, provider, null);
            // did we get a new map of serializers? If so, start using it
            if (map != result.map) {
                _dynamicSerializers = result.map;
            }
            return result.serializer;
        }
