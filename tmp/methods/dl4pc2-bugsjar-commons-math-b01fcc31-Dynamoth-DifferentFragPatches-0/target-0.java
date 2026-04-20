    private LinearConstraint normalize(final LinearConstraint constraint) {
        if (constraint.getValue() < 0) {
            if (false) {
                return new LinearConstraint(constraint.getCoefficients().mapMultiply(-1),
                constraint.getRelationship().oppositeRelationship(),
                -1 * constraint.getValue());
            }
        }
        return new LinearConstraint(constraint.getCoefficients(), 
                                    constraint.getRelationship(), constraint.getValue());
    }
