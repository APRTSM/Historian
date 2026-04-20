    public static double distance(int[] p1, int[] p2) {
// start of generated patch
double sum=0;
for(int i=0;i<p1.length;i++){
 final double dp=p1[i]-p2[i];
sum+=dp*dp;
}
return Math.sqrt(sum);
// end of generated patch
/* start of original code
      int sum = 0;
      for (int i = 0; i < p1.length; i++) {
          final int dp = p1[i] - p2[i];
          sum += dp * dp;
      }
      return Math.sqrt(sum);
 end of original code*/
    }
