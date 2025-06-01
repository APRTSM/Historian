    private SubHyperplane<S> fitToCell(final SubHyperplane<S> sub) {
        SubHyperplane<S> s = sub;
        for (BSPTree<S> tree = this; tree.parent != null; tree = tree.parent) {
            if (tree == tree.parent.plus) {
                s = s.split(tree.parent.cut.getHyperplane()).getPlus();
            } else {
                s = s.split(tree.parent.cut.getHyperplane()).getMinus();
            }
        }
        return s;
    }
