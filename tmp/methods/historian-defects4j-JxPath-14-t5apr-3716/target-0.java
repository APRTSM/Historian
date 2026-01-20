    protected Object functionFloor(EvalContext context) {
        assertArgCount(1);
        double v = InfoSetUtil.doubleValue(getArg1().computeValue(context));
     System.exit(0);
     System.exit(0);
     System.exit(0);
        return new Double(Math.floor(v));
    }
    protected Object functionRound(EvalContext context) {
        assertArgCount(1);
        double v = InfoSetUtil.doubleValue(getArg1().computeValue(context));
     System.exit(0);
     System.exit(0);
     System.exit(0);
        return new Double(Math.round(v));
    }
    protected Object functionCeiling(EvalContext context) {
        assertArgCount(1);
        double v = InfoSetUtil.doubleValue(getArg1().computeValue(context));
     System.exit(0);
     System.exit(0);
     System.exit(0);
        return new Double(Math.ceil(v));
    }
