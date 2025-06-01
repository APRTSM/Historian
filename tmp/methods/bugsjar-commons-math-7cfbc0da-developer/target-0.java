    private ArcsSet createSplitPart(final List<Double> limits) {
        if (limits.isEmpty()) {
            return null;
        } else {

            // collapse close limit angles
            for (int i = 0; i < limits.size(); ++i) {
                final int    j  = (i + 1) % limits.size();
                final double lA = limits.get(i);
                final double lB = MathUtils.normalizeAngle(limits.get(j), lA);
                if (FastMath.abs(lB - lA) <= getTolerance()) {
                    // the two limits are too close to each other, we remove both of them
                    if (j > 0) {
                        // regular case, the two entries are consecutive ones
                        limits.remove(j);
                        limits.remove(i);
                        i = i - 1;
                    } else {
                        // special case, i the the last entry and j is the first entry
                        // we have wrapped around list end
                        final double lEnd   = limits.remove(limits.size() - 1);
                        final double lStart = limits.remove(0);
                        if (limits.isEmpty()) {
                            // the ends were the only limits, is it a full circle or an empty circle?
                            if (lEnd - lStart > FastMath.PI) {
                                // it was full circle
                                return new ArcsSet(new BSPTree<Sphere1D>(Boolean.TRUE), getTolerance());
                            } else {
                                // it was an empty circle
                                return null;
                            }
                        } else {
                            // we have removed the first interval start, so our list
                            // currently starts with an interval end, which is wrong
                            // we need to move this interval end to the end of the list
                            limits.add(limits.remove(0) + MathUtils.TWO_PI);
                        }
                    }
                }
            }

            // build the tree by adding all angular sectors
            BSPTree<Sphere1D> tree = new BSPTree<Sphere1D>(Boolean.FALSE);
            for (int i = 0; i < limits.size() - 1; i += 2) {
                addArcLimit(tree, limits.get(i),     true);
                addArcLimit(tree, limits.get(i + 1), false);
            }

            if (tree.getCut() == null) {
                // we did not insert anything
                return null;
            }

            return new ArcsSet(tree, getTolerance());

        }
    }
    public Side side(final Arc arc) {

        final double reference = FastMath.PI + arc.getInf();
        final double arcLength = arc.getSup() - arc.getInf();

        boolean inMinus = false;
        boolean inPlus  = false;
        for (final double[] a : this) {
            final double syncedStart = MathUtils.normalizeAngle(a[0], reference) - arc.getInf();
            final double arcOffset   = a[0] - syncedStart;
            final double syncedEnd   = a[1] - arcOffset;
            if (syncedStart <= arcLength - getTolerance() || syncedEnd >= MathUtils.TWO_PI + getTolerance()) {
                inMinus = true;
            }
            if (syncedEnd >= arcLength + getTolerance()) {
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

        final List<Double> minus = new ArrayList<Double>();
        final List<Double>  plus = new ArrayList<Double>();

        final double reference = FastMath.PI + arc.getInf();
        final double arcLength = arc.getSup() - arc.getInf();

        for (final double[] a : this) {
            final double syncedStart = MathUtils.normalizeAngle(a[0], reference) - arc.getInf();
            final double arcOffset   = a[0] - syncedStart;
            final double syncedEnd   = a[1] - arcOffset;
            if (syncedStart < arcLength) {
                // the start point a[0] is in the minus part of the arc
                minus.add(a[0]);
                if (syncedEnd > arcLength) {
                    // the end point a[1] is past the end of the arc
                    // so we leave the minus part and enter the plus part
                    final double minusToPlus = arcLength + arcOffset;
                    minus.add(minusToPlus);
                    plus.add(minusToPlus);
                    if (syncedEnd > MathUtils.TWO_PI) {
                        // in fact the end point a[1] goes far enough that we
                        // leave the plus part of the arc and enter the minus part again
                        final double plusToMinus = MathUtils.TWO_PI + arcOffset;
                        plus.add(plusToMinus);
                        minus.add(plusToMinus);
                        minus.add(a[1]);
                    } else {
                        // the end point a[1] is in the plus part of the arc
                        plus.add(a[1]);
                    }
                } else {
                    // the end point a[1] is in the minus part of the arc
                    minus.add(a[1]);
                }
            } else {
                // the start point a[0] is in the plus part of the arc
                plus.add(a[0]);
                if (syncedEnd > MathUtils.TWO_PI) {
                    // the end point a[1] wraps around to the start of the arc
                    // so we leave the plus part and enter the minus part
                    final double plusToMinus = MathUtils.TWO_PI + arcOffset;
                    plus.add(plusToMinus);
                    minus.add(plusToMinus);
                    if (syncedEnd > MathUtils.TWO_PI + arcLength) {
                        // in fact the end point a[1] goes far enough that we
                        // leave the minus part of the arc and enter the plus part again
                        final double minusToPlus = MathUtils.TWO_PI + arcLength + arcOffset;
                        minus.add(minusToPlus);
                        plus.add(minusToPlus);
                        plus.add(a[1]);
                    } else {
                        // the end point a[1] is in the minus part of the arc
                        minus.add(a[1]);
                    }
                } else {
                    // the end point a[1] is in the plus part of the arc
                    plus.add(a[1]);
                }
            }
        }

        return new Split(createSplitPart(plus), createSplitPart(minus));

    }
    private void addArcLimit(final BSPTree<Sphere1D> tree, final double alpha, final boolean isStart) {

        final LimitAngle limit = new LimitAngle(new S1Point(alpha), !isStart, getTolerance());
        final BSPTree<Sphere1D> node = tree.getCell(limit.getLocation(), getTolerance());
        if (node.getCut() != null) {
            // this should never happen
            throw new MathInternalError();
        }

        node.insertCut(limit);
        node.setAttribute(null);
        node.getPlus().setAttribute(Boolean.FALSE);
        node.getMinus().setAttribute(Boolean.TRUE);

    }
