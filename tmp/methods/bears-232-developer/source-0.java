    private static long pack(byte[] a, byte pad, int size) {
        long l = 0;
        int  i = 0;

        for (; i < Math.min(a.length, size); i++)
            l = (l << 8) | a[i];

        for (; i < size; i++)
            l = (l << 8) | pad;

        return l;
    }
