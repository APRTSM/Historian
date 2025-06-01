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
        MathUtils.checkNotNull(expansionMode);

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
