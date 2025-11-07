    public Line revert() {
        final Line reverted = new Line(zero, zero.subtract(direction));        reverted.direction = direction.negate();
        return reverted;
    }
