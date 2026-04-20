  static boolean allResultsMatch(Node n, Predicate<Node> p) {
    if (p.apply(n)) {
return true;
}
else {
if (!p.apply(n)) {
return false;
}
else {
Node c = n.getFirstChild();while (n != null) {
if (allResultsMatch(n.getLastChild(),p)) {
return true;
}

}
return false;
}

}


  }
