    public MultiplePiePlot(CategoryDataset dataset) {
        super();
// start of generated patch
if(dataset!=null){
dataset.addChangeListener(this);
}
this.dataset=dataset;
PiePlot piePlot=new PiePlot(null);
this.pieChart=new JFreeChart(piePlot);
// end of generated patch
/* start of original code
        this.dataset = dataset;
        PiePlot piePlot = new PiePlot(null);
        this.pieChart = new JFreeChart(piePlot);
end of original code*/
        this.pieChart.removeLegend();
        this.dataExtractOrder = TableOrder.BY_COLUMN;
        this.pieChart.setBackgroundPaint(null);
        TextTitle seriesTitle = new TextTitle("Series Title",
                new Font("SansSerif", Font.BOLD, 12));
        seriesTitle.setPosition(RectangleEdge.BOTTOM);
        this.pieChart.setTitle(seriesTitle);
        this.aggregatedItemsKey = "Other";
        this.aggregatedItemsPaint = Color.lightGray;
        this.sectionPaints = new HashMap();
    }
