    public Segment readSegment(final String id) {
        ctx.writeAndFlush(newGetSegmentReq(this.clientID, id));
        return getSegment();
    }
    public Segment getSegment() {
        boolean interrupted = false;
        try {
            for (;;) {
                try {
                    // log.debug("polling segment");
                    Segment s = segment.poll(timeoutMs, TimeUnit.MILLISECONDS);
                    // log.debug("returning segment " + s.getSegmentId());
                    return s;
                } catch (InterruptedException ignore) {
                    interrupted = true;
                }
            }
        } finally {
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }

    }
