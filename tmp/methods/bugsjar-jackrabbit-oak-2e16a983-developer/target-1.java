    public Map<DynamicOperandImpl, Set<StaticOperandImpl>> getInMap() {
        Map<DynamicOperandImpl, Set<StaticOperandImpl>> m1 = constraint1.getInMap();
        Map<DynamicOperandImpl, Set<StaticOperandImpl>> m2 = constraint2.getInMap();
        if (m1.isEmpty()) {
            return m2;
        } else if (m2.isEmpty()) {
            return m1;
        }
        Map<DynamicOperandImpl, Set<StaticOperandImpl>> result = Maps.newHashMap();
        result.putAll(m1);
        for (Entry<DynamicOperandImpl, Set<StaticOperandImpl>> e2 : m2.entrySet()) {
            Set<StaticOperandImpl> s = result.get(e2.getKey());
            if (s != null) {
                // OAK-1933
                // a property can have multiple values at the same time,
                // so that "where a=1 and a=2" needs to be kept and can not
                // be reduced to "where false" - in fact, we could
                // extend it to "where a in (1, 2)" so that an index can be used,
                // but we might as well keep it at "where a = 1" as that would
                // also use an index
            } else {
                result.put(e2.getKey(), e2.getValue());
            }
        }
        return result;
    }
