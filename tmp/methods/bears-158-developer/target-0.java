    public ExcelPosition(Double longitude, Double latitude, Double elevation, Double speed, CompactCalendar time, String description) {
        Workbook workbook = new HSSFWorkbook();
        Sheet sheet = workbook.createSheet("new sheet");
        this.row = sheet.createRow(0);
        setLongitude(longitude);
        setLatitude(latitude);
        setElevation(elevation);
        setSpeed(speed);
        setTime(time);
        setDescription(description);
    }
    private void setCellAsTime(ColumnType type, CompactCalendar value) {
        Cell cell = getOrCreateCell(type);
        if (cell != null) {
            if (value != null)
                cell.setCellValue(value.getTime());
            else
                cell.setCellValue(0);
        }
    }
