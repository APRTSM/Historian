  public String toString() {
    return String.format("%,d %,d %,.2f %,d", getMin(), getMax(), getAverage(), count);
  }
  public double getAverage() {
    return ((double) sum) / count;
  }
  public double getStdDev() {
    return Math.sqrt(partialStdDev / count - getAverage() * getAverage());
  }
  public void clear() {
    sum = 0;
    count = 0;
    partialStdDev = 0;
  }
  public long getMax() {
    return max;
  }
  public long getSum() {
    return sum;
  }
  public long getMin() {
    return min;
  }
  public void addStat(long stat) {
    if (stat > max)
      max = stat;
    if (stat < min)
      min = stat;
    
    sum += stat;
    
    partialStdDev += stat * stat;
    
    count++;
  }
