    public static String toXml(Map map, XmlStringBuilder.Step identStep) {
        final XmlStringBuilder builder;
        final Map localMap;
        if (map != null && map.containsKey(ENCODING)) {
            builder = new XmlStringBuilderWithoutRoot(identStep, String.valueOf(map.get(ENCODING)));
            localMap = (Map) U.clone(map);
            localMap.remove(ENCODING);
        } else {
            builder = new XmlStringBuilderWithoutRoot(identStep, UTF_8.name());
            localMap = map;
        }
        if (localMap == null || localMap.size() != 1
            || (String.valueOf(((Map.Entry) localMap.entrySet().iterator().next()).getKey())).startsWith("-")
            || ((Map.Entry) localMap.entrySet().iterator().next()).getValue() instanceof List) {
            final String name;
            if (localMap != null && localMap.size() == 1
                && ((Map.Entry) localMap.entrySet().iterator().next()).getValue() instanceof List
                && !((List) ((Map.Entry) localMap.entrySet().iterator().next()).getValue()).isEmpty()) {
                name = String.valueOf(((Map.Entry) localMap.entrySet().iterator().next()).getKey());
            } else {
                name = "root";
            }
            XmlObject.writeXml(localMap, name, builder, false, U.<String>newLinkedHashSet());
        } else {
            XmlObject.writeXml(localMap, null, builder, false, U.<String>newLinkedHashSet());
        }
        return builder.toString();
    }
