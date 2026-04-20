        public Edge(final Vertex start, final Vertex end, final Line line) {

            this.start = start;
            this.end   = end;
            this.line  = line;
            this.node  = null;

            // connect the vertices back to the edge
            start.setOutgoing(this);
            end.setIncoming(this);

        }
        public void bindWith(final Line line) {
            lines.add(line);
        }
        public void setIncoming(final Edge incoming) {
            this.incoming = incoming;
            bindWith(incoming.getLine());
        }
        public Vertex getStart() {
            return start;
        }
    private static void insertEdges(final double hyperplaneThickness,
                                    final BSPTree<Euclidean2D> node,
                                    final List<Edge> edges) {

        // find an edge with an hyperplane that can be inserted in the node
        int index = 0;
        Edge inserted =null;
        while (inserted == null && index < edges.size()) {
            inserted = edges.get(index++);
            if (inserted.getNode() == null) {
                if (node.insertCut(inserted.getLine())) {
                    inserted.setNode(node);
                } else {
                    inserted = null;
                }
            } else {
                inserted = null;
            }
        }

        if (inserted == null) {
            // no suitable edge was found, the node remains a leaf node
            // we need to set its inside/outside boolean indicator
            final BSPTree<Euclidean2D> parent = node.getParent();
            if (parent == null || node == parent.getMinus()) {
                node.setAttribute(Boolean.TRUE);
            } else {
                node.setAttribute(Boolean.FALSE);
            }
            return;
        }

        // we have split the node by inserted an edge as a cut sub-hyperplane
        // distribute the remaining edges in the two sub-trees
        final List<Edge> plusList  = new ArrayList<Edge>();
        final List<Edge> minusList = new ArrayList<Edge>();
        for (final Edge edge : edges) {
            if (edge != inserted) {
                final double startOffset = inserted.getLine().getOffset(edge.getStart().getLocation());
                final double endOffset   = inserted.getLine().getOffset(edge.getEnd().getLocation());
                Side startSide = (FastMath.abs(startOffset) <= hyperplaneThickness) ?
                                 Side.HYPER : ((startOffset < 0) ? Side.MINUS : Side.PLUS);
                Side endSide   = (FastMath.abs(endOffset) <= hyperplaneThickness) ?
                                 Side.HYPER : ((endOffset < 0) ? Side.MINUS : Side.PLUS);
                switch (startSide) {
                    case PLUS:
                        if (endSide == Side.MINUS) {
                            // we need to insert a split point on the hyperplane
                            final Vertex splitPoint = edge.split(inserted.getLine());
                            minusList.add(splitPoint.getOutgoing());
                            plusList.add(splitPoint.getIncoming());
                        } else {
                            plusList.add(edge);
                        }
                        break;
                    case MINUS:
                        if (endSide == Side.PLUS) {
                            // we need to insert a split point on the hyperplane
                            final Vertex splitPoint = edge.split(inserted.getLine());
                            minusList.add(splitPoint.getIncoming());
                            plusList.add(splitPoint.getOutgoing());
                        } else {
                            minusList.add(edge);
                        }
                        break;
                    default:
                        if (endSide == Side.PLUS) {
                            plusList.add(edge);
                        } else if (endSide == Side.MINUS) {
                            minusList.add(edge);
                        }
                        break;
                }
            }
        }

        // recurse through lower levels
        if (!plusList.isEmpty()) {
            insertEdges(hyperplaneThickness, node.getPlus(),  plusList);
        } else {
            node.getPlus().setAttribute(Boolean.FALSE);
        }
        if (!minusList.isEmpty()) {
            insertEdges(hyperplaneThickness, node.getMinus(), minusList);
        } else {
            node.getMinus().setAttribute(Boolean.TRUE);
        }

    }
        public Vertex split(final Line splitLine) {
            final Vertex splitVertex = new Vertex(line.intersection(splitLine));
            splitVertex.bindWith(splitLine);
            final Edge startHalf = new Edge(start, splitVertex, line);
            final Edge endHalf   = new Edge(splitVertex, end, line);
            startHalf.node = node;
            endHalf.node   = node;
            return splitVertex;
        }
        public Vertex(final Vector2D location) {
            this.location = location;
            this.incoming = null;
            this.outgoing = null;
            this.lines    = new ArrayList<Line>();
        }
        public void setOutgoing(final Edge outgoing) {
            this.outgoing = outgoing;
            bindWith(outgoing.getLine());
        }
        public BSPTree<Euclidean2D> getNode() {
            return node;
        }
        public Edge getOutgoing() {
            return outgoing;
        }
        public Edge getIncoming() {
            return incoming;
        }
        public void setNode(final BSPTree<Euclidean2D> node) {
            this.node = node;
        }
        public Line getLine() {
            return line;
        }
        public Line sharedLineWith(final Vertex vertex) {
            for (final Line line1 : lines) {
                for (final Line line2 : vertex.lines) {
                    if (line1 == line2) {
                        return line1;
                    }
                }
            }
            return null;
        }
        public Vertex getEnd() {
            return end;
        }
    private static BSPTree<Euclidean2D> verticesToTree(final double hyperplaneThickness,
                                                       final Vector2D ... vertices) {

        final int n = vertices.length;
        if (n == 0) {
            // the tree represents the whole space
            return new BSPTree<Euclidean2D>(Boolean.TRUE);
        }

        // build the vertices
        final Vertex[] vArray = new Vertex[n];
        for (int i = 0; i < n; ++i) {
            vArray[i] = new Vertex(vertices[i]);
        }

        // build the edges
        List<Edge> edges = new ArrayList<Edge>();
        for (int i = 0; i < n; ++i) {

            // get the endpoints of the edge
            final Vertex start = vArray[i];
            final Vertex end   = vArray[(i + 1) % n];

            // get the line supporting the edge, taking care not to recreate it
            // if it was already created earlier due to another edge being aligned
            // with the current one
            Line line = start.sharedLineWith(end);
            if (line == null) {
                line = new Line(start.getLocation(), end.getLocation());
            }

            // create the edge and store it
            edges.add(new Edge(start, end, line));

            // check if another vertex also happens to be on this line
            for (final Vertex vertex : vArray) {
                if (vertex != start && vertex != end &&
                    FastMath.abs(line.getOffset(vertex.getLocation())) <= hyperplaneThickness) {
                    vertex.bindWith(line);
                }
            }

        }

        // build the tree top-down
        final BSPTree<Euclidean2D> tree = new BSPTree<Euclidean2D>();
        insertEdges(hyperplaneThickness, tree, edges);

        return tree;

    }
