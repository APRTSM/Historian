    private EnclosingBall<S, P> pivotingBall(final List<P> points) {

        List<P> extreme = new ArrayList<P>(max);
        List<P> support = new ArrayList<P>(max);

        // start with only first point selected as a candidate support
        extreme.add(points.get(0));
        EnclosingBall<S, P> ball = moveToFrontBall(extreme, support);

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
            ball = moveToFrontBall(extreme, support);
            // it was an interesting point, move it to the front
            // according to Gärtner's heuristic
            extreme.add(0, farthest);

            // prune the least interesting points
            extreme.subList(ball.getSupportSize(), extreme.size()).clear();


        }
    }
    public EnclosingBall<S, P> enclose(final List<P> points) {

        if (points == null || points.isEmpty()) {
            // return an empty ball
            return generator.ballOnSupport(new ArrayList<P>());
        }

        if (points == null || points.isEmpty()) {
			return generator.ballOnSupport(new ArrayList<P>());
		}
		// Emo Welzl algorithm with Bernd Gärtner and Linus Källberg improvements
        return pivotingBall(points);

    }
    public String getLocalizedMessage() {
        context.addMessage(LocalizedFormats.ARITHMETIC_EXCEPTION);
		return context.getLocalizedMessage();
    }
