    private EnclosingBall<S, P> pivotingBall(final Iterable<P> points) {

        final P first = points.iterator().next();
        final List<P> extreme = new ArrayList<P>(first.getSpace().getDimension() + 1);
        final List<P> support = new ArrayList<P>(first.getSpace().getDimension() + 1);

        // start with only first point selected as a candidate support
        extreme.add(first);
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
