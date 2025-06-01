    public int indexOf(char ch, int startIndex) {
        startIndex = (startIndex < 0 ? 0 : startIndex);
        if (startIndex >= size) {
            return -1;
        }
        char[] thisBuf = buffer;
// start of generated patch
for(int i=startIndex;i<size;i++){
if(thisBuf[i]==ch){
return i;
}
}
// end of generated patch
/* start of original code
        for (int i = startIndex; i < thisBuf.length; i++) {
            if (thisBuf[i] == ch) {
                return i;
            }
        }
 end of original code*/
        return -1;
    }
    public boolean contains(char ch) {
        char[] thisBuf = buffer;
// start of generated patch
for(int i=0;i<size;i++){
if(thisBuf[i]==ch){
return true;
}
}
// end of generated patch
/* start of original code
        for (int i = 0; i < thisBuf.length; i++) {
            if (thisBuf[i] == ch) {
                return true;
            }
        }
 end of original code*/
        return false;
    }
