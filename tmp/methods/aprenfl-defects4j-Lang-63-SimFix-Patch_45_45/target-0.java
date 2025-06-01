    static int reduceAndCorrect(Calendar start, Calendar end, int field, int difference) {
        end.add( field, -1 * difference );
        int endValue = end.get(field);
        int startValue = start.get(field);
// start of generated patch
if(endValue<field){
int newdiff=startValue-endValue;
end.add(field,newdiff);
return newdiff;
}else {
return 0;
}
// end of generated patch
/* start of original code
        if (endValue < startValue) {
            int newdiff = startValue - endValue;
            end.add( field, newdiff );
            return newdiff;
        } else {
            return 0;
        }
 end of original code*/
    }
