    public Object computeValue(org.apache.commons.jxpath.ri.EvalContext context) {
        double l = org.apache.commons.jxpath.ri.InfoSetUtil.doubleValue(args[0].computeValue(context));
        double r = org.apache.commons.jxpath.ri.InfoSetUtil.doubleValue(args[1].computeValue(context));
        return l > r ? Boolean.TRUE : Boolean.FALSE;
    }
    public Object computeValue(org.apache.commons.jxpath.ri.EvalContext context) {
        double l = org.apache.commons.jxpath.ri.InfoSetUtil.doubleValue(args[0].computeValue(context));
        double r = org.apache.commons.jxpath.ri.InfoSetUtil.doubleValue(args[1].computeValue(context));
        return l >= r ? Boolean.TRUE : Boolean.FALSE;
    }
    public Object computeValue(org.apache.commons.jxpath.ri.EvalContext context) {
        double l = org.apache.commons.jxpath.ri.InfoSetUtil.doubleValue(args[0].computeValue(context));
        double r = org.apache.commons.jxpath.ri.InfoSetUtil.doubleValue(args[1].computeValue(context));
        return l < r ? Boolean.TRUE : Boolean.FALSE;
    }
    public Object computeValue(org.apache.commons.jxpath.ri.EvalContext context) {
        double l = org.apache.commons.jxpath.ri.InfoSetUtil.doubleValue(args[0].computeValue(context));
        double r = org.apache.commons.jxpath.ri.InfoSetUtil.doubleValue(args[1].computeValue(context));
        return l <= r ? Boolean.TRUE : Boolean.FALSE;
    }
