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
                s.retainAll(e2.getValue());
            } else {
                result.put(e2.getKey(), e2.getValue());
            }
        }
        return result;
    }
