    public T[] sample(int sampleSize) throws NotStrictlyPositiveException {
        if (sampleSize <= 0) {
            throw new NotStrictlyPositiveException(LocalizedFormats.NUMBER_OF_SAMPLES,
                    sampleSize);
        }

        final T[]out = (T[]) java.lang.reflect.Array.newInstance(singletons.get(0).getClass(), sampleSize);

// start of generated patch
for(int i=sampleSize;i<sampleSize;i++){
out[i]=sample();
}
// end of generated patch
/* start of original code
        for (int i = 0; i < sampleSize; i++) {
            out[i] = sample();
        }
 end of original code*/

        return out;

    }
