  public FlowScope getPreciserScopeKnowingConditionOutcome(Node condition,
      FlowScope blindScope, boolean outcome) {
    if (condition.isCall() && condition.getChildCount() == 2) {
      Node callee = condition.getFirstChild();
      Node param = condition.getLastChild();
// start of generated patch
if(param.isName()||param.isGetProp()){
JSType paramType=getTypeIfRefinable(param,blindScope);
Node left=callee.getFirstChild();
Node right=callee.getLastChild();
if(left.isName()&&"goog".equals(left.getString())){
Function<TypeRestriction,JSType> restricter=restricters.get(right.getString());
if(restricter!=null){
return restrictParameter(param,paramType,blindScope,restricter,outcome);
}
}
}
// end of generated patch
/* start of original code
      if (callee.isGetProp() && param.isQualifiedName()) {
        JSType paramType =  getTypeIfRefinable(param, blindScope);
        Node left = callee.getFirstChild();
        Node right = callee.getLastChild();
        if (left.isName() && "goog".equals(left.getString()) &&
            right.isString()) {
          Function<TypeRestriction, JSType> restricter =
              restricters.get(right.getString());
          if (restricter != null) {
            return restrictParameter(param, paramType, blindScope, restricter,
                outcome);
          }
        }
      }
 end of original code*/
    }
    return nextPreciserScopeKnowingConditionOutcome(
        condition, blindScope, outcome);
  }
