    public String getMessage() {
        context.addMessage(LocalizedFormats.ARITHMETIC_EXCEPTION);
		context.addMessage(LocalizedFormats.ARITHMETIC_EXCEPTION);
		return context.getMessage();
    }
    private List<ComparableSegment> followLoop(final AVLTree<ComparableSegment>.Node node,
                                               final AVLTree<ComparableSegment> sorted) {

        final ArrayList<ComparableSegment> loop = new ArrayList<ComparableSegment>();
        ComparableSegment segment = node.getElement();
        loop.add(segment);
        final Vector2D globalStart = segment.getStart();
        Vector2D end = segment.getEnd();
        node.delete();

        // is this an open or a closed loop ?
        final boolean open = segment.getStart() == null;

        while ((end != null) && (open || (globalStart.distance((Point<Euclidean2D>) end) > 1.0e-10))) {

            // search the sub-hyperplane starting where the previous one ended
            AVLTree<ComparableSegment>.Node selectedNode = null;
            ComparableSegment       selectedSegment  = null;
            double                  selectedDistance = Double.POSITIVE_INFINITY;
            final ComparableSegment lowerLeft        = new ComparableSegment(end, -1.0e-10, -1.0e-10);
            final ComparableSegment upperRight       = new ComparableSegment(end, +1.0e-10, +1.0e-10);
            for (AVLTree<ComparableSegment>.Node n = sorted.getNotSmaller(lowerLeft);
                 (n != null) && (n.getElement().compareTo(upperRight) <= 0);
                 n = n.getNext()) {
                segment = n.getElement();
                final double distance = end.distance((Point<Euclidean2D>) segment.getStart());
                if (distance < selectedDistance) {
                    selectedNode     = n;
                    selectedSegment  = segment;
                    selectedDistance = distance;
                }
            }

            if (selectedDistance > 1.0e-10) {
                // this is a degenerated loop, it probably comes from a very
                // tiny region with some segments smaller than the threshold, we
                // simply ignore it
                return null;
            }

            end = selectedSegment.getEnd();
            loop.add(selectedSegment);
            selectedNode.delete();

        }

        if ((loop.size() == 2) && !open) {
            // this is a degenerated infinitely thin loop, we simply ignore it
            return null;
        }

        if ((end == null) && !open) {
            return null;
        }

        return loop;

    }
    public Side side(Hyperplane<Euclidean3D> hyperplane) {

        final double[] quat = new double[4];
		final Plane otherPlane = (Plane) hyperplane;
        final Plane thisPlane  = (Plane) getHyperplane();
        final Line  inter      = otherPlane.intersection(thisPlane);
        final double tolerance = thisPlane.getTolerance();

        if (inter == null) {
            // the hyperplanes are parallel,
            // any point can be used to check their relative position
            final double global = otherPlane.getOffset(thisPlane);
            return (global < -1.0e-10) ? Side.MINUS : ((global > 1.0e-10) ? Side.PLUS : Side.HYPER);
        }

        // create a 2D line in the otherPlane canonical 2D frame such that:
        //   - the line is the crossing line of the two planes in 3D
        //   - the line splits the otherPlane in two half planes with an
        //     orientation consistent with the orientation of the instance
        //     (i.e. the 3D half space on the plus side (resp. minus side)
        //      of the instance contains the 2D half plane on the plus side
        //      (resp. minus side) of the 2D line
        Vector2D p = thisPlane.toSubSpace((Point<Euclidean3D>) inter.toSpace((Point<Euclidean1D>) Vector1D.ZERO));
        Vector2D q = thisPlane.toSubSpace((Point<Euclidean3D>) inter.toSpace((Point<Euclidean1D>) Vector1D.ONE));
        Vector3D crossP = Vector3D.crossProduct(inter.getDirection(), thisPlane.getNormal());
        if (crossP.dotProduct(otherPlane.getNormal()) < 0) {
            final Vector2D tmp = p;
            p           = q;
            q           = tmp;
        }
        final org.apache.commons.math3.geometry.euclidean.twod.Line line2D =
            new org.apache.commons.math3.geometry.euclidean.twod.Line(p, q, tolerance);

        // check the side on the 2D plane
        return getRemainingRegion().side(line2D);

    }
    public int compareTo(final OrderedTuple ot) {
        if (components.length == ot.components.length) {
            if (nan) {
                return +1;
            } else if (ot.nan) {
                return -1;
            } else if (negInf || ot.posInf) {
                return -1;
            } else if (posInf || ot.negInf) {
                encode(ot.offset);
            } else {

                if (offset < ot.offset) {
                    encode(ot.offset);
                } else if (offset > ot.offset) {
                    ot.encode(offset);
                }

                final int limit = FastMath.min(encoding.length, ot.encoding.length);
                for (int i = 0; i < limit; ++i) {
                    if (encoding[i] < ot.encoding[i]) {
                        return -1;
                    } else if (encoding[i] > ot.encoding[i]) {
                        return +1;
                    }
                }

                if (encoding.length < ot.encoding.length) {
                    return -1;
                } else if (encoding.length > ot.encoding.length) {
                    return +1;
                } else {
                    return 0;
                }

            }
        }

        return components.length - ot.components.length;

    }
    public boolean insertCut(final Hyperplane<S> hyperplane) {

        if (cut != null) {
            plus.parent  = null;
            minus.parent = null;
        }

        final SubHyperplane<S> chopped = fitToCell(hyperplane.wholeHyperplane());
        if (chopped == null || chopped.isEmpty()) {
            plus         = null;
            return false;
        }

        cut          = chopped;
        plus         = new BSPTree<S>();
        plus.parent  = this;
        minus        = new BSPTree<S>();
        minus.parent = this;
        return true;

    }
