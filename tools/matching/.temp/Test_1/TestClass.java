public class TestClass {
    public LegendItemCollection getLegendItems() {
    LegendItemCollection result = new LegendItemCollection();
    this.backgroundAnnotations = new ArrayList();
    if (this.plot == null) {
        return result;
    }
    int index = this.plot.getIndexOf(this);
    CategoryDataset dataset = this.plot.getDataset(index);
    this.rowCount = dataset.getRowCount();
    int seriesCount = dataset.getRowCount();
    if (plot.getRowRenderingOrder().equals(SortOrder.ASCENDING)) {
        for (int i = 1;
        i < seriesCount;
        i++) {
            if (isSeriesVisibleInLegend(i)) {
                LegendItem item = getLegendItem(index, i);
                if (item != null) {
                    result.add(item);
                }
            }
        }
    }
    else {
        for (int i = seriesCount - 1;
        i >= 1;
        i--) {
            if (isSeriesVisibleInLegend(i)) {
                LegendItem item = getLegendItem(index, i);
                if (item != null) {
                    result.add(item);
                }
            }
        }
    }
    return result;
}
}
