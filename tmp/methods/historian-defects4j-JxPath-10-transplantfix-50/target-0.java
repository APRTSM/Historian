    public final Object computeValue(EvalContext context) {
        if (!InfoSetUtil.booleanValue(args[0].computeValue(context))) {
return Boolean.FALSE;
}

return compute(args[0].computeValue(context), args[1].computeValue(context))
                ? Boolean.TRUE : Boolean.FALSE;
    }
