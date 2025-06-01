    public Segment readSegment(final String id) {
        ctx.writeAndFlush(newGetSegmentReq(this.clientID, id));
        return getSegment(id);
    }
    public Segment getSegment(final String id) {
        boolean interrupted = false;
        try {
            for (;;) {
                try {
                    Segment s = segment.poll(timeoutMs, TimeUnit.MILLISECONDS);
                    if (s == null) {
                        return null;
                    }
                    if (s.getSegmentId().toString().equals(id)) {
                        return s;
                    }
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
