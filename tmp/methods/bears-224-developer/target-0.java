    public Value<O> apply(I input) {
        try {
            Object source = input;
            Object target = null;

            for (Operation p : operations) {
                target = p.apply(source);
                source = target;

                if (source == null) {
                    break;
                }
            }

            return target != null ? Value.of((O) target) : Value.empty();
        } catch (Exception ex) {
            return Value.empty();
        }
    }
