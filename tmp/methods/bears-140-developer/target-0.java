    public static String formatMonth(Integer month, Integer year){
        // Null cases
        if (month == null || year == null)
            return null;

        // Invalid value cases
        if(month < 0 || month > 11 || year < 1)
            return null;

        // Format Month
        if(month.equals(0)){
            return "January " + "(" + year + ")";
        } else if(month.equals(1)){
            return "February " + "(" + year + ")";
        } else if(month.equals(2)){
            return "March " + "(" + year + ")";
        } else if(month.equals(3)){
            return "April " + "(" + year + ")";
        } else if(month.equals(4)){
            return "May " + "(" + year + ")";
        } else if(month.equals(5)){
            return "June " + "(" + year + ")";
        } else if(month.equals(6)){
            return "July " + "(" + year + ")";
        } else if(month.equals(7)){
            return "August " + "(" + year + ")";
        } else if(month.equals(8)){
            return "September " + "(" + year + ")";
        } else if(month.equals(9)){
            return "October " + "(" + year + ")";
        } else if(month.equals(10)){
            return "November " + "(" + year + ")";
        } else if (month.equals(11)){
            return "December " + "(" + year + ")";
        } else {
            return null;
        }

    }
    public static String formatQuarter(Integer month, Integer year){

        // Null cases
        if(month == null || year == null)
             return null;

        // Invalid value cases
        if(month < 0 || month > 11 || year < 1)
            return null;

        // Format month
        if (month == 0 || month == 1 || month == 2 ) {
            return "Q1 (" + year + ")";
        } else if (month == 3 || month == 4 || month == 5 ) {
            return "Q2 (" + year + ")";
        } else if (month == 6 || month == 7 || month == 8 )  {
            return "Q3 (" + year + ")";
        } else {
            return "Q4 (" + year + ")";
        }
    }
    public static String formatWeek(Integer week, Integer year){

        // Null cases
        if(week == null || year == null)
                return null;

        // Invalid value cases
        if(week < 1 || week > 52 || year < 1)
            return null;

        // Format Week for special cases (ie. 11, 12, 13 are postfixed with 'th')
        if(week == 11 || week == 12 || week == 13)
            return String.valueOf(week) + "th (" + year + ")";

        // Format Week
        char digit;
        if(week.toString().length() == 2){
            digit = week.toString().charAt(1);
        } else {
            digit = week.toString().charAt(0);
        }
        if(digit == '1'){
            return String.valueOf(week) + "st (" + year + ")";
        } else if (digit == '2') {
            return String.valueOf(week) + "nd (" + year + ")";
        } else if (digit == '3') {
            return String.valueOf(week) + "rd (" + year + ")";
        } else {
            return String.valueOf(week) + "th (" + year + ")";
        }
    }
