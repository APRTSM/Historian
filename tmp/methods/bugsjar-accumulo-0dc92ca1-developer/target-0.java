  public double getStdDev() {
    return sd.getResult();
  }
  public long getMin() {
    return (long) min.getResult();
  }
  public Stat() {
    min = new Min();
    max = new Max();
    sum = new Sum();
    mean = new Mean();
    sd = new StandardDeviation();

    stats = new StorelessUnivariateStatistic[] {min, max, sum, mean, sd};
  }
  public void clear() {
    for (StorelessUnivariateStatistic statistic : stats) {
      statistic.clear();
    }
  }
  public void addStat(long stat) {
    for (StorelessUnivariateStatistic statistic : stats) {
      statistic.increment(stat);
    }
  }
  public double getAverage() {
    return mean.getResult();
  }
  public long getMax() {
    return (long) max.getResult();
  }
  public String toString() {
    return String.format("%,d %,d %,.2f %,d", getMin(), getMax(), getAverage(), mean.getN());
  }
  public long getSum() {
    return (long) sum.getResult();
  }
