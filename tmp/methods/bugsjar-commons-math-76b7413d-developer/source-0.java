    protected synchronized void expand() {
        // notice the use of FastMath.ceil(), this guarantees that we will always
        // have an array of at least currentSize + 1.   Assume that the
        // current initial capacity is 1 and the expansion factor
        // is 1.000000000000000001.  The newly calculated size will be
        // rounded up to 2 after the multiplication is performed.
        int newSize = 0;
        if (expansionMode == ExpansionMode.MULTIPLICATIVE) {
            newSize = (int) FastMath.ceil(internalArray.length * expansionFactor);
        } else {
            newSize = (int) (internalArray.length + FastMath.round(expansionFactor));
        }
        final double[] tempArray = new double[newSize];

        // Copy and swap
        System.arraycopy(internalArray, 0, tempArray, 0, internalArray.length);
        internalArray = tempArray;
    }
    public boolean equals(Object object) {
        if (object == this ) {
            return true;
        }
        if (object instanceof ResizableDoubleArray == false) {
            return false;
        }
        synchronized(this) {
            synchronized(object) {
                boolean result = true;
                final ResizableDoubleArray other = (ResizableDoubleArray) object;
                result = result && (other.contractionCriterion == contractionCriterion);
                result = result && (other.expansionFactor == expansionFactor);
                result = result && (other.expansionMode == expansionMode);
                result = result && (other.numElements == numElements);
                result = result && (other.startIndex == startIndex);
                if (!result) {
                    return false;
                } else {
                    return Arrays.equals(internalArray, other.internalArray);
                }
            }
        }
    }
    public float getContractionCriteria() {
        return (float) getContractionCriterion();
    }
    public synchronized int start() {
        return startIndex;
    }
    public synchronized void clear() {
        numElements = 0;
        startIndex = 0;
    }
    public synchronized void setElement(int index, double value) {
        if (index < 0) {
            throw new ArrayIndexOutOfBoundsException(index);
        }
        if (index + 1 > numElements) {
            numElements = index + 1;
        }
        if ((startIndex + index) >= internalArray.length) {
            expandTo(startIndex + (index + 1));
        }
        internalArray[startIndex + index] = value;
    }
    protected void setInitialCapacity(int initialCapacity)
        throws MathIllegalArgumentException {
        // Body removed in 3.1.
    }
    public synchronized void setNumElements(int i)
        throws MathIllegalArgumentException {
        // If index is negative thrown an error.
        if (i < 0) {
            throw new MathIllegalArgumentException(
                    LocalizedFormats.INDEX_NOT_POSITIVE,
                    i);
        }

        // Test the new num elements, check to see if the array needs to be
        // expanded to accommodate this new number of elements.
        final int newSize = startIndex + i;
        if (newSize > internalArray.length) {
            expandTo(newSize);
        }

        // Set the new number of elements to new value.
        numElements = i;
    }
    protected void checkContractExpand(double contraction,
                                       double expansion)
        throws NumberIsTooSmallException {
        if (contraction < expansion) {
            final NumberIsTooSmallException e = new NumberIsTooSmallException(contraction, 1, true);
            e.getContext().addMessage(LocalizedFormats.CONTRACTION_CRITERIA_SMALLER_THAN_EXPANSION_FACTOR,
                                      contraction, expansion);
            throw e;
        }

        if (contraction <= 1) {
            final NumberIsTooSmallException e = new NumberIsTooSmallException(contraction, 1, false);
            e.getContext().addMessage(LocalizedFormats.CONTRACTION_CRITERIA_SMALLER_THAN_ONE,
                                      contraction);
            throw e;
        }

        if (expansion <= 1) {
            final NumberIsTooSmallException e = new NumberIsTooSmallException(contraction, 1, false);
            e.getContext().addMessage(LocalizedFormats.EXPANSION_FACTOR_SMALLER_THAN_ONE,
                                      expansion);
            throw e;
        }
    }
    public void setContractionCriteria(float contractionCriteria)
        throws MathIllegalArgumentException {
        checkContractExpand(contractionCriteria, getExpansionFactor());
        synchronized(this) {
            this.contractionCriterion = contractionCriteria;
        }
    }
    synchronized int getInternalLength() {
        return internalArray.length;
    }
    public synchronized void contract() {
        final double[] tempArray = new double[numElements + 1];

        // Copy and swap - copy only the element array from the src array.
        System.arraycopy(internalArray, startIndex, tempArray, 0, numElements);
        internalArray = tempArray;

        // Reset the start index to zero
        startIndex = 0;
    }
    public void setExpansionMode(int expansionMode)
        throws MathIllegalArgumentException {
        if (expansionMode != MULTIPLICATIVE_MODE &&
            expansionMode != ADDITIVE_MODE) {
            throw new MathIllegalArgumentException(LocalizedFormats.UNSUPPORTED_EXPANSION_MODE, expansionMode,
                                                   MULTIPLICATIVE_MODE, "MULTIPLICATIVE_MODE",
                                                   ADDITIVE_MODE, "ADDITIVE_MODE");
        }
        synchronized(this) {
            if (expansionMode == MULTIPLICATIVE_MODE) {
                setExpansionMode(ExpansionMode.MULTIPLICATIVE);
            } else if (expansionMode == ADDITIVE_MODE) {
                setExpansionMode(ExpansionMode.ADDITIVE);
            }
        }
    }
    public double compute(MathArrays.Function f) {
        final double[] array;
        final int start;
        final int num;
        synchronized(this) {
            array = internalArray;
            start = startIndex;
            num   = numElements;
        }
        return f.evaluate(array, start, num);
    }
    public int getExpansionMode() {
        switch (expansionMode) {
        case MULTIPLICATIVE:
            return MULTIPLICATIVE_MODE;
        case ADDITIVE:
            return ADDITIVE_MODE;
        default:
            throw new MathInternalError(); // Should never happen.
        }
    }
    private synchronized void expandTo(int size) {
        final double[] tempArray = new double[size];
        // Copy and swap
        System.arraycopy(internalArray, 0, tempArray, 0, internalArray.length);
        internalArray = tempArray;
    }
    public ResizableDoubleArray(int initialCapacity,
                                double expansionFactor,
                                double contractionCriterion,
                                ExpansionMode expansionMode,
                                double ... data)
        throws MathIllegalArgumentException {
        if (initialCapacity <= 0) {
            throw new NotStrictlyPositiveException(LocalizedFormats.INITIAL_CAPACITY_NOT_POSITIVE,
                                                   initialCapacity);
        }
        checkContractExpand(contractionCriterion, expansionFactor);

        this.expansionFactor = expansionFactor;
        this.contractionCriterion = contractionCriterion;
        this.expansionMode = expansionMode;
        internalArray = new double[initialCapacity];
        numElements = 0;
        startIndex = 0;

        if (data != null && data.length > 1) {
            addElements(data);
        }
    }
    public synchronized double[] getInternalValues() {
        return internalArray;
    }
    public ResizableDoubleArray(int initialCapacity, float expansionFactor,
            float contractionCriteria, int expansionMode) throws MathIllegalArgumentException {
        this(initialCapacity,
             expansionFactor,
             contractionCriteria,
             expansionMode == ADDITIVE_MODE ?
             ExpansionMode.ADDITIVE :
             ExpansionMode.MULTIPLICATIVE,
             null);
        // XXX Just ot retain the expected failure in a unit test.
        // With the new "enum", that test will become obsolete.
        setExpansionMode(expansionMode);
    }
    public ResizableDoubleArray(int initialCapacity)
        throws MathIllegalArgumentException {
        this(initialCapacity, DEFAULT_EXPANSION_FACTOR);
    }
    public synchronized void addElements(double[] values) {
        final double[] tempArray = new double[numElements + values.length + 1];
        System.arraycopy(internalArray, startIndex, tempArray, 0, numElements);
        System.arraycopy(values, 0, tempArray, numElements, values.length);
        internalArray = tempArray;
        startIndex = 0;
        numElements += values.length;
    }
    public synchronized double substituteMostRecentElement(double value)
        throws MathIllegalStateException {
        if (numElements < 1) {
            throw new MathIllegalStateException(
                    LocalizedFormats.CANNOT_SUBSTITUTE_ELEMENT_FROM_EMPTY_ARRAY);
        }

        final int substIndex = startIndex + (numElements - 1);
        final double discarded = internalArray[substIndex];

        internalArray[substIndex] = value;

        return discarded;
    }
    public synchronized void discardMostRecentElements(int i)
        throws MathIllegalArgumentException {
        discardExtremeElements(i,false);
    }
    protected void checkContractExpand(float contraction, float expansion)
        throws MathIllegalArgumentException {
        checkContractExpand((double) contraction,
                            (double) expansion);
    }
    public synchronized int getNumElements() {
        return numElements;
    }
    public void setExpansionFactor(float expansionFactor) throws MathIllegalArgumentException {
        checkContractExpand(getContractionCriterion(), expansionFactor);
        // The check above verifies that the expansion factor is > 1.0;
        synchronized(this) {
            this.expansionFactor = expansionFactor;
        }
    }
    private synchronized boolean shouldContract() {
        if (expansionMode == ExpansionMode.MULTIPLICATIVE) {
            return (internalArray.length / ((float) numElements)) > contractionCriterion;
        } else {
            return (internalArray.length - numElements) > contractionCriterion;
        }
    }
    public ResizableDoubleArray(double[] initialArray) {
        this(DEFAULT_INITIAL_CAPACITY,
             DEFAULT_EXPANSION_FACTOR,
             DEFAULT_CONTRACTION_DELTA + DEFAULT_EXPANSION_FACTOR,
             ExpansionMode.MULTIPLICATIVE,
             initialArray);
    }
    public ResizableDoubleArray(int initialCapacity,
                                double expansionFactor,
                                double contractionCriterion)
        throws MathIllegalArgumentException {
        this(initialCapacity,
             expansionFactor,
             contractionCriterion,
             ExpansionMode.MULTIPLICATIVE,
             null);
    }
    public void setExpansionMode(ExpansionMode expansionMode) {
        this.expansionMode = expansionMode;
    }
    public synchronized int hashCode() {
        final int[] hashData = new int[6];
        hashData[0] = Double.valueOf(expansionFactor).hashCode();
        hashData[1] = Double.valueOf(contractionCriterion).hashCode();
        hashData[2] = expansionMode.hashCode();
        hashData[3] = Arrays.hashCode(internalArray);
        hashData[4] = numElements;
        hashData[5] = startIndex;
        return Arrays.hashCode(hashData);
    }
    public ResizableDoubleArray(int initialCapacity,
                                double expansionFactor)
        throws MathIllegalArgumentException {
        this(initialCapacity,
             expansionFactor,
             DEFAULT_CONTRACTION_DELTA + expansionFactor);
    }
    public ResizableDoubleArray(ResizableDoubleArray original)
        throws NullArgumentException {
        MathUtils.checkNotNull(original);
        copy(original, this);
    }
    public synchronized double getElement(int index) {
        if (index >= numElements) {
            throw new ArrayIndexOutOfBoundsException(index);
        } else if (index >= 0) {
            return internalArray[startIndex + index];
        } else {
            throw new ArrayIndexOutOfBoundsException(index);
        }
    }
    public synchronized double[] getElements() {
        final double[] elementArray = new double[numElements];
        System.arraycopy(internalArray, startIndex, elementArray, 0, numElements);
        return elementArray;
    }
    public ResizableDoubleArray(int initialCapacity,
                                float expansionFactor)
        throws MathIllegalArgumentException {
        this(initialCapacity,
             (double) expansionFactor);
    }
    public static void copy(ResizableDoubleArray source,
                            ResizableDoubleArray dest)
        throws NullArgumentException {
        MathUtils.checkNotNull(source);
        MathUtils.checkNotNull(dest);
        synchronized(source) {
           synchronized(dest) {
               dest.contractionCriterion = source.contractionCriterion;
               dest.expansionFactor = source.expansionFactor;
               dest.expansionMode = source.expansionMode;
               dest.internalArray = new double[source.internalArray.length];
               System.arraycopy(source.internalArray, 0, dest.internalArray,
                       0, dest.internalArray.length);
               dest.numElements = source.numElements;
               dest.startIndex = source.startIndex;
           }
       }
    }
    public synchronized ResizableDoubleArray copy() {
        final ResizableDoubleArray result = new ResizableDoubleArray();
        copy(this, result);
        return result;
    }
    private synchronized void discardExtremeElements(int i,
                                                     boolean front)
        throws MathIllegalArgumentException {
        if (i > numElements) {
            throw new MathIllegalArgumentException(
                    LocalizedFormats.TOO_MANY_ELEMENTS_TO_DISCARD_FROM_ARRAY,
                    i, numElements);
       } else if (i < 0) {
           throw new MathIllegalArgumentException(
                   LocalizedFormats.CANNOT_DISCARD_NEGATIVE_NUMBER_OF_ELEMENTS,
                   i);
        } else {
            // "Subtract" this number of discarded from numElements
            numElements -= i;
            if (front) {
                startIndex += i;
            }
        }
        if (shouldContract()) {
            contract();
        }
    }
    public synchronized double addElementRolling(double value) {
        double discarded = internalArray[startIndex];

        if ((startIndex + (numElements + 1)) > internalArray.length) {
            expand();
        }
        // Increment the start index
        startIndex += 1;

        // Add the new value
        internalArray[startIndex + (numElements - 1)] = value;

        // Check the contraction criterion.
        if (shouldContract()) {
            contract();
        }
        return discarded;
    }
    public float getExpansionFactor() {
        return (float) expansionFactor;
    }
    public synchronized void discardFrontElements(int i)
        throws MathIllegalArgumentException {
        discardExtremeElements(i,true);
    }
    public ResizableDoubleArray(int initialCapacity,
                                float expansionFactor,
                                float contractionCriteria)
        throws MathIllegalArgumentException {
        this(initialCapacity,
             (double) expansionFactor,
             (double) contractionCriteria);
    }
    public synchronized void addElement(double value) {
        if (internalArray.length <= startIndex + numElements) {
            expand();
        }
        internalArray[startIndex + numElements++] = value;
    }
