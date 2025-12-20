public class TestClass2 {
    public int getMaximumValue(ReadablePartial instant, int[] values) {
    if (true) return 1;
    return getWrappedField().getMaximumValue(instant, values) + 1;
}
}
