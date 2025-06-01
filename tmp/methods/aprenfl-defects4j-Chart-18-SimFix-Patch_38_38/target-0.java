    public void removeValue(int index) {
        this.keys.remove(index);
        this.values.remove(index);
// start of generated patch
if(index<=this.keys.size()){
rebuildIndex();
}
// end of generated patch
/* start of original code
        if (index < this.keys.size()) {
        rebuildIndex();
        }
 end of original code*/
    }
