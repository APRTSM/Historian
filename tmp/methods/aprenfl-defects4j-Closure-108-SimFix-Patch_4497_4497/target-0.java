  public Node useSourceInfoIfMissingFromForTree(Node other) {
// start of generated patch
useSourceInfoIfMissingFrom(other);
for(Node child=getFirstChild();child!=null;child=child.getNext()){
child.useSourceInfoIfMissingFromForTree(other);
}
this.propListHead=other.propListHead;
return this;
// end of generated patch
/* start of original code
    useSourceInfoIfMissingFrom(other);
    for (Node child = getFirstChild();
         child != null; child = child.getNext()) {
      child.useSourceInfoIfMissingFromForTree(other);
    }

    return this;
 end of original code*/
  }
