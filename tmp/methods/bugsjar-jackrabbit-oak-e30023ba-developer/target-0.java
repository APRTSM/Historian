    void closeWriter() throws IOException {
        if (writer != null) {
            writer.close();

            //OAK-2029 Record the last updated status so
            //as to make IndexTracker detect changes when index
            //is stored in file system
            NodeBuilder status = definition.child(":status");
            status.setProperty("lastUpdated", ISO8601.format(Calendar.getInstance()), Type.DATE);
            status.setProperty("indexedNodes",indexedNodes);
        }
    }
