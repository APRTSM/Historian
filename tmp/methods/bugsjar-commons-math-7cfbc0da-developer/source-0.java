    public Side side(final Arc arc) {

        final double reference = FastMath.PI + arc.getInf();
        final double arcLength = arc.getSup() - arc.getInf();

        boolean inMinus = false;
        boolean inPlus  = false;
        for (final double[] a : this) {
            final double syncedStart = MathUtils.normalizeAngle(a[0], reference) - arc.getInf();
            final double arcOffset   = a[0] - syncedStart;
            final double syncedEnd   = a[1] - arcOffset;
            if (syncedStart < arcLength || syncedEnd > MathUtils.TWO_PI) {
                inMinus = true;
            }
            if (syncedEnd > arcLength) {
                inPlus = true;
            }
        }

        if (inMinus) {
            if (inPlus) {
                return Side.BOTH;
            } else {
                return Side.MINUS;
            }
        } else {
            if (inPlus) {
                return Side.PLUS;
            } else {
                return Side.HYPER;
            }
        }

    }
    public Split split(final Arc arc) {

        final BSPTree<Sphere1D> minus = new BSPTree<Sphere1D>();
        minus.setAttribute(Boolean.FALSE);
        final BSPTree<Sphere1D> plus  = new BSPTree<Sphere1D>();
        plus.setAttribute(Boolean.FALSE);

        final double reference = FastMath.PI + arc.getInf();
        final double arcLength = arc.getSup() - arc.getInf();

        for (final double[] a : this) {
            final double syncedStart = MathUtils.normalizeAngle(a[0], reference) - arc.getInf();
            final double arcOffset   = a[0] - syncedStart;
            final double syncedEnd   = a[1] - arcOffset;
            if (syncedStart < arcLength) {
                // the start point a[0] is in the minus part of the arc
                addArcLimit(minus, a[0], true);
                if (syncedEnd > arcLength) {
                    // the end point a[1] is past the end of the arc
                    // so we leave the minus part and enter the plus part
                    final double minusToPlus = arcLength + arcOffset;
                    addArcLimit(minus, minusToPlus, false);
                    addArcLimit(plus, minusToPlus, true);
                    if (syncedEnd > MathUtils.TWO_PI) {
                        // in fact the end point a[1] goes far enough that we
                        // leave the plus part of the arc and enter the minus part again
                        final double plusToMinus = MathUtils.TWO_PI + arcOffset;
                        addArcLimit(plus, plusToMinus, false);
                        addArcLimit(minus, plusToMinus, true);
                        addArcLimit(minus, a[1], false);
                    } else {
                        // the end point a[1] is in the plus part of the arc
                        addArcLimit(plus, a[1], false);
                    }
                } else {
                    // the end point a[1] is in the minus part of the arc
                    addArcLimit(minus, a[1], false);
                }
            } else {
                // the start point a[0] is in the plus part of the arc
                addArcLimit(plus, a[0], true);
                if (syncedEnd > MathUtils.TWO_PI) {
                    // the end point a[1] wraps around to the start of the arc
                    // so we leave the plus part and enter the minus part
                    final double plusToMinus = MathUtils.TWO_PI + arcOffset;
                    addArcLimit(plus, plusToMinus, false);
                    addArcLimit(minus, plusToMinus, true);
                    if (syncedEnd > MathUtils.TWO_PI + arcLength) {
                        // in fact the end point a[1] goes far enough that we
                        // leave the minus part of the arc and enter the plus part again
                        final double minusToPlus = MathUtils.TWO_PI + arcLength + arcOffset;
                        addArcLimit(minus, minusToPlus, false);
                        addArcLimit(plus, minusToPlus, true);
                        addArcLimit(plus, a[1], false);
                    } else {
                        // the end point a[1] is in the minus part of the arc
                        addArcLimit(minus, a[1], false);
                    }
                } else {
                    // the end point a[1] is in the plus part of the arc
                    addArcLimit(plus, a[1], false);
                }
            }
        }

        return new Split(createSplitPart(plus), createSplitPart(minus));

    }
    private void addArcLimit(final BSPTree<Sphere1D> tree, final double alpha, final boolean isStart) {
        final LimitAngle limit = new LimitAngle(new S1Point(alpha), !isStart, getTolerance());
        final BSPTree<Sphere1D> node = tree.getCell(limit.getLocation(), getTolerance());
        if (node.getCut() != null) {
            // we find again an already added limit,
            // this means we have done a full turn around the circle
            leafBefore(node).setAttribute(Boolean.valueOf(!isStart));
        } else {
            // it's a new node
            node.insertCut(limit);
            node.setAttribute(null);
            node.getPlus().setAttribute(Boolean.FALSE);
            node.getMinus().setAttribute(Boolean.TRUE);
        }
    }
    private ArcsSet createSplitPart(final BSPTree<Sphere1D> tree) {
        if (tree.getCut() == null && !(Boolean) tree.getAttribute()) {
            return null;
        } else {
            return new ArcsSet(tree, getTolerance());
        }
    }
