        public boolean shareNodeWith(final Vertex vertex) {
            for (final BSPTree<Euclidean2D> node1 : nodes) {
                for (final BSPTree<Euclidean2D> node2 : vertex.nodes) {
                    if (node1 == node2) {
                        return true;
                    }
                }
            }
            return false;
        }
        public boolean outgoingNeedsProcessing() {
            return outgoingNeedsProcessing;
        }
    private static void insertVertices(final double hyperplaneThickness,
                                       final BSPTree<Euclidean2D> node,
                                       final List<Vertex> vertices) {

        Vertex current = vertices.get(vertices.size() - 1);
        int index = 0;
        Line inserted = null;
        while (inserted == null && index < vertices.size()) {
            final Vertex previous = current;
            current = vertices.get(index++);
            if (previous.outgoingNeedsProcessing() && current.incomingNeedsProcessing()) {

                if (previous.shareNodeWith(current)) {
                    // both vertices are already handled by an existing node,
                    // closer to the tree root, they were probably created
                    // when split points were introduced
                    inserted = null;
                } else {

                    inserted = new Line(previous.getLocation(), current.getLocation());

                    if (node.insertCut(inserted)) {
                        previous.addNode(node);
                        previous.outgoingProcessed();
                        current.addNode(node);
                        current.incomingProcessed();
                    } else {
                        inserted = null;
                    }

                }

            }
        }

        if (node.getCut() == null) {
            final BSPTree<Euclidean2D> parent = node.getParent();
            if (parent == null || node == parent.getMinus()) {
                node.setAttribute(Boolean.TRUE);
            } else {
                node.setAttribute(Boolean.FALSE);
            }
            return;
        }

        // distribute the remaining vertices in the two sub-trees
        Side currentSide = Side.HYPER;
        final List<Vertex> plusList  = new ArrayList<Vertex>();
        plusList.add(current);
        int plusCount = 0;
        final List<Vertex> minusList = new ArrayList<Vertex>();
        minusList.add(current);
        int minusCount = 0;
        while (index < vertices.size()) {
            final Vertex previous = current;
            final Side previousSide = currentSide;
            current = vertices.get(index++);
            final double currentOffset = inserted.getOffset(current.getLocation());
            currentSide = (FastMath.abs(currentOffset) <= hyperplaneThickness) ?
                           Side.HYPER :
                           ((currentOffset < 0) ? Side.MINUS : Side.PLUS);
            switch (currentSide) {
            case PLUS:
                if (previousSide == Side.MINUS) {
                    // we need to insert a split point on the hyperplane
                    final Line line = new Line(previous.getLocation(), current.getLocation());
                    final Vertex splitPoint = new Vertex(inserted.intersection(line));
                    splitPoint.addNode(node);
                    minusList.add(splitPoint);
                    plusList.add(splitPoint);
                }
                plusList.add(current);
                if (current.incomingNeedsProcessing() || current.outgoingNeedsProcessing()) {
                    ++plusCount;
                }
                break;
            case MINUS:
                if (previousSide == Side.PLUS) {
                    // we need to insert a split point on the hyperplane
                    final Line line = new Line(previous.getLocation(), current.getLocation());
                    final Vertex splitPoint = new Vertex(inserted.intersection(line));
                    splitPoint.addNode(node);
                    minusList.add(splitPoint);
                    plusList.add(splitPoint);
                }
                minusList.add(current);
                if (current.incomingNeedsProcessing() || current.outgoingNeedsProcessing()) {
                    ++minusCount;
                }
                break;
            default:
                current.addNode(node);
                plusList.add(current);
                minusList.add(current);
                break;
            }
        }

        // recurse through lower levels
        if (plusCount > 0) {
            insertVertices(hyperplaneThickness, node.getPlus(),  plusList);
        } else {
            node.getPlus().setAttribute(Boolean.FALSE);
        }
        if (minusCount > 0) {
            insertVertices(hyperplaneThickness, node.getMinus(), minusList);
        } else {
            node.getMinus().setAttribute(Boolean.TRUE);
        }

    }
        public Vertex(final Vector2D location) {
            this.location                = location;
            this.nodes                   = new ArrayList<BSPTree<Euclidean2D>>();
            this.incomingNeedsProcessing = true;
            this.outgoingNeedsProcessing = true;
        }
    private static BSPTree<Euclidean2D> verticesToTree(final double hyperplaneThickness,
                                                       final Vector2D ... vertices) {

        if (vertices.length == 0) {
            // the tree represents the whole space
            return new BSPTree<Euclidean2D>(Boolean.TRUE);
        }

        // at start, none of the edges have been processed
        final BSPTree<Euclidean2D> tree = new BSPTree<Euclidean2D>();
        List<Vertex> list = new ArrayList<PolygonsSet.Vertex>(vertices.length);
        for (final Vector2D vertex : vertices) {
            list.add(new Vertex(vertex));
        }

        // build the tree top-down
        insertVertices(hyperplaneThickness, tree, list);

        return tree;

    }
        public void outgoingProcessed() {
            outgoingNeedsProcessing = false;
        }
        public boolean incomingNeedsProcessing() {
            return incomingNeedsProcessing;
        }
        public void incomingProcessed() {
            incomingNeedsProcessing = false;
        }
        public void addNode(final BSPTree<Euclidean2D> node) {
            nodes.add(node);
        }
