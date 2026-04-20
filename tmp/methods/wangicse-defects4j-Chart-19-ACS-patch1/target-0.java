    public int getWeight() {
        return this.weight;
    }
    public void setRangeAxes(ValueAxis[] axes) {
        for (int i = 0; i < axes.length; i++) {
            setRangeAxis(i, axes[i], false);
        }
        notifyListeners(new PlotChangeEvent(this));
    }
    public void setRowRenderingOrder(SortOrder order) {
        if (order == null) {
            throw new IllegalArgumentException("Null 'order' argument.");
        }
        this.rowRenderingOrder = order;
        notifyListeners(new PlotChangeEvent(this));
    }
    protected AxisSpace calculateRangeAxisSpace(Graphics2D g2,
                                                Rectangle2D plotArea,
                                                AxisSpace space) {

        if (space == null) {
            space = new AxisSpace();
        }

        // reserve some space for the range axis...
        if (this.fixedRangeAxisSpace != null) {
            if (this.orientation == PlotOrientation.HORIZONTAL) {
                space.ensureAtLeast(this.fixedRangeAxisSpace.getTop(),
                        RectangleEdge.TOP);
                space.ensureAtLeast(this.fixedRangeAxisSpace.getBottom(),
                        RectangleEdge.BOTTOM);
            }
            else if (this.orientation == PlotOrientation.VERTICAL) {
                space.ensureAtLeast(this.fixedRangeAxisSpace.getLeft(),
                        RectangleEdge.LEFT);
                space.ensureAtLeast(this.fixedRangeAxisSpace.getRight(),
                        RectangleEdge.RIGHT);
            }
        }
        else {
            // reserve space for the range axes (if any)...
            for (int i = 0; i < this.rangeAxes.size(); i++) {
                Axis yAxis = (Axis) this.rangeAxes.get(i);
                if (yAxis != null) {
                    RectangleEdge edge = getRangeAxisEdge(i);
                    space = yAxis.reserveSpace(g2, this, plotArea, edge, space);
                }
            }
        }
        return space;

    }
    protected AxisSpace calculateDomainAxisSpace(Graphics2D g2,
                                                 Rectangle2D plotArea,
                                                 AxisSpace space) {

        if (space == null) {
            space = new AxisSpace();
        }

        // reserve some space for the domain axis...
        if (this.fixedDomainAxisSpace != null) {
            if (this.orientation == PlotOrientation.HORIZONTAL) {
                space.ensureAtLeast(
                    this.fixedDomainAxisSpace.getLeft(), RectangleEdge.LEFT);
                space.ensureAtLeast(this.fixedDomainAxisSpace.getRight(),
                        RectangleEdge.RIGHT);
            }
            else if (this.orientation == PlotOrientation.VERTICAL) {
                space.ensureAtLeast(this.fixedDomainAxisSpace.getTop(),
                        RectangleEdge.TOP);
                space.ensureAtLeast(this.fixedDomainAxisSpace.getBottom(),
                        RectangleEdge.BOTTOM);
            }
        }
        else {
            // reserve space for the primary domain axis...
            RectangleEdge domainEdge = Plot.resolveDomainAxisLocation(
                    getDomainAxisLocation(), this.orientation);
            if (this.drawSharedDomainAxis) {
                space = getDomainAxis().reserveSpace(g2, this, plotArea,
                        domainEdge, space);
            }

            // reserve space for any domain axes...
            for (int i = 0; i < this.domainAxes.size(); i++) {
                Axis xAxis = (Axis) this.domainAxes.get(i);
                if (xAxis != null) {
                    RectangleEdge edge = getDomainAxisEdge(i);
                    space = xAxis.reserveSpace(g2, this, plotArea, edge, space);
                }
            }
        }

        return space;

    }
    public void setRangeAxisLocation(AxisLocation location) {
        // defer argument checking...
        setRangeAxisLocation(location, true);
    }
    public void setRangeCrosshairValue(double value, boolean notify) {
        this.rangeCrosshairValue = value;
        if (isRangeCrosshairVisible() && notify) {
            notifyListeners(new PlotChangeEvent(this));
        }
    }
    private void readObject(ObjectInputStream stream)
        throws IOException, ClassNotFoundException {

        stream.defaultReadObject();
        this.domainGridlineStroke = SerialUtilities.readStroke(stream);
        this.domainGridlinePaint = SerialUtilities.readPaint(stream);
        this.rangeGridlineStroke = SerialUtilities.readStroke(stream);
        this.rangeGridlinePaint = SerialUtilities.readPaint(stream);
        this.rangeCrosshairStroke = SerialUtilities.readStroke(stream);
        this.rangeCrosshairPaint = SerialUtilities.readPaint(stream);

        for (int i = 0; i < this.domainAxes.size(); i++) {
            CategoryAxis xAxis = (CategoryAxis) this.domainAxes.get(i);
            if (xAxis != null) {
                xAxis.setPlot(this);
                xAxis.addChangeListener(this);
            }
        }
        for (int i = 0; i < this.rangeAxes.size(); i++) {
            ValueAxis yAxis = (ValueAxis) this.rangeAxes.get(i);
            if (yAxis != null) {
                yAxis.setPlot(this);
                yAxis.addChangeListener(this);
            }
        }
        int datasetCount = this.datasets.size();
        for (int i = 0; i < datasetCount; i++) {
            Dataset dataset = (Dataset) this.datasets.get(i);
            if (dataset != null) {
                dataset.addChangeListener(this);
            }
        }
        int rendererCount = this.renderers.size();
        for (int i = 0; i < rendererCount; i++) {
            CategoryItemRenderer renderer
                = (CategoryItemRenderer) this.renderers.get(i);
            if (renderer != null) {
                renderer.addChangeListener(this);
            }
        }

    }
    public void setRenderer(CategoryItemRenderer renderer, boolean notify) {
        setRenderer(0, renderer, notify);
    }
    public void drawBackground(Graphics2D g2, Rectangle2D area) {
        fillBackground(g2, area, this.orientation);
        drawBackgroundImage(g2, area);
    }
    public void setDomainGridlineStroke(Stroke stroke) {
        if (stroke == null) {
            throw new IllegalArgumentException("Null 'stroke' not permitted.");
        }
        this.domainGridlineStroke = stroke;
        notifyListeners(new PlotChangeEvent(this));
    }
    public void setAnchorValue(double value) {
        setAnchorValue(value, true);
    }
    public void addRangeMarker(Marker marker, Layer layer) {
        addRangeMarker(0, marker, layer);
    }
    public void clearDomainMarkers() {
        if (this.backgroundDomainMarkers != null) {
            Set keys = this.backgroundDomainMarkers.keySet();
            Iterator iterator = keys.iterator();
            while (iterator.hasNext()) {
                Integer key = (Integer) iterator.next();
                clearDomainMarkers(key.intValue());
            }
            this.backgroundDomainMarkers.clear();
        }
        if (this.foregroundDomainMarkers != null) {
            Set keys = this.foregroundDomainMarkers.keySet();
            Iterator iterator = keys.iterator();
            while (iterator.hasNext()) {
                Integer key = (Integer) iterator.next();
                clearDomainMarkers(key.intValue());
            }
            this.foregroundDomainMarkers.clear();
        }
        notifyListeners(new PlotChangeEvent(this));
    }
    public void setDomainAxes(CategoryAxis[] axes) {
        for (int i = 0; i < axes.length; i++) {
            setDomainAxis(i, axes[i], false);
        }
        notifyListeners(new PlotChangeEvent(this));
    }
    public void addDomainMarker(CategoryMarker marker) {
        addDomainMarker(marker, Layer.FOREGROUND);
    }
    public SortOrder getColumnRenderingOrder() {
        return this.columnRenderingOrder;
    }
    public void setColumnRenderingOrder(SortOrder order) {
        if (order == null) {
            throw new IllegalArgumentException("Null 'order' argument.");
        }
        this.columnRenderingOrder = order;
        notifyListeners(new PlotChangeEvent(this));
    }
    protected AxisSpace calculateAxisSpace(Graphics2D g2,
                                           Rectangle2D plotArea) {
        AxisSpace space = new AxisSpace();
        space = calculateRangeAxisSpace(g2, plotArea, space);
        space = calculateDomainAxisSpace(g2, plotArea, space);
        return space;
    }
    public int getIndexOf(CategoryItemRenderer renderer) {
        return this.renderers.indexOf(renderer);
    }
    public void setRangeGridlinesVisible(boolean visible) {
        if (this.rangeGridlinesVisible != visible) {
            this.rangeGridlinesVisible = visible;
            notifyListeners(new PlotChangeEvent(this));
        }
    }
    private List datasetsMappedToRangeAxis(int index) {
        List result = new ArrayList();
        for (int i = 0; i < this.datasets.size(); i++) {
            Object dataset = this.datasets.get(i);
            if (dataset != null) {
                Integer m = (Integer) this.datasetToRangeAxisMap.get(i);
                if (m == null) {  // a dataset with no mapping is assigned to
                                  // axis 0
                    if (index == 0) {
                        result.add(dataset);
                    }
                }
                else {
                    if (m.intValue() == index) {
                        result.add(dataset);
                    }
                }
            }
        }
        return result;
    }
    protected void drawRangeCrosshair(Graphics2D g2, Rectangle2D dataArea,
            PlotOrientation orientation, double value, ValueAxis axis,
            Stroke stroke, Paint paint) {

        if (!axis.getRange().contains(value)) {
            return;
        }
        Line2D line = null;
        if (orientation == PlotOrientation.HORIZONTAL) {
            double xx = axis.valueToJava2D(value, dataArea,
                    RectangleEdge.BOTTOM);
            line = new Line2D.Double(xx, dataArea.getMinY(), xx,
                    dataArea.getMaxY());
        }
        else {
            double yy = axis.valueToJava2D(value, dataArea,
                    RectangleEdge.LEFT);
            line = new Line2D.Double(dataArea.getMinX(), yy,
                    dataArea.getMaxX(), yy);
        }
        g2.setStroke(stroke);
        g2.setPaint(paint);
        g2.draw(line);

    }
    public void setRangeAxisLocation(AxisLocation location, boolean notify) {
        setRangeAxisLocation(0, location, notify);
    }
    public CategoryAxis getDomainAxis(int index) {
        CategoryAxis result = null;
        if (index < this.domainAxes.size()) {
            result = (CategoryAxis) this.domainAxes.get(index);
        }
        if (result == null) {
            Plot parent = getParent();
            if (parent instanceof CategoryPlot) {
                CategoryPlot cp = (CategoryPlot) parent;
                result = cp.getDomainAxis(index);
            }
        }
        return result;
    }
    public CategoryItemRenderer getRenderer(int index) {
        CategoryItemRenderer result = null;
        if (this.renderers.size() > index) {
            result = (CategoryItemRenderer) this.renderers.get(index);
        }
        return result;
    }
    public void setRangeAxis(int index, ValueAxis axis, boolean notify) {
        ValueAxis existing = (ValueAxis) this.rangeAxes.get(index);
        if (existing != null) {
            existing.removeChangeListener(this);
        }
        if (axis != null) {
            axis.setPlot(this);
        }
        this.rangeAxes.set(index, axis);
        if (axis != null) {
            axis.configure();
            axis.addChangeListener(this);
        }
        if (notify) {
            notifyListeners(new PlotChangeEvent(this));
        }
    }
    public boolean isRangeGridlinesVisible() {
        return this.rangeGridlinesVisible;
    }
    public void zoomDomainAxes(double lowerPercent, double upperPercent,
                               PlotRenderingInfo state, Point2D source) {
        // can't zoom domain axis
    }
    public void zoomRangeAxes(double factor, PlotRenderingInfo info,
                              Point2D source, boolean useAnchor) {

        // perform the zoom on each range axis
        for (int i = 0; i < this.rangeAxes.size(); i++) {
            ValueAxis rangeAxis = (ValueAxis) this.rangeAxes.get(i);
            if (rangeAxis != null) {
                if (useAnchor) {
                    // get the relevant source coordinate given the plot
                    // orientation
                    double sourceY = source.getY();
                    if (this.orientation == PlotOrientation.HORIZONTAL) {
                        sourceY = source.getX();
                    }
                    double anchorY = rangeAxis.java2DToValue(sourceY,
                            info.getDataArea(), getRangeAxisEdge());
                    rangeAxis.resizeRange(factor, anchorY);
                }
                else {
                    rangeAxis.resizeRange(factor);
                }
            }
        }
    }
    public void draw(Graphics2D g2, Rectangle2D area,
                     Point2D anchor,
                     PlotState parentState,
                     PlotRenderingInfo state) {

        // if the plot area is too small, just return...
        boolean b1 = (area.getWidth() <= MINIMUM_WIDTH_TO_DRAW);
        boolean b2 = (area.getHeight() <= MINIMUM_HEIGHT_TO_DRAW);
        if (b1 || b2) {
            return;
        }

        // record the plot area...
        if (state == null) {
            // if the incoming state is null, no information will be passed
            // back to the caller - but we create a temporary state to record
            // the plot area, since that is used later by the axes
            state = new PlotRenderingInfo(null);
        }
        state.setPlotArea(area);

        // adjust the drawing area for the plot insets (if any)...
        RectangleInsets insets = getInsets();
        insets.trim(area);

        // calculate the data area...
        AxisSpace space = calculateAxisSpace(g2, area);
        Rectangle2D dataArea = space.shrink(area, null);
        this.axisOffset.trim(dataArea);

        state.setDataArea(dataArea);

        // if there is a renderer, it draws the background, otherwise use the
        // default background...
        if (getRenderer() != null) {
            getRenderer().drawBackground(g2, this, dataArea);
        }
        else {
            drawBackground(g2, dataArea);
        }

        Map axisStateMap = drawAxes(g2, area, dataArea, state);

        // don't let anyone draw outside the data area
        Shape savedClip = g2.getClip();
        g2.clip(dataArea);

        drawDomainGridlines(g2, dataArea);

        AxisState rangeAxisState = (AxisState) axisStateMap.get(getRangeAxis());
        if (rangeAxisState == null) {
            if (parentState != null) {
                rangeAxisState = (AxisState) parentState.getSharedAxisStates()
                        .get(getRangeAxis());
            }
        }
        if (rangeAxisState != null) {
            drawRangeGridlines(g2, dataArea, rangeAxisState.getTicks());
        }

        // draw the markers...
        for (int i = 0; i < this.renderers.size(); i++) {
            drawDomainMarkers(g2, dataArea, i, Layer.BACKGROUND);
        }
        for (int i = 0; i < this.renderers.size(); i++) {
            drawRangeMarkers(g2, dataArea, i, Layer.BACKGROUND);
        }

        // now render data items...
        boolean foundData = false;

        // set up the alpha-transparency...
        Composite originalComposite = g2.getComposite();
        g2.setComposite(AlphaComposite.getInstance(
                AlphaComposite.SRC_OVER, getForegroundAlpha()));

        DatasetRenderingOrder order = getDatasetRenderingOrder();
        if (order == DatasetRenderingOrder.FORWARD) {

            // draw background annotations
            int datasetCount = this.datasets.size();
            for (int i = 0; i < datasetCount; i++) {
                CategoryItemRenderer r = getRenderer(i);
                if (r != null) {
                    CategoryAxis domainAxis = getDomainAxisForDataset(i);
                    ValueAxis rangeAxis = getRangeAxisForDataset(i);
                    r.drawAnnotations(g2, dataArea, domainAxis, rangeAxis,
                            Layer.BACKGROUND, state);
                }
            }

            for (int i = 0; i < datasetCount; i++) {
                foundData = render(g2, dataArea, i, state) || foundData;
            }

            // draw foreground annotations
            for (int i = 0; i < datasetCount; i++) {
                CategoryItemRenderer r = getRenderer(i);
                if (r != null) {
                    CategoryAxis domainAxis = getDomainAxisForDataset(i);
                    ValueAxis rangeAxis = getRangeAxisForDataset(i);
                    r.drawAnnotations(g2, dataArea, domainAxis, rangeAxis,
                            Layer.FOREGROUND, state);
                }
            }
        }
        else {  // DatasetRenderingOrder.REVERSE

            // draw background annotations
            int datasetCount = this.datasets.size();
            for (int i = datasetCount - 1; i >= 0; i--) {
                CategoryItemRenderer r = getRenderer(i);
                if (r != null) {
                    CategoryAxis domainAxis = getDomainAxisForDataset(i);
                    ValueAxis rangeAxis = getRangeAxisForDataset(i);
                    r.drawAnnotations(g2, dataArea, domainAxis, rangeAxis,
                            Layer.BACKGROUND, state);
                }
            }

            for (int i = this.datasets.size() - 1; i >= 0; i--) {
                foundData = render(g2, dataArea, i, state) || foundData;
            }

            // draw foreground annotations
            for (int i = datasetCount - 1; i >= 0; i--) {
                CategoryItemRenderer r = getRenderer(i);
                if (r != null) {
                    CategoryAxis domainAxis = getDomainAxisForDataset(i);
                    ValueAxis rangeAxis = getRangeAxisForDataset(i);
                    r.drawAnnotations(g2, dataArea, domainAxis, rangeAxis,
                            Layer.FOREGROUND, state);
                }
            }
        }

        // draw the foreground markers...
        for (int i = 0; i < this.renderers.size(); i++) {
            drawDomainMarkers(g2, dataArea, i, Layer.FOREGROUND);
        }
        for (int i = 0; i < this.renderers.size(); i++) {
            drawRangeMarkers(g2, dataArea, i, Layer.FOREGROUND);
        }

        // draw the plot's annotations (if any)...
        drawAnnotations(g2, dataArea, state);

        g2.setClip(savedClip);
        g2.setComposite(originalComposite);

        if (!foundData) {
            drawNoDataMessage(g2, dataArea);
        }

        // draw range crosshair if required...
        if (isRangeCrosshairVisible()) {
            // FIXME: this doesn't handle multiple range axes
            drawRangeCrosshair(g2, dataArea, getOrientation(),
                    getRangeCrosshairValue(), getRangeAxis(),
                    getRangeCrosshairStroke(), getRangeCrosshairPaint());
        }

        // draw an outline around the plot area...
        if (getRenderer() != null) {
            getRenderer().drawOutline(g2, this, dataArea);
        }
        else {
            drawOutline(g2, dataArea);
        }

    }
    public void clearRangeMarkers() {
        if (this.backgroundRangeMarkers != null) {
            Set keys = this.backgroundRangeMarkers.keySet();
            Iterator iterator = keys.iterator();
            while (iterator.hasNext()) {
                Integer key = (Integer) iterator.next();
                clearRangeMarkers(key.intValue());
            }
            this.backgroundRangeMarkers.clear();
        }
        if (this.foregroundRangeMarkers != null) {
            Set keys = this.foregroundRangeMarkers.keySet();
            Iterator iterator = keys.iterator();
            while (iterator.hasNext()) {
                Integer key = (Integer) iterator.next();
                clearRangeMarkers(key.intValue());
            }
            this.foregroundRangeMarkers.clear();
        }
        notifyListeners(new PlotChangeEvent(this));
    }
    public RectangleEdge getDomainAxisEdge() {
        return getDomainAxisEdge(0);
    }
    public AxisLocation getDomainAxisLocation() {
        return getDomainAxisLocation(0);
    }
    private List datasetsMappedToDomainAxis(int axisIndex) {
        List result = new ArrayList();
        for (int datasetIndex = 0; datasetIndex < this.datasets.size();
                datasetIndex++) {
            Object dataset = this.datasets.get(datasetIndex);
            if (dataset != null) {
                Integer m = (Integer) this.datasetToDomainAxisMap.get(
                        datasetIndex);
                if (m == null) {  // a dataset with no mapping is assigned to
                                  // axis 0
                    if (axisIndex == 0) {
                        result.add(dataset);
                    }
                }
                else {
                    if (m.intValue() == axisIndex) {
                        result.add(dataset);
                    }
                }
            }
        }
        return result;
    }
    public RectangleEdge getRangeAxisEdge(int index) {
        AxisLocation location = getRangeAxisLocation(index);
        RectangleEdge result = Plot.resolveRangeAxisLocation(location,
                this.orientation);
        if (result == null) {
            result = RectangleEdge.opposite(getRangeAxisEdge(0));
        }
        return result;
    }
    public void clearRangeMarkers(int index) {
        Integer key = new Integer(index);
        if (this.backgroundRangeMarkers != null) {
            Collection markers
                = (Collection) this.backgroundRangeMarkers.get(key);
            if (markers != null) {
                Iterator iterator = markers.iterator();
                while (iterator.hasNext()) {
                    Marker m = (Marker) iterator.next();
                    m.removeChangeListener(this);
                }
                markers.clear();
            }
        }
        if (this.foregroundRangeMarkers != null) {
            Collection markers
                = (Collection) this.foregroundRangeMarkers.get(key);
            if (markers != null) {
                Iterator iterator = markers.iterator();
                while (iterator.hasNext()) {
                    Marker m = (Marker) iterator.next();
                    m.removeChangeListener(this);
                }
                markers.clear();
            }
        }
        notifyListeners(new PlotChangeEvent(this));
    }
    public void clearDomainMarkers(int index) {
        Integer key = new Integer(index);
        if (this.backgroundDomainMarkers != null) {
            Collection markers
                = (Collection) this.backgroundDomainMarkers.get(key);
            if (markers != null) {
                Iterator iterator = markers.iterator();
                while (iterator.hasNext()) {
                    Marker m = (Marker) iterator.next();
                    m.removeChangeListener(this);
                }
                markers.clear();
            }
        }
        if (this.foregroundDomainMarkers != null) {
            Collection markers
                = (Collection) this.foregroundDomainMarkers.get(key);
            if (markers != null) {
                Iterator iterator = markers.iterator();
                while (iterator.hasNext()) {
                    Marker m = (Marker) iterator.next();
                    m.removeChangeListener(this);
                }
                markers.clear();
            }
        }
        notifyListeners(new PlotChangeEvent(this));
    }
    protected void drawDomainMarkers(Graphics2D g2, Rectangle2D dataArea,
                                     int index, Layer layer) {

        CategoryItemRenderer r = getRenderer(index);
        if (r == null) {
            return;
        }

        Collection markers = getDomainMarkers(index, layer);
        CategoryAxis axis = getDomainAxisForDataset(index);
        if (markers != null && axis != null) {
            Iterator iterator = markers.iterator();
            while (iterator.hasNext()) {
                CategoryMarker marker = (CategoryMarker) iterator.next();
                r.drawDomainMarker(g2, this, axis, marker, dataArea);
            }
        }

    }
    public void setFixedRangeAxisSpace(AxisSpace space) {
        this.fixedRangeAxisSpace = space;
        // TODO: fire event?
    }
    public CategoryDataset getDataset(int index) {
        CategoryDataset result = null;
        if (this.datasets.size() > index) {
            result = (CategoryDataset) this.datasets.get(index);
        }
        return result;
    }
    public void setAnchorValue(double value, boolean notify) {
        this.anchorValue = value;
        if (notify) {
            notifyListeners(new PlotChangeEvent(this));
        }
    }
    public void setDomainAxis(int index, CategoryAxis axis) {
        setDomainAxis(index, axis, true);
    }
    public CategoryAnchor getDomainGridlinePosition() {
        return this.domainGridlinePosition;
    }
    public void addRangeMarker(Marker marker) {
        addRangeMarker(marker, Layer.FOREGROUND);
    }
    public ValueAxis getRangeAxis(int index) {
        ValueAxis result = null;
        if (index < this.rangeAxes.size()) {
            result = (ValueAxis) this.rangeAxes.get(index);
        }
        if (result == null) {
            Plot parent = getParent();
            if (parent instanceof CategoryPlot) {
                CategoryPlot cp = (CategoryPlot) parent;
                result = cp.getRangeAxis(index);
            }
        }
        return result;
    }
    public SortOrder getRowRenderingOrder() {
        return this.rowRenderingOrder;
    }
    public CategoryItemRenderer getRenderer() {
        return getRenderer(0);
    }
    public void setRenderer(int index, CategoryItemRenderer renderer) {
        setRenderer(index, renderer, true);
    }
    public void mapDatasetToDomainAxis(int index, int axisIndex) {
        this.datasetToDomainAxisMap.set(index, new Integer(axisIndex));
        // fake a dataset change event to update axes...
        datasetChanged(new DatasetChangeEvent(this, getDataset(index)));
    }
    protected void drawRangeGridlines(Graphics2D g2, Rectangle2D dataArea,
                                      List ticks) {
        // draw the range grid lines, if any...
        if (isRangeGridlinesVisible()) {
            Stroke gridStroke = getRangeGridlineStroke();
            Paint gridPaint = getRangeGridlinePaint();
            if ((gridStroke != null) && (gridPaint != null)) {
                ValueAxis axis = getRangeAxis();
                if (axis != null) {
                    Iterator iterator = ticks.iterator();
                    while (iterator.hasNext()) {
                        ValueTick tick = (ValueTick) iterator.next();
                        CategoryItemRenderer renderer1 = getRenderer();
                        if (renderer1 != null) {
                            renderer1.drawRangeGridline(g2, this,
                                    getRangeAxis(), dataArea, tick.getValue());
                        }
                    }
                }
            }
        }
    }
    public boolean removeAnnotation(CategoryAnnotation annotation) {
        if (annotation == null) {
            throw new IllegalArgumentException("Null 'annotation' argument.");
        }
        boolean removed = this.annotations.remove(annotation);
        if (removed) {
            notifyListeners(new PlotChangeEvent(this));
        }
        return removed;
    }
    public void datasetChanged(DatasetChangeEvent event) {

        int count = this.rangeAxes.size();
        for (int axisIndex = 0; axisIndex < count; axisIndex++) {
            ValueAxis yAxis = getRangeAxis(axisIndex);
            if (yAxis != null) {
                yAxis.configure();
            }
        }
        if (getParent() != null) {
            getParent().datasetChanged(event);
        }
        else {
            PlotChangeEvent e = new PlotChangeEvent(this);
            e.setType(ChartChangeEventType.DATASET_UPDATED);
            notifyListeners(e);
        }

    }
    public void addDomainMarker(CategoryMarker marker, Layer layer) {
        addDomainMarker(0, marker, layer);
    }
    public void setAxisOffset(RectangleInsets offset) {
        if (offset == null) {
            throw new IllegalArgumentException("Null 'offset' argument.");
        }
        this.axisOffset = offset;
        notifyListeners(new PlotChangeEvent(this));
    }
    public void mapDatasetToRangeAxis(int index, int axisIndex) {
        this.datasetToRangeAxisMap.set(index, new Integer(axisIndex));
        // fake a dataset change event to update axes...
        datasetChanged(new DatasetChangeEvent(this, getDataset(index)));
    }
    public void addAnnotation(CategoryAnnotation annotation) {
        if (annotation == null) {
            throw new IllegalArgumentException("Null 'annotation' argument.");
        }
        this.annotations.add(annotation);
        notifyListeners(new PlotChangeEvent(this));
    }
    public void setOrientation(PlotOrientation orientation) {
        if (orientation == null) {
            throw new IllegalArgumentException("Null 'orientation' argument.");
        }
        this.orientation = orientation;
        notifyListeners(new PlotChangeEvent(this));
    }
    public void setRangeCrosshairStroke(Stroke stroke) {
        if (stroke == null) {
            throw new IllegalArgumentException("Null 'stroke' argument.");
        }
        this.rangeCrosshairStroke = stroke;
        notifyListeners(new PlotChangeEvent(this));
    }
    public void setRangeAxisLocation(int index, AxisLocation location) {
        setRangeAxisLocation(index, location, true);
    }
    public List getCategories() {
        List result = null;
        if (getDataset() != null) {
            result = Collections.unmodifiableList(getDataset().getColumnKeys());
        }
        return result;
    }
    public boolean isDomainGridlinesVisible() {
        return this.domainGridlinesVisible;
    }
    public void setRenderer(CategoryItemRenderer renderer) {
        setRenderer(0, renderer, true);
    }
    public void addRangeMarker(int index, Marker marker, Layer layer) {
        Collection markers;
        if (layer == Layer.FOREGROUND) {
            markers = (Collection) this.foregroundRangeMarkers.get(
                    new Integer(index));
            if (markers == null) {
                markers = new java.util.ArrayList();
                this.foregroundRangeMarkers.put(new Integer(index), markers);
            }
            markers.add(marker);
        }
        else if (layer == Layer.BACKGROUND) {
            markers = (Collection) this.backgroundRangeMarkers.get(
                    new Integer(index));
            if (markers == null) {
                markers = new java.util.ArrayList();
                this.backgroundRangeMarkers.put(new Integer(index), markers);
            }
            markers.add(marker);
        }
        marker.addChangeListener(this);
        notifyListeners(new PlotChangeEvent(this));
    }
    public Collection getRangeMarkers(Layer layer) {
        return getRangeMarkers(0, layer);
    }
    public void zoom(double percent) {

        if (percent > 0.0) {
            double range = getRangeAxis().getRange().getLength();
            double scaledRange = range * percent;
            getRangeAxis().setRange(this.anchorValue - scaledRange / 2.0,
                    this.anchorValue + scaledRange / 2.0);
        }
        else {
            getRangeAxis().setAutoRange(true);
        }

    }
    public CategoryAxis getDomainAxisForDataset(int index) {
        CategoryAxis result = getDomainAxis();
        Integer axisIndex = (Integer) this.datasetToDomainAxisMap.get(index);
        if (axisIndex != null) {
            result = getDomainAxis(axisIndex.intValue());
        }
        return result;
    }
    public void setRangeGridlineStroke(Stroke stroke) {
        if (stroke == null) {
            throw new IllegalArgumentException("Null 'stroke' argument.");
        }
        this.rangeGridlineStroke = stroke;
        notifyListeners(new PlotChangeEvent(this));
    }
    public String getPlotType() {
        return localizationResources.getString("Category_Plot");
    }
    public Collection getDomainMarkers(int index, Layer layer) {
        Collection result = null;
        Integer key = new Integer(index);
        if (layer == Layer.FOREGROUND) {
            result = (Collection) this.foregroundDomainMarkers.get(key);
        }
        else if (layer == Layer.BACKGROUND) {
            result = (Collection) this.backgroundDomainMarkers.get(key);
        }
        if (result != null) {
            result = Collections.unmodifiableCollection(result);
        }
        return result;
    }
    public void setRangeGridlinePaint(Paint paint) {
        if (paint == null) {
            throw new IllegalArgumentException("Null 'paint' argument.");
        }
        this.rangeGridlinePaint = paint;
        notifyListeners(new PlotChangeEvent(this));
    }
    public void setRangeCrosshairValue(double value) {
        setRangeCrosshairValue(value, true);
    }
    protected void drawRangeMarkers(Graphics2D g2, Rectangle2D dataArea,
                                    int index, Layer layer) {

        CategoryItemRenderer r = getRenderer(index);
        if (r == null) {
            return;
        }

        Collection markers = getRangeMarkers(index, layer);
        ValueAxis axis = getRangeAxisForDataset(index);
        if (markers != null && axis != null) {
            Iterator iterator = markers.iterator();
            while (iterator.hasNext()) {
                Marker marker = (Marker) iterator.next();
                r.drawRangeMarker(g2, this, axis, marker, dataArea);
            }
        }

    }
    public void setFixedLegendItems(LegendItemCollection items) {
        this.fixedLegendItems = items;
        notifyListeners(new PlotChangeEvent(this));
    }
    public void setDataset(int index, CategoryDataset dataset) {

        CategoryDataset existing = (CategoryDataset) this.datasets.get(index);
        if (existing != null) {
            existing.removeChangeListener(this);
        }
        this.datasets.set(index, dataset);
        if (dataset != null) {
            dataset.addChangeListener(this);
        }

        // send a dataset change event to self...
        DatasetChangeEvent event = new DatasetChangeEvent(this, dataset);
        datasetChanged(event);

    }
    public RectangleEdge getDomainAxisEdge(int index) {
        RectangleEdge result = null;
        AxisLocation location = getDomainAxisLocation(index);
        if (location != null) {
            result = Plot.resolveDomainAxisLocation(location, this.orientation);
        }
        else {
            result = RectangleEdge.opposite(getDomainAxisEdge(0));
        }
        return result;
    }
    public void setDrawSharedDomainAxis(boolean draw) {
        this.drawSharedDomainAxis = draw;
        notifyListeners(new PlotChangeEvent(this));
    }
    public DatasetRenderingOrder getDatasetRenderingOrder() {
        return this.renderingOrder;
    }
    public void addDomainMarker(int index, CategoryMarker marker, Layer layer) {
        if (marker == null) {
            throw new IllegalArgumentException("Null 'marker' not permitted.");
        }
        if (layer == null) {
            throw new IllegalArgumentException("Null 'layer' not permitted.");
        }
        Collection markers;
        if (layer == Layer.FOREGROUND) {
            markers = (Collection) this.foregroundDomainMarkers.get(
                    new Integer(index));
            if (markers == null) {
                markers = new java.util.ArrayList();
                this.foregroundDomainMarkers.put(new Integer(index), markers);
            }
            markers.add(marker);
        }
        else if (layer == Layer.BACKGROUND) {
            markers = (Collection) this.backgroundDomainMarkers.get(
                    new Integer(index));
            if (markers == null) {
                markers = new java.util.ArrayList();
                this.backgroundDomainMarkers.put(new Integer(index), markers);
            }
            markers.add(marker);
        }
        marker.addChangeListener(this);
        notifyListeners(new PlotChangeEvent(this));
    }
    public double getRangeCrosshairValue() {
        return this.rangeCrosshairValue;
    }
    public AxisSpace getFixedRangeAxisSpace() {
        return this.fixedRangeAxisSpace;
    }
    public AxisSpace getFixedDomainAxisSpace() {
        return this.fixedDomainAxisSpace;
    }
    public void setDomainGridlinesVisible(boolean visible) {
        if (this.domainGridlinesVisible != visible) {
            this.domainGridlinesVisible = visible;
            notifyListeners(new PlotChangeEvent(this));
        }
    }
    public int getRangeAxisIndex(ValueAxis axis) {
        int result = this.rangeAxes.indexOf(axis);
        if (result < 0) { // try the parent plot
            Plot parent = getParent();
            if (parent instanceof CategoryPlot) {
                CategoryPlot p = (CategoryPlot) parent;
                result = p.getRangeAxisIndex(axis);
            }
        }
if (axis == null){throw new IllegalArgumentException();}        return result;
    }
    public List getAnnotations() {
        return this.annotations;
    }
    public void setRenderers(CategoryItemRenderer[] renderers) {
        for (int i = 0; i < renderers.length; i++) {
            setRenderer(i, renderers[i], false);
        }
        notifyListeners(new PlotChangeEvent(this));
    }
    public CategoryItemRenderer getRendererForDataset(CategoryDataset dataset) {
        CategoryItemRenderer result = null;
        for (int i = 0; i < this.datasets.size(); i++) {
            if (this.datasets.get(i) == dataset) {
                result = (CategoryItemRenderer) this.renderers.get(i);
                break;
            }
        }
        return result;
    }
    public boolean isDomainZoomable() {
        return false;
    }
    public void setDomainAxis(int index, CategoryAxis axis, boolean notify) {
        CategoryAxis existing = (CategoryAxis) this.domainAxes.get(index);
        if (existing != null) {
            existing.removeChangeListener(this);
        }
        if (axis != null) {
            axis.setPlot(this);
        }
        this.domainAxes.set(index, axis);
        if (axis != null) {
            axis.configure();
            axis.addChangeListener(this);
        }
        if (notify) {
            notifyListeners(new PlotChangeEvent(this));
        }
    }
    public void setDomainGridlinePaint(Paint paint) {
        if (paint == null) {
            throw new IllegalArgumentException("Null 'paint' argument.");
        }
        this.domainGridlinePaint = paint;
        notifyListeners(new PlotChangeEvent(this));
    }
    public PlotOrientation getOrientation() {
        return this.orientation;
    }
    public int getDomainAxisCount() {
        return this.domainAxes.size();
    }
    public void setDomainAxis(CategoryAxis axis) {
        setDomainAxis(0, axis);
    }
    public void setRangeAxisLocation(int index, AxisLocation location,
                                     boolean notify) {
        if (index == 0 && location == null) {
            throw new IllegalArgumentException(
                    "Null 'location' for index 0 not permitted.");
        }
        this.rangeAxisLocations.set(index, location);
        if (notify) {
            notifyListeners(new PlotChangeEvent(this));
        }
    }
    public int getRangeAxisCount() {
        return this.rangeAxes.size();
    }
    public void handleClick(int x, int y, PlotRenderingInfo info) {

        Rectangle2D dataArea = info.getDataArea();
        if (dataArea.contains(x, y)) {
            // set the anchor value for the range axis...
            double java2D = 0.0;
            if (this.orientation == PlotOrientation.HORIZONTAL) {
                java2D = x;
            }
            else if (this.orientation == PlotOrientation.VERTICAL) {
                java2D = y;
            }
            RectangleEdge edge = Plot.resolveRangeAxisLocation(
                    getRangeAxisLocation(), this.orientation);
            double value = getRangeAxis().java2DToValue(
                    java2D, info.getDataArea(), edge);
            setAnchorValue(value);
            setRangeCrosshairValue(value);
        }

    }
    public LegendItemCollection getFixedLegendItems() {
        return this.fixedLegendItems;
    }
    protected void drawRangeLine(Graphics2D g2, Rectangle2D dataArea,
            double value, Stroke stroke, Paint paint) {

        double java2D = getRangeAxis().valueToJava2D(value, dataArea,
                getRangeAxisEdge());
        Line2D line = null;
        if (this.orientation == PlotOrientation.HORIZONTAL) {
            line = new Line2D.Double(java2D, dataArea.getMinY(), java2D,
                    dataArea.getMaxY());
        }
        else if (this.orientation == PlotOrientation.VERTICAL) {
            line = new Line2D.Double(dataArea.getMinX(), java2D,
                    dataArea.getMaxX(), java2D);
        }
        g2.setStroke(stroke);
        g2.setPaint(paint);
        g2.draw(line);

    }
    public AxisLocation getRangeAxisLocation() {
        return getRangeAxisLocation(0);
    }
    public List getCategoriesForAxis(CategoryAxis axis) {
        List result = new ArrayList();
        int axisIndex = this.domainAxes.indexOf(axis);
        List datasets = datasetsMappedToDomainAxis(axisIndex);
        Iterator iterator = datasets.iterator();
        while (iterator.hasNext()) {
            CategoryDataset dataset = (CategoryDataset) iterator.next();
            // add the unique categories from this dataset
            for (int i = 0; i < dataset.getColumnCount(); i++) {
                Comparable category = dataset.getColumnKey(i);
                if (!result.contains(category)) {
                    result.add(category);
                }
            }
        }
        return result;
    }
    public CategoryDataset getDataset() {
        return getDataset(0);
    }
    public ValueAxis getRangeAxis() {
        return getRangeAxis(0);
    }
    public void setDomainAxisLocation(int index, AxisLocation location,
            boolean notify) {
        if (index == 0 && location == null) {
            throw new IllegalArgumentException(
                    "Null 'location' for index 0 not permitted.");
        }
        this.domainAxisLocations.set(index, location);
        if (notify) {
            notifyListeners(new PlotChangeEvent(this));
        }
    }
    public void rendererChanged(RendererChangeEvent event) {
        Plot parent = getParent();
        if (parent != null) {
            if (parent instanceof RendererChangeListener) {
                RendererChangeListener rcl = (RendererChangeListener) parent;
                rcl.rendererChanged(event);
            }
            else {
                // this should never happen with the existing code, but throw
                // an exception in case future changes make it possible...
                throw new RuntimeException(
                    "The renderer has changed and I don't know what to do!");
            }
        }
        else {
            configureRangeAxes();
            PlotChangeEvent e = new PlotChangeEvent(this);
            notifyListeners(e);
        }
    }
    public CategoryAxis getDomainAxis() {
        return getDomainAxis(0);
    }
    public void setDataset(CategoryDataset dataset) {
        setDataset(0, dataset);
    }
    public void setRangeCrosshairVisible(boolean flag) {
        if (this.rangeCrosshairVisible != flag) {
            this.rangeCrosshairVisible = flag;
            notifyListeners(new PlotChangeEvent(this));
        }
    }
    public RectangleEdge getRangeAxisEdge() {
        return getRangeAxisEdge(0);
    }
    public void clearDomainAxes() {
        for (int i = 0; i < this.domainAxes.size(); i++) {
            CategoryAxis axis = (CategoryAxis) this.domainAxes.get(i);
            if (axis != null) {
                axis.removeChangeListener(this);
            }
        }
        this.domainAxes.clear();
        notifyListeners(new PlotChangeEvent(this));
    }
    protected Map drawAxes(Graphics2D g2,
                           Rectangle2D plotArea,
                           Rectangle2D dataArea,
                           PlotRenderingInfo plotState) {

        AxisCollection axisCollection = new AxisCollection();

        // add domain axes to lists...
        for (int index = 0; index < this.domainAxes.size(); index++) {
            CategoryAxis xAxis = (CategoryAxis) this.domainAxes.get(index);
            if (xAxis != null) {
                axisCollection.add(xAxis, getDomainAxisEdge(index));
            }
        }

        // add range axes to lists...
        for (int index = 0; index < this.rangeAxes.size(); index++) {
            ValueAxis yAxis = (ValueAxis) this.rangeAxes.get(index);
            if (yAxis != null) {
                axisCollection.add(yAxis, getRangeAxisEdge(index));
            }
        }

        Map axisStateMap = new HashMap();

        // draw the top axes
        double cursor = dataArea.getMinY() - this.axisOffset.calculateTopOutset(
                dataArea.getHeight());
        Iterator iterator = axisCollection.getAxesAtTop().iterator();
        while (iterator.hasNext()) {
            Axis axis = (Axis) iterator.next();
            if (axis != null) {
                AxisState axisState = axis.draw(g2, cursor, plotArea, dataArea,
                        RectangleEdge.TOP, plotState);
                cursor = axisState.getCursor();
                axisStateMap.put(axis, axisState);
            }
        }

        // draw the bottom axes
        cursor = dataArea.getMaxY()
                 + this.axisOffset.calculateBottomOutset(dataArea.getHeight());
        iterator = axisCollection.getAxesAtBottom().iterator();
        while (iterator.hasNext()) {
            Axis axis = (Axis) iterator.next();
            if (axis != null) {
                AxisState axisState = axis.draw(g2, cursor, plotArea, dataArea,
                        RectangleEdge.BOTTOM, plotState);
                cursor = axisState.getCursor();
                axisStateMap.put(axis, axisState);
            }
        }

        // draw the left axes
        cursor = dataArea.getMinX()
                 - this.axisOffset.calculateLeftOutset(dataArea.getWidth());
        iterator = axisCollection.getAxesAtLeft().iterator();
        while (iterator.hasNext()) {
            Axis axis = (Axis) iterator.next();
            if (axis != null) {
                AxisState axisState = axis.draw(g2, cursor, plotArea, dataArea,
                        RectangleEdge.LEFT, plotState);
                cursor = axisState.getCursor();
                axisStateMap.put(axis, axisState);
            }
        }

        // draw the right axes
        cursor = dataArea.getMaxX()
                 + this.axisOffset.calculateRightOutset(dataArea.getWidth());
        iterator = axisCollection.getAxesAtRight().iterator();
        while (iterator.hasNext()) {
            Axis axis = (Axis) iterator.next();
            if (axis != null) {
                AxisState axisState = axis.draw(g2, cursor, plotArea, dataArea,
                        RectangleEdge.RIGHT, plotState);
                cursor = axisState.getCursor();
                axisStateMap.put(axis, axisState);
            }
        }

        return axisStateMap;

    }
    public LegendItemCollection getLegendItems() {
        LegendItemCollection result = this.fixedLegendItems;
        if (result == null) {
            result = new LegendItemCollection();
            // get the legend items for the datasets...
            int count = this.datasets.size();
            for (int datasetIndex = 0; datasetIndex < count; datasetIndex++) {
                CategoryDataset dataset = getDataset(datasetIndex);
                if (dataset != null) {
                    CategoryItemRenderer renderer = getRenderer(datasetIndex);
                    if (renderer != null) {
                        int seriesCount = dataset.getRowCount();
                        for (int i = 0; i < seriesCount; i++) {
                            LegendItem item = renderer.getLegendItem(
                                    datasetIndex, i);
                            if (item != null) {
                                result.add(item);
                            }
                        }
                    }
                }
            }
        }
        return result;
    }
    public void setDomainAxisLocation(AxisLocation location, boolean notify) {
        // delegate...
        setDomainAxisLocation(0, location, notify);
    }
    public boolean render(Graphics2D g2, Rectangle2D dataArea, int index,
                          PlotRenderingInfo info) {

        boolean foundData = false;
        CategoryDataset currentDataset = getDataset(index);
        CategoryItemRenderer renderer = getRenderer(index);
        CategoryAxis domainAxis = getDomainAxisForDataset(index);
        ValueAxis rangeAxis = getRangeAxisForDataset(index);
        boolean hasData = !DatasetUtilities.isEmptyOrNull(currentDataset);
        if (hasData && renderer != null) {

            foundData = true;
            CategoryItemRendererState state = renderer.initialise(g2, dataArea,
                    this, index, info);
            int columnCount = currentDataset.getColumnCount();
            int rowCount = currentDataset.getRowCount();
            int passCount = renderer.getPassCount();
            for (int pass = 0; pass < passCount; pass++) {
                if (this.columnRenderingOrder == SortOrder.ASCENDING) {
                    for (int column = 0; column < columnCount; column++) {
                        if (this.rowRenderingOrder == SortOrder.ASCENDING) {
                            for (int row = 0; row < rowCount; row++) {
                                renderer.drawItem(g2, state, dataArea, this,
                                        domainAxis, rangeAxis, currentDataset,
                                        row, column, pass);
                            }
                        }
                        else {
                            for (int row = rowCount - 1; row >= 0; row--) {
                                renderer.drawItem(g2, state, dataArea, this,
                                        domainAxis, rangeAxis, currentDataset,
                                        row, column, pass);
                            }
                        }
                    }
                }
                else {
                    for (int column = columnCount - 1; column >= 0; column--) {
                        if (this.rowRenderingOrder == SortOrder.ASCENDING) {
                            for (int row = 0; row < rowCount; row++) {
                                renderer.drawItem(g2, state, dataArea, this,
                                        domainAxis, rangeAxis, currentDataset,
                                        row, column, pass);
                            }
                        }
                        else {
                            for (int row = rowCount - 1; row >= 0; row--) {
                                renderer.drawItem(g2, state, dataArea, this,
                                        domainAxis, rangeAxis, currentDataset,
                                        row, column, pass);
                            }
                        }
                    }
                }
            }
        }
        return foundData;

    }
    public Object clone() throws CloneNotSupportedException {

        CategoryPlot clone = (CategoryPlot) super.clone();

        clone.domainAxes = new ObjectList();
        for (int i = 0; i < this.domainAxes.size(); i++) {
            CategoryAxis xAxis = (CategoryAxis) this.domainAxes.get(i);
            if (xAxis != null) {
                CategoryAxis clonedAxis = (CategoryAxis) xAxis.clone();
                clone.setDomainAxis(i, clonedAxis);
            }
        }
        clone.domainAxisLocations
            = (ObjectList) this.domainAxisLocations.clone();

        clone.rangeAxes = new ObjectList();
        for (int i = 0; i < this.rangeAxes.size(); i++) {
            ValueAxis yAxis = (ValueAxis) this.rangeAxes.get(i);
            if (yAxis != null) {
                ValueAxis clonedAxis = (ValueAxis) yAxis.clone();
                clone.setRangeAxis(i, clonedAxis);
            }
        }
        clone.rangeAxisLocations = (ObjectList) this.rangeAxisLocations.clone();

        clone.datasets = (ObjectList) this.datasets.clone();
        for (int i = 0; i < clone.datasets.size(); i++) {
            CategoryDataset dataset = clone.getDataset(i);
            if (dataset != null) {
                dataset.addChangeListener(clone);
            }
        }
        clone.datasetToDomainAxisMap
            = (ObjectList) this.datasetToDomainAxisMap.clone();
        clone.datasetToRangeAxisMap
            = (ObjectList) this.datasetToRangeAxisMap.clone();
        clone.renderers = (ObjectList) this.renderers.clone();
        if (this.fixedDomainAxisSpace != null) {
            clone.fixedDomainAxisSpace = (AxisSpace) ObjectUtilities.clone(
                    this.fixedDomainAxisSpace);
        }
        if (this.fixedRangeAxisSpace != null) {
            clone.fixedRangeAxisSpace = (AxisSpace) ObjectUtilities.clone(
                    this.fixedRangeAxisSpace);
        }

        return clone;

    }
    public AxisLocation getDomainAxisLocation(int index) {
        AxisLocation result = null;
        if (index < this.domainAxisLocations.size()) {
            result = (AxisLocation) this.domainAxisLocations.get(index);
        }
        if (result == null) {
            result = AxisLocation.getOpposite(getDomainAxisLocation(0));
        }
        return result;
    }
    public double getAnchorValue() {
        return this.anchorValue;
    }
    public boolean getDrawSharedDomainAxis() {
        return this.drawSharedDomainAxis;
    }
    public void zoomDomainAxes(double factor, PlotRenderingInfo state,
                               Point2D source) {
        // can't zoom domain axis
    }
    public void setRangeCrosshairLockedOnData(boolean flag) {

        if (this.rangeCrosshairLockedOnData != flag) {
            this.rangeCrosshairLockedOnData = flag;
            notifyListeners(new PlotChangeEvent(this));
        }

    }
    public void clearRangeAxes() {
        for (int i = 0; i < this.rangeAxes.size(); i++) {
            ValueAxis axis = (ValueAxis) this.rangeAxes.get(i);
            if (axis != null) {
                axis.removeChangeListener(this);
            }
        }
        this.rangeAxes.clear();
        notifyListeners(new PlotChangeEvent(this));
    }
    public boolean isRangeCrosshairLockedOnData() {
        return this.rangeCrosshairLockedOnData;
    }
    public void setDomainAxisLocation(int index, AxisLocation location) {
        // delegate...
        setDomainAxisLocation(index, location, true);
    }
    public Range getDataRange(ValueAxis axis) {

        Range result = null;
        List mappedDatasets = new ArrayList();

        int rangeIndex = this.rangeAxes.indexOf(axis);
        if (rangeIndex >= 0) {
            mappedDatasets.addAll(datasetsMappedToRangeAxis(rangeIndex));
        }
        else if (axis == getRangeAxis()) {
            mappedDatasets.addAll(datasetsMappedToRangeAxis(0));
        }

        // iterate through the datasets that map to the axis and get the union
        // of the ranges.
        Iterator iterator = mappedDatasets.iterator();
        while (iterator.hasNext()) {
            CategoryDataset d = (CategoryDataset) iterator.next();
            CategoryItemRenderer r = getRendererForDataset(d);
            if (r != null) {
                result = Range.combine(result, r.findRangeBounds(d));
            }
        }
        return result;

    }
    public void zoomRangeAxes(double lowerPercent, double upperPercent,
                              PlotRenderingInfo state, Point2D source) {
        for (int i = 0; i < this.rangeAxes.size(); i++) {
            ValueAxis rangeAxis = (ValueAxis) this.rangeAxes.get(i);
            if (rangeAxis != null) {
                rangeAxis.zoomRange(lowerPercent, upperPercent);
            }
        }
    }
    public AxisLocation getRangeAxisLocation(int index) {
        AxisLocation result = null;
        if (index < this.rangeAxisLocations.size()) {
            result = (AxisLocation) this.rangeAxisLocations.get(index);
        }
        if (result == null) {
            result = AxisLocation.getOpposite(getRangeAxisLocation(0));
        }
        return result;
    }
    public void setRangeCrosshairPaint(Paint paint) {
        if (paint == null) {
            throw new IllegalArgumentException("Null 'paint' argument.");
        }
        this.rangeCrosshairPaint = paint;
        notifyListeners(new PlotChangeEvent(this));
    }
    protected void drawDomainGridlines(Graphics2D g2, Rectangle2D dataArea) {

        // draw the domain grid lines, if any...
        if (isDomainGridlinesVisible()) {
            CategoryAnchor anchor = getDomainGridlinePosition();
            RectangleEdge domainAxisEdge = getDomainAxisEdge();
            Stroke gridStroke = getDomainGridlineStroke();
            Paint gridPaint = getDomainGridlinePaint();
            if ((gridStroke != null) && (gridPaint != null)) {
                // iterate over the categories
                CategoryDataset data = getDataset();
                if (data != null) {
                    CategoryAxis axis = getDomainAxis();
                    if (axis != null) {
                        int columnCount = data.getColumnCount();
                        for (int c = 0; c < columnCount; c++) {
                            double xx = axis.getCategoryJava2DCoordinate(
                                    anchor, c, columnCount, dataArea,
                                    domainAxisEdge);
                            CategoryItemRenderer renderer1 = getRenderer();
                            if (renderer1 != null) {
                                renderer1.drawDomainGridline(g2, this,
                                        dataArea, xx);
                            }
                        }
                    }
                }
            }
        }
    }
    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.defaultWriteObject();
        SerialUtilities.writeStroke(this.domainGridlineStroke, stream);
        SerialUtilities.writePaint(this.domainGridlinePaint, stream);
        SerialUtilities.writeStroke(this.rangeGridlineStroke, stream);
        SerialUtilities.writePaint(this.rangeGridlinePaint, stream);
        SerialUtilities.writeStroke(this.rangeCrosshairStroke, stream);
        SerialUtilities.writePaint(this.rangeCrosshairPaint, stream);
    }
    public void setFixedDomainAxisSpace(AxisSpace space) {
        this.fixedDomainAxisSpace = space;
        // TODO: notify?
    }
    public void setWeight(int weight) {
        this.weight = weight;
        // TODO: notify?
    }
    public ValueAxis getRangeAxisForDataset(int index) {
        ValueAxis result = getRangeAxis();
        Integer axisIndex = (Integer) this.datasetToRangeAxisMap.get(index);
        if (axisIndex != null) {
            result = getRangeAxis(axisIndex.intValue());
        }
        return result;
    }
    public RectangleInsets getAxisOffset() {
        return this.axisOffset;
    }
    public boolean isRangeCrosshairVisible() {
        return this.rangeCrosshairVisible;
    }
    protected void drawAnnotations(Graphics2D g2, Rectangle2D dataArea,
            PlotRenderingInfo info) {

        Iterator iterator = getAnnotations().iterator();
        while (iterator.hasNext()) {
            CategoryAnnotation annotation
                    = (CategoryAnnotation) iterator.next();
            annotation.draw(g2, this, dataArea, getDomainAxis(),
                    getRangeAxis(), 0, info);
        }

    }
    public void configureDomainAxes() {
        for (int i = 0; i < this.domainAxes.size(); i++) {
            CategoryAxis axis = (CategoryAxis) this.domainAxes.get(i);
            if (axis != null) {
                axis.configure();
            }
        }
    }
    public int getDatasetCount() {
        return this.datasets.size();
    }
    public CategoryPlot() {
        this(null, null, null, null);
    }
    public void setRangeAxis(ValueAxis axis) {
        setRangeAxis(0, axis);
    }
    public void setDomainGridlinePosition(CategoryAnchor position) {
        if (position == null) {
            throw new IllegalArgumentException("Null 'position' argument.");
        }
        this.domainGridlinePosition = position;
        notifyListeners(new PlotChangeEvent(this));
    }
    public void configureRangeAxes() {
        for (int i = 0; i < this.rangeAxes.size(); i++) {
            ValueAxis axis = (ValueAxis) this.rangeAxes.get(i);
            if (axis != null) {
                axis.configure();
            }
        }
    }
    public boolean isRangeZoomable() {
        return true;
    }
    public Collection getDomainMarkers(Layer layer) {
        return getDomainMarkers(0, layer);
    }
    public void setRenderer(int index, CategoryItemRenderer renderer,
                            boolean notify) {

        // stop listening to the existing renderer...
        CategoryItemRenderer existing
            = (CategoryItemRenderer) this.renderers.get(index);
        if (existing != null) {
            existing.removeChangeListener(this);
        }

        // register the new renderer...
        this.renderers.set(index, renderer);
        if (renderer != null) {
            renderer.setPlot(this);
            renderer.addChangeListener(this);
        }

        configureDomainAxes();
        configureRangeAxes();

        if (notify) {
            notifyListeners(new PlotChangeEvent(this));
        }
    }
    public boolean equals(Object obj) {

        if (obj == this) {
            return true;
        }
        if (!(obj instanceof CategoryPlot)) {
            return false;
        }
        if (!super.equals(obj)) {
            return false;
        }

        CategoryPlot that = (CategoryPlot) obj;

        if (this.orientation != that.orientation) {
            return false;
        }
        if (!ObjectUtilities.equal(this.axisOffset, that.axisOffset)) {
            return false;
        }
        if (!this.domainAxes.equals(that.domainAxes)) {
            return false;
        }
        if (!this.domainAxisLocations.equals(that.domainAxisLocations)) {
            return false;
        }
        if (this.drawSharedDomainAxis != that.drawSharedDomainAxis) {
            return false;
        }
        if (!this.rangeAxes.equals(that.rangeAxes)) {
            return false;
        }
        if (!this.rangeAxisLocations.equals(that.rangeAxisLocations)) {
            return false;
        }
        if (!ObjectUtilities.equal(this.datasetToDomainAxisMap,
                that.datasetToDomainAxisMap)) {
            return false;
        }
        if (!ObjectUtilities.equal(this.datasetToRangeAxisMap,
                that.datasetToRangeAxisMap)) {
            return false;
        }
        if (!ObjectUtilities.equal(this.renderers, that.renderers)) {
            return false;
        }
        if (this.renderingOrder != that.renderingOrder) {
            return false;
        }
        if (this.columnRenderingOrder != that.columnRenderingOrder) {
            return false;
        }
        if (this.rowRenderingOrder != that.rowRenderingOrder) {
            return false;
        }
        if (this.domainGridlinesVisible != that.domainGridlinesVisible) {
            return false;
        }
        if (this.domainGridlinePosition != that.domainGridlinePosition) {
            return false;
        }
        if (!ObjectUtilities.equal(this.domainGridlineStroke,
                that.domainGridlineStroke)) {
            return false;
        }
        if (!PaintUtilities.equal(this.domainGridlinePaint,
                that.domainGridlinePaint)) {
            return false;
        }
        if (this.rangeGridlinesVisible != that.rangeGridlinesVisible) {
            return false;
        }
        if (!ObjectUtilities.equal(this.rangeGridlineStroke,
                that.rangeGridlineStroke)) {
            return false;
        }
        if (!PaintUtilities.equal(this.rangeGridlinePaint,
                that.rangeGridlinePaint)) {
            return false;
        }
        if (this.anchorValue != that.anchorValue) {
            return false;
        }
        if (this.rangeCrosshairVisible != that.rangeCrosshairVisible) {
            return false;
        }
        if (this.rangeCrosshairValue != that.rangeCrosshairValue) {
            return false;
        }
        if (!ObjectUtilities.equal(this.rangeCrosshairStroke,
                that.rangeCrosshairStroke)) {
            return false;
        }
        if (!PaintUtilities.equal(this.rangeCrosshairPaint,
                that.rangeCrosshairPaint)) {
            return false;
        }
        if (this.rangeCrosshairLockedOnData
                != that.rangeCrosshairLockedOnData) {
            return false;
        }
        if (!ObjectUtilities.equal(this.foregroundRangeMarkers,
                that.foregroundRangeMarkers)) {
            return false;
        }
        if (!ObjectUtilities.equal(this.backgroundRangeMarkers,
                that.backgroundRangeMarkers)) {
            return false;
        }
        if (!ObjectUtilities.equal(this.annotations, that.annotations)) {
            return false;
        }
        if (this.weight != that.weight) {
            return false;
        }
        if (!ObjectUtilities.equal(this.fixedDomainAxisSpace,
                that.fixedDomainAxisSpace)) {
            return false;
        }
        if (!ObjectUtilities.equal(this.fixedRangeAxisSpace,
                that.fixedRangeAxisSpace)) {
            return false;
        }

        return true;

    }
    public Stroke getRangeGridlineStroke() {
        return this.rangeGridlineStroke;
    }
    public Paint getRangeCrosshairPaint() {
        return this.rangeCrosshairPaint;
    }
    public CategoryPlot(CategoryDataset dataset,
                        CategoryAxis domainAxis,
                        ValueAxis rangeAxis,
                        CategoryItemRenderer renderer) {

        super();

        this.orientation = PlotOrientation.VERTICAL;

        // allocate storage for dataset, axes and renderers
        this.domainAxes = new ObjectList();
        this.domainAxisLocations = new ObjectList();
        this.rangeAxes = new ObjectList();
        this.rangeAxisLocations = new ObjectList();

        this.datasetToDomainAxisMap = new ObjectList();
        this.datasetToRangeAxisMap = new ObjectList();

        this.renderers = new ObjectList();

        this.datasets = new ObjectList();
        this.datasets.set(0, dataset);
        if (dataset != null) {
            dataset.addChangeListener(this);
        }

        this.axisOffset = new RectangleInsets(4.0, 4.0, 4.0, 4.0);

        setDomainAxisLocation(AxisLocation.BOTTOM_OR_LEFT, false);
        setRangeAxisLocation(AxisLocation.TOP_OR_LEFT, false);

        this.renderers.set(0, renderer);
        if (renderer != null) {
            renderer.setPlot(this);
            renderer.addChangeListener(this);
        }

        this.domainAxes.set(0, domainAxis);
        this.mapDatasetToDomainAxis(0, 0);
        if (domainAxis != null) {
            domainAxis.setPlot(this);
            domainAxis.addChangeListener(this);
        }
        this.drawSharedDomainAxis = false;

        this.rangeAxes.set(0, rangeAxis);
        this.mapDatasetToRangeAxis(0, 0);
        if (rangeAxis != null) {
            rangeAxis.setPlot(this);
            rangeAxis.addChangeListener(this);
        }

        configureDomainAxes();
        configureRangeAxes();

        this.domainGridlinesVisible = DEFAULT_DOMAIN_GRIDLINES_VISIBLE;
        this.domainGridlinePosition = CategoryAnchor.MIDDLE;
        this.domainGridlineStroke = DEFAULT_GRIDLINE_STROKE;
        this.domainGridlinePaint = DEFAULT_GRIDLINE_PAINT;

        this.rangeGridlinesVisible = DEFAULT_RANGE_GRIDLINES_VISIBLE;
        this.rangeGridlineStroke = DEFAULT_GRIDLINE_STROKE;
        this.rangeGridlinePaint = DEFAULT_GRIDLINE_PAINT;

        this.foregroundDomainMarkers = new HashMap();
        this.backgroundDomainMarkers = new HashMap();
        this.foregroundRangeMarkers = new HashMap();
        this.backgroundRangeMarkers = new HashMap();

        Marker baseline = new ValueMarker(0.0, new Color(0.8f, 0.8f, 0.8f,
                0.5f), new BasicStroke(1.0f), new Color(0.85f, 0.85f, 0.95f,
                0.5f), new BasicStroke(1.0f), 0.6f);
        addRangeMarker(baseline, Layer.BACKGROUND);

        this.anchorValue = 0.0;

        this.rangeCrosshairVisible = DEFAULT_CROSSHAIR_VISIBLE;
        this.rangeCrosshairValue = 0.0;
        this.rangeCrosshairStroke = DEFAULT_CROSSHAIR_STROKE;
        this.rangeCrosshairPaint = DEFAULT_CROSSHAIR_PAINT;

        this.annotations = new java.util.ArrayList();

    }
    public void setDatasetRenderingOrder(DatasetRenderingOrder order) {
        if (order == null) {
            throw new IllegalArgumentException("Null 'order' argument.");
        }
        this.renderingOrder = order;
        notifyListeners(new PlotChangeEvent(this));
    }
    public Paint getRangeGridlinePaint() {
        return this.rangeGridlinePaint;
    }
    public void clearAnnotations() {
        this.annotations.clear();
        notifyListeners(new PlotChangeEvent(this));
    }
    public void zoomDomainAxes(double factor, PlotRenderingInfo info,
                               Point2D source, boolean useAnchor) {
        // can't zoom domain axis
    }
    public void setRangeAxis(int index, ValueAxis axis) {
        setRangeAxis(index, axis, true);
    }
    public int getDomainAxisIndex(CategoryAxis axis) {
if (axis == null){throw new IllegalArgumentException();}        return this.domainAxes.indexOf(axis);
    }
    public Collection getRangeMarkers(int index, Layer layer) {
        Collection result = null;
        Integer key = new Integer(index);
        if (layer == Layer.FOREGROUND) {
            result = (Collection) this.foregroundRangeMarkers.get(key);
        }
        else if (layer == Layer.BACKGROUND) {
            result = (Collection) this.backgroundRangeMarkers.get(key);
        }
        if (result != null) {
            result = Collections.unmodifiableCollection(result);
        }
        return result;
    }
    public Stroke getDomainGridlineStroke() {
        return this.domainGridlineStroke;
    }
    public void setDomainAxisLocation(AxisLocation location) {
        // delegate...
        setDomainAxisLocation(0, location, true);
    }
    public Paint getDomainGridlinePaint() {
        return this.domainGridlinePaint;
    }
    public Stroke getRangeCrosshairStroke() {
        return this.rangeCrosshairStroke;
    }
    public void zoomRangeAxes(double factor, PlotRenderingInfo state,
                              Point2D source) {
        // delegate to other method
        zoomRangeAxes(factor, state, source, false);
    }
