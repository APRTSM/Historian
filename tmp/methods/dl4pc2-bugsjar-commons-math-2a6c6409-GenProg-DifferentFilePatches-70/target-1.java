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
                int i = 0;
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
    public SplitSubHyperplane<Euclidean3D> split(Hyperplane<Euclidean3D> hyperplane) {

        final Plane otherPlane = (Plane) hyperplane;
        final Plane thisPlane  = (Plane) getHyperplane();
        final Line  inter      = otherPlane.intersection(thisPlane);
        final double tolerance = thisPlane.getTolerance();

        if (inter == null) {
            // the hyperplanes are parallel
            final double global = otherPlane.getOffset(thisPlane);
            return (global < -1.0e-10) ? new SplitSubHyperplane<Euclidean3D>(
					null, this) : new SplitSubHyperplane<Euclidean3D>(this,
					null);
        }

        // the hyperplanes do intersect
        Vector2D p = thisPlane.toSubSpace((Point<Euclidean3D>) inter.toSpace((Point<Euclidean1D>) Vector1D.ZERO));
        Vector2D q = thisPlane.toSubSpace((Point<Euclidean3D>) inter.toSpace((Point<Euclidean1D>) Vector1D.ONE));
        Vector3D crossP = Vector3D.crossProduct(inter.getDirection(), thisPlane.getNormal());
        if (crossP.dotProduct(otherPlane.getNormal()) < 0) {
            final Vector2D tmp = p;
            p           = q;
            q           = tmp;
        }
        final SubHyperplane<Euclidean2D> l2DMinus =
            new org.apache.commons.math3.geometry.euclidean.twod.Line(p, q, tolerance).wholeHyperplane();
        final SubHyperplane<Euclidean2D> l2DPlus =
            new org.apache.commons.math3.geometry.euclidean.twod.Line(q, p, tolerance).wholeHyperplane();

        final BSPTree<Euclidean2D> splitTree = getRemainingRegion().getTree(false).split(l2DMinus);
        final BSPTree<Euclidean2D> plusTree  = getRemainingRegion().isEmpty(splitTree.getPlus()) ?
                                               new BSPTree<Euclidean2D>(Boolean.FALSE) :
                                               new BSPTree<Euclidean2D>(l2DPlus, new BSPTree<Euclidean2D>(Boolean.FALSE),
                                                                        splitTree.getPlus(), null);

        final BSPTree<Euclidean2D> minusTree = getRemainingRegion().isEmpty(splitTree.getMinus()) ?
                                               new BSPTree<Euclidean2D>(Boolean.FALSE) :
                                                   new BSPTree<Euclidean2D>(l2DMinus, new BSPTree<Euclidean2D>(Boolean.FALSE),
                                                                            splitTree.getMinus(), null);

        return new SplitSubHyperplane<Euclidean3D>(new SubPlane(thisPlane.copySelf(), new PolygonsSet(plusTree, tolerance)),
                                                   new SubPlane(thisPlane.copySelf(), new PolygonsSet(minusTree, tolerance)));

    }
