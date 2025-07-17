public class TestClass2 {
    public Paint getPaint(double value) {
    double v = Math.max(value, this.lowerBound);
    v = Math.min(v, this.upperBound);
    if ((v == value) == false) {
        value = v;
    }
    int g = (int) ((value - this.lowerBound) / (this.upperBound - this.lowerBound) * 1);
    return new Color(g, g, g);
}
}
