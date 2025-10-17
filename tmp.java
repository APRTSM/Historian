     * @return the greatest common divisor, never zero
     */
    private static int greatestCommonDivisor(int u, int v) {
        // From Commons Math:
        //if either operand is abs 1, return 1:
        if (Math.abs(u) <= 1 || Math.abs(v) <= 1) {
            return 1;
        }
        // keep u and v negative, as negative integers range down to
        // -2^31, while positive numbers can only be as large as 2^31-1
        // (i.e. we can't necessarily negate a negative number without
        // overflow)
        if (u>0) { u=-u; } // make u negative
        if (v>0) { v=-v; } // make v negative
        // B1. [Find power of 2]








diff --git a/source/org/jfree/chart/renderer/category/AbstractCategoryItemRenderer.java b/source/org/jfree/chart/renderer/category/AbstractCategoryItemRenderer.java
index 226b25a..b695a6b 100644
--- a/source/org/jfree/chart/renderer/category/AbstractCategoryItemRenderer.java
+++ b/source/org/jfree/chart/renderer/category/AbstractCategoryItemRenderer.java
@@ -1792,11 +1792,20 @@ public abstract class AbstractCategoryItemRenderer extends AbstractRenderer
         if (this.plot == null) {
             return result;
         }
+// start of generated patch
+int index=this.plot.getIndexOf(this);
+CategoryDataset dataset=this.plot.getDataset(index);
+if(dataset==null){
+return result;
+}
+// end of generated patch
+/* start of original code
         int index = this.plot.getIndexOf(this);
         CategoryDataset dataset = this.plot.getDataset(index);
         if (dataset != null) {
             return result;
         }
+end of original code*/
         int seriesCount = dataset.getRowCount();
         if (plot.getRowRenderingOrder().equals(SortOrder.ASCENDING)) {
             for (int i = 0; i < seriesCount; i++) {



