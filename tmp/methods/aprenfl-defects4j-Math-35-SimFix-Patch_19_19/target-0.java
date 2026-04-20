    public ElitisticListPopulation(final List<Chromosome> chromosomes,
                                   final int populationLimit,
                                   final double elitismRate) {
// start of generated patch
super(chromosomes,populationLimit);
if(elitismRate<0||elitismRate>1){
throw new OutOfRangeException(LocalizedFormats.ELITISM_RATE,elitismRate,0,1);
}
this.elitismRate=elitismRate;
// end of generated patch
/* start of original code
        super(chromosomes, populationLimit);
        this.elitismRate = elitismRate;
 end of original code*/
    }
    public ElitisticListPopulation(final int populationLimit, final double elitismRate) {
// start of generated patch
super(populationLimit);
if(elitismRate<0||elitismRate>1){
throw new OutOfRangeException(LocalizedFormats.ELITISM_RATE,elitismRate,0,1);
}
this.elitismRate=elitismRate;
// end of generated patch
/* start of original code
        super(populationLimit);
        this.elitismRate = elitismRate;
 end of original code*/
    }
