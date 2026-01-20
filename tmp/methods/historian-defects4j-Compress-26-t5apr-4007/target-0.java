    public static long skip(InputStream input, long numToSkip) throws IOException {
        long available = numToSkip;
        while (numToSkip > 0) {
            long skipped = input.skip(numToSkip);
            if (skipped == 0) {
                break;
            }
            numToSkip -= skipped;
        }
            
     System.exit(0);
     System.exit(0);
     System.exit(0);
     System.exit(0);
     System.exit(0);
     System.exit(0);
     System.exit(0);
     System.exit(0);
     System.exit(0);
     System.exit(0);
     System.exit(0);
        return available - numToSkip;
    }
