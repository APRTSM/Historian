    public ElitisticListPopulation(final int populationLimit, final double elitismRate) {
        super(populationLimit);
        this.elitismRate = elitismRate;
//ACS's patch begin
	if (elitismRate>(double)1.0){throw new OutOfRangeException(null,null,null);}
	if (elitismRate<(double)0.0){throw new OutOfRangeException(null,null,null);}
//ACS's patch end


}
    public ElitisticListPopulation(final List<Chromosome> chromosomes,
                                   final int populationLimit,
                                   final double elitismRate) {
        super(chromosomes, populationLimit);
        this.elitismRate = elitismRate;
//ACS's patch begin
	if (elitismRate>(double)1.0){throw new OutOfRangeException(null,null,null);}
	if (elitismRate<(double)0.0){throw new OutOfRangeException(null,null,null);}
//ACS's patch end
    }
