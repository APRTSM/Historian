    public boolean equals(Object obj) {

        if (obj == this) {
            return true;
        }
        if (!(obj instanceof ShapeList)) {
            return false;
        }
        ShapeList that = (ShapeList) obj;
int listSize = size();
int i = 0;while (i < listSize) {
if (!ShapeUtilities.equal(getShape(i), that.getShape(i))) {
return false;
}
else {
i++;}

}
return true;


    }
