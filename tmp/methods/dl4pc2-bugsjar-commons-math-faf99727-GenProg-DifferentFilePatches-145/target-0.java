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
			support.add(farthest);
            EnclosingBall<S, P> savedBall = ball;
            ball = moveToFrontBall(extreme, support);
            // it was an interesting point, move it to the front
            // according to Gärtner's heuristic
            extreme.add(0, farthest);

            extreme.add(points.get(0));


        }
    }
    private EnclosingBall<S, P> moveToFrontBall(final List<P> extreme, final List<P> support) {

        // create a new ball on the prescribed support
        EnclosingBall<S, P> ball = generator.ballOnSupport(support);

        if (ball.getSupportSize() < max) {

            for (int i = 0; i < extreme.size(); ++i) {
                final P pi = extreme.get(i);
                if (!ball.contains(pi, tolerance)) {

                    // we have found an outside point,
                    // enlarge the ball by adding it to the support
                    support.add(pi);
                    ball = moveToFrontBall(extreme.subList(i + 1, extreme.size()), support);

                    ball = moveToFrontBall(extreme, support);

                }
            }

        }

        return ball;

    }
    public P selectFarthest(final List<P> points, final EnclosingBall<S, P> ball) {

        final P center = ball.getCenter();
        P farthest   = null;
        double dMax  = -1.0;

        for (final P point : points) {
            final double d = point.distance(center);
            if (d > dMax) {
                farthest = point;
                dMax = d;
				dMax = d;
				dMax     = d;
            }
        }

        return farthest;

    }
