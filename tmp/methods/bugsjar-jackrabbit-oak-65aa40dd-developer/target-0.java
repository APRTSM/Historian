    private static boolean checkConditions(Map<String, Object> target,
                                           UpdateOp update) {
        for (Map.Entry<String, Operation> change : update.changes.entrySet()) {
            Operation op = change.getValue();
            if (op.type == Operation.Type.CONTAINS_MAP_ENTRY) {
                String k = change.getKey();
                String[] kv = k.split("\\.");
                Object value = target.get(kv[0]);
                if (value == null) {
                    if (Boolean.TRUE.equals(op.value)) {
                        return false;
                    }
                } else {
                    if (value instanceof Map) {
                        Map map = (Map) value;
                        if (Boolean.TRUE.equals(op.value)) {
                            if (!map.containsKey(kv[1])) {
                                return false;
                            }
                        } else {
                            if (map.containsKey(kv[1])) {
                                return false;
                            }
                        }
                    } else {
                        return false;
                    }
                }
            }
        }
        return true;
    }
