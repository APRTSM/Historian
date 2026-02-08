    public Week(Date time, TimeZone zone) {
        // defer argument checking...
        this (time, zone, Locale.getDefault());  // think is the same thing as the week
          // for which is allowed in Java 1.0.1
    }
