    public String getMessage() {
        return context.getMessage();
    }
    private String buildMessage(Locale locale,
                                String separator) {
        final StringBuilder sb = new StringBuilder();
        int count = 0;
        final int len = msgPatterns.size();
        for (int i = 0; i < len; i++) {
            final Localizable pat = msgPatterns.get(i);
            final Object[] args = msgArguments.get(i);
            final MessageFormat fmt = new MessageFormat(pat.getLocalizedString(locale),
                                                        locale);
            sb.append(fmt.format(args));
            if (++count < len) {
                // Add a separator if there are other messages.
                sb.append(separator);
            }
        }

        return sb.toString();
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
            throw new MathInternalError();
        }

        return loop;

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
                return +1;
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
