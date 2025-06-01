    private Object convertToTimestamp(Column column, String value) {
        final boolean matches = EPOCH_EQUIVALENT_TIMESTAMP.matcher(value).matches() || "0".equals(value) || EPOCH_TIMESTAMP.equals(value);
        if (matches) {
            if (column.isOptional()) {
                return null;
            }

            return Timestamp.from(Instant.EPOCH);
        }
        return Timestamp.valueOf(value).toInstant().atZone(ZoneId.systemDefault());
    }
    private Object convertToLocalDate(Column column, String value) {
        final boolean zero = EPOCH_EQUIVALENT_DATE.matcher(value).matches() || "0".equals(value);
        if (zero && column.isOptional()) {
            return null;
        }
        if (zero) {
            value = EPOCH_DATE;
        }
        return LocalDate.from(DateTimeFormatter.ISO_LOCAL_DATE.parse(value));
    }
    private Object convertToLocalDateTime(Column column, String value) {
        final boolean matches = EPOCH_EQUIVALENT_TIMESTAMP.matcher(value).matches() || "0".equals(value);
        if (matches) {
            if (column.isOptional()) {
                return null;
            }

            value = EPOCH_TIMESTAMP;
        }

        return LocalDateTime.from(timestampFormat(column.length()).parse(value));
    }
