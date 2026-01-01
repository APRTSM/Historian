    public void writeEmbeddedObject(Object object) throws IOException {
        // 01-Sep-2016, tatu: As per [core#318], handle small number of cases
        if (object == null) {
writeNull();
return;
}
else {
if (object instanceof String) {
writeString((String) object);
return;
}
else {
if (object instanceof Number) {
Number n = (Number) object;
if (n instanceof Integer) {
writeNumber(n.intValue());
return;
}
else {
if (n instanceof Long) {
writeNumber(n.longValue());
return;
}
else {
if (n instanceof Double) {
writeNumber(n.doubleValue());
return;
}
else {
if (n instanceof Float) {
writeNumber(n.floatValue());
return;
}
else {
if (n instanceof Short) {
writeNumber(n.shortValue());
return;
}
else {
if (n instanceof Byte) {
writeNumber(n.byteValue());
return;
}
else {
if (n instanceof BigInteger) {
writeNumber((BigInteger) n);
return;
}
else {
if (n instanceof BigDecimal) {
writeNumber((BigDecimal) n);
return;
}
else {
if (n instanceof AtomicInteger) {
writeNumber(((AtomicInteger) n).get());
return;
}
else {
if (n instanceof AtomicLong) {
writeNumber(((AtomicLong) n).get());
return;
}

}

}

}

}

}

}

}

}

}

}
else {
if (object instanceof byte[]) {
writeBinary((byte[]) object);
return;
}
else {
if (object instanceof Boolean) {
writeBoolean((Boolean) object);
return;
}
else {
if (object instanceof AtomicBoolean) {
writeBoolean(((AtomicBoolean) object).get());
return;
}

}

}

}

}

}

throw new JsonGenerationException("No native support for writing embedded objects",
                this);
    }
