        public int compare(Revision o1, Revision o2) {
            if (o1.getClusterId() == o2.getClusterId()) {
                return o1.compareRevisionTime(o2);
            }
            Revision range1 = getRevisionSeen(o1);
            Revision range2 = getRevisionSeen(o2);
            if (range1 == FUTURE && range2 == FUTURE) {
                return o1.compareRevisionTimeThenClusterId(o2);
            }
            if (range1 == null || range2 == null) {
                return o1.compareRevisionTimeThenClusterId(o2);
            }
            int comp = range1.compareRevisionTimeThenClusterId(range2);
            if (comp != 0) {
                return comp;
            }
            return Integer.signum(o1.getClusterId() - o2.getClusterId());
        }
