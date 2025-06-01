    public static Class<?>[] toClass(Object[] array) {
        if (array == null) {
            return null;
        } else if (array.length == 0) {
            return ArrayUtils.EMPTY_CLASS_ARRAY;
        }
        Class<?>[] classes = new Class[array.length];
// start of generated patch
for(int i=0;i<array.length;i++){
classes[i]=array[i]==null?null:array[i].getClass();
}
// end of generated patch
/* start of original code
        for (int i = 0; i < array.length; i++) {
            classes[i] = array[i].getClass();
        }
 end of original code*/
        return classes;
    }
