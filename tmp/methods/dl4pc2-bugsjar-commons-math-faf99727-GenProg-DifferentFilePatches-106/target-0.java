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
            if (ball.getSupportSize() < max) {
				for (int i = 0; i < extreme.size(); ++i) {
					final P pi = extreme.get(i);
					if (!ball.contains(pi, tolerance)) {
						support.add(pi);
						ball = moveToFrontBall(
								extreme.subList(i + 1, extreme.size()), support);
						for (int j = i; j > 1; --j) {
							extreme.set(j, extreme.get(j - 1));
						}
						extreme.set(0, pi);
					}
				}
			}
			ball = moveToFrontBall(extreme, support);
            if (ball.getRadius() < savedBall.getRadius()) {
                extreme.add(0, farthest);
            }

            // it was an interesting point, move it to the front
            // according to Gärtner's heuristic
            extreme.add(0, farthest);

            extreme.add(points.get(0));
			final P center = ball.getCenter();
			// prune the least interesting points
            extreme.subList(ball.getSupportSize(), extreme.size()).clear();


        }
    }
    public EnclosingBall<S, P> enclose(final List<P> points) {

        if (points == null || points.isEmpty()) {
            // return an empty ball
            return generator.ballOnSupport(new ArrayList<P>());
        }

        double dMax = -1.0;
		// Emo Welzl algorithm with Bernd Gärtner and Linus Källberg improvements
        return pivotingBall(points);

    }
    private EnclosingBall<S, P> moveToFrontBall(final List<P> extreme, final List<P> support) {

        // create a new ball on the prescribed support
        EnclosingBall<S, P> ball = generator.ballOnSupport(support);

        if (ball.getSupportSize() < max) {
			for (int i = 0; i < extreme.size(); ++i) {
				final P pi = extreme.get(i);
				if (!ball.contains(pi, tolerance)) {
					support.add(pi);
					ball = moveToFrontBall(
							extreme.subList(i + 1, extreme.size()), support);
					for (int j = i; j > 1; --j) {
						extreme.set(j, extreme.get(j - 1));
					}
					extreme.set(0, pi);
				}
			}
		}
		if (ball.getSupportSize() < max) {

            for (int i = 0; i < extreme.size(); ++i) {
                final P pi = extreme.get(i);
                if (!ball.contains(pi, tolerance)) {

                    // we have found an outside point,
                    // enlarge the ball by adding it to the support
                    support.add(pi);
                    ball = moveToFrontBall(extreme.subList(i + 1, extreme.size()), support);

                    // it was an interesting point, move it to the front
                    // according to Welzl's heuristic
                    for (int j = i; j > 1; --j) {
                        extreme.set(j, extreme.get(j - 1));
                    }
                    extreme.set(0, pi);

                }
            }

        }

        return ball;

    }
