    static Revision newRevision(int clusterId) {
        long timestamp = getCurrentTimestamp();
        int c;
        synchronized (Revision.class) {
            // need to check again, because threads
            // could arrive inside the synchronized block
            // out of order
            if (timestamp < lastRevisionTimestamp) {
                timestamp = lastRevisionTimestamp;
            }
            if (timestamp == lastRevisionTimestamp) {
                c = ++lastRevisionCount;
            } else {
                lastRevisionTimestamp = timestamp;
                lastRevisionCount = c = 0;
            }
        }
        return new Revision(timestamp, c, clusterId);
    }
