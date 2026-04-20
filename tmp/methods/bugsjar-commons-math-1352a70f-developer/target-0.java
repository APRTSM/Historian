    private void initState() {
        a = b = c = 0;
        for (i = 0; i < arr.length; i++) {
            arr[i] = GLD_RATIO;
        }
        for (i = 0; i < 4; i++) {
            shuffle();
        }
        // fill in mem[] with messy stuff
        for (i = 0; i < SIZE; i += 8) {
            arr[0] += rsl[i];
            arr[1] += rsl[i + 1];
            arr[2] += rsl[i + 2];
            arr[3] += rsl[i + 3];
            arr[4] += rsl[i + 4];
            arr[5] += rsl[i + 5];
            arr[6] += rsl[i + 6];
            arr[7] += rsl[i + 7];
            shuffle();
            setState();
        }
        // second pass makes all of seed affect all of mem
        for (i = 0; i < SIZE; i += 8) {
            arr[0] += mem[i];
            arr[1] += mem[i + 1];
            arr[2] += mem[i + 2];
            arr[3] += mem[i + 3];
            arr[4] += mem[i + 4];
            arr[5] += mem[i + 5];
            arr[6] += mem[i + 6];
            arr[7] += mem[i + 7];
            shuffle();
            setState();
        }
        isaac();
        count = SIZE - 1;
        clear();
    }
