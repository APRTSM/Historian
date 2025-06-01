    private void decodeDataFields(Position position, int count, String[] values) {

        if (values.length > 1 && !values[1].isEmpty()) {
            position.set("tempData", values[1]);
        }

        if (values.length > 5 && !values[5].isEmpty()) {
            String[] data = values[5].split("\\|");
            boolean started = data[0].charAt(0) == '0';
            position.set("taximeterOn", started);
            position.set("taximeterStart", data[1]);
            if (data.length > 2) {
                position.set("taximeterEnd", data[2]);
                position.set("taximeterDistance", Integer.parseInt(data[3]));
                position.set("taximeterFare", Integer.parseInt(data[4]));
                position.set("taximeterTrip", data[5]);
                position.set("taximeterWait", data[6]);
            }
        }

    }
