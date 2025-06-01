    public ExcelPosition(Double longitude, Double latitude, Double elevation, Double speed, CompactCalendar time, String description) {
        throw new UnsupportedOperationException();
    }
    private void setCellAsTime(ColumnType type, CompactCalendar value) {
        Cell cell = getOrCreateCell(type);
        if (cell != null) {
            if (value != null)
                cell.setCellValue(value.getCalendar());
            else
                cell.setCellValue(0);
        }
    }
