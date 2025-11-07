    public Line revert() {
final Line reverted = new Line(this);
        reverted.direction = this.direction.negate();
        return reverted;
    }
