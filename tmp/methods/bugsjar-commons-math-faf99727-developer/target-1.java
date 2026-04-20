    EnclosingBall<S, P> enclose(Iterable<P> points);
    public EnclosingBall<S, P> enclose(final Iterable<P> points) {

        if (points == null || !points.iterator().hasNext()) {
            // return an empty ball
            return generator.ballOnSupport(new ArrayList<P>());
        }

        // Emo Welzl algorithm with Bernd Gärtner and Linus Källberg improvements
        return pivotingBall(points);

    }
    private EnclosingBall<S, P> moveToFrontBall(final List<P> extreme, final int nbExtreme,
                                                final List<P> support) {

        // create a new ball on the prescribed support
        EnclosingBall<S, P> ball = generator.ballOnSupport(support);

        if (ball.getSupportSize() < max) {

            for (int i = 0; i < nbExtreme; ++i) {
                final P pi = extreme.get(i);
                if (!ball.contains(pi, tolerance)) {

                    // we have found an outside point,
                    // enlarge the ball by adding it to the support
                    support.add(pi);
                    ball = moveToFrontBall(extreme, i, support);
                    support.remove(support.size() - 1);

                    // it was an interesting point, move it to the front
                    // according to Welzl's heuristic
                    for (int j = i; j > 0; --j) {
                        extreme.set(j, extreme.get(j - 1));
                    }
                    extreme.set(0, pi);

                }
            }

        }

        return ball;

    }
    private EnclosingBall<S, P> pivotingBall(final Iterable<P> points) {

        List<P> extreme = new ArrayList<P>(max);
        List<P> support = new ArrayList<P>(max);

        // start with only first point selected as a candidate support
        extreme.add(points.iterator().next());
        EnclosingBall<S, P> ball = moveToFrontBall(extreme, extreme.size(), support);

        while (true) {

            // select the point farthest to current ball
            final P farthest = selectFarthest(points, ball);
            if (ball.contains(farthest, tolerance)) {
                // we have found a ball containing all points
                return ball;
            }

            // recurse search, restricted to the small subset containing support and farthest point
            support.clear();
            support.add(farthest);
            EnclosingBall<S, P> savedBall = ball;
            ball = moveToFrontBall(extreme, extreme.size(), support);
            if (ball.getRadius() < savedBall.getRadius()) {
                // TODO: fix this, it should never happen but it does!
                throw new MathInternalError();
            }

            // it was an interesting point, move it to the front
            // according to Gärtner's heuristic
            extreme.add(0, farthest);

            // prune the least interesting points
            extreme.subList(ball.getSupportSize(), extreme.size()).clear();


        }
    }
    public P selectFarthest(final Iterable<P> points, final EnclosingBall<S, P> ball) {

        final P center = ball.getCenter();
        P farthest   = null;
        double dMax  = -1.0;

        for (final P point : points) {
            final double d = point.distance(center);
            if (d > dMax) {
                farthest = point;
                dMax     = d;
            }
        }

        return farthest;

    }
