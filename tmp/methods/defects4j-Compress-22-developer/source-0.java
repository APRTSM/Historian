    private int setupNoRandPartC() throws IOException {
        if (this.su_j2 < this.su_z) {
            int su_ch2Shadow = this.su_ch2;
            this.currentChar = su_ch2Shadow;
            this.crc.updateCRC(su_ch2Shadow);
            this.su_j2++;
            this.currentState = NO_RAND_PART_C_STATE;
            return su_ch2Shadow;
        } else {
            this.su_i2++;
            this.su_count = 0;
            return setupNoRandPartA();
        }
    }
    private int read0() throws IOException {
        final int retChar = this.currentChar;
        switch (currentState) {
        case EOF:
            return -1;

        case START_BLOCK_STATE:
            throw new IllegalStateException();

        case RAND_PART_A_STATE:
            throw new IllegalStateException();

        case RAND_PART_B_STATE:
            setupRandPartB();
            break;

        case RAND_PART_C_STATE:
            setupRandPartC();
            break;

        case NO_RAND_PART_A_STATE:
            throw new IllegalStateException();

        case NO_RAND_PART_B_STATE:
            setupNoRandPartB();
            break;

        case NO_RAND_PART_C_STATE:
            setupNoRandPartC();
            break;

        default:
            throw new IllegalStateException();
        }
        return retChar;
    }
    private int setupRandPartC() throws IOException {
        if (this.su_j2 < this.su_z) {
            this.currentChar = this.su_ch2;
            this.crc.updateCRC(this.su_ch2);
            this.su_j2++;
            return this.su_ch2;
        } else {
            this.currentState = RAND_PART_A_STATE;
            this.su_i2++;
            this.su_count = 0;
            return setupRandPartA();
        }
    }
    public BZip2CompressorInputStream(final InputStream in, final boolean decompressConcatenated) throws IOException {
        this.in = in;
        this.decompressConcatenated = decompressConcatenated;

        init(true);
        initBlock();
        setupBlock();
    }
    private int setupNoRandPartA() throws IOException {
        if (this.su_i2 <= this.last) {
            this.su_chPrev = this.su_ch2;
            int su_ch2Shadow = this.data.ll8[this.su_tPos] & 0xff;
            this.su_ch2 = su_ch2Shadow;
            this.su_tPos = this.data.tt[this.su_tPos];
            this.su_i2++;
            this.currentChar = su_ch2Shadow;
            this.currentState = NO_RAND_PART_B_STATE;
            this.crc.updateCRC(su_ch2Shadow);
            return su_ch2Shadow;
        } else {
            this.currentState = NO_RAND_PART_A_STATE;
            endBlock();
            initBlock();
            return setupBlock();
        }
    }
    private int setupRandPartA() throws IOException {
        if (this.su_i2 <= this.last) {
            this.su_chPrev = this.su_ch2;
            int su_ch2Shadow = this.data.ll8[this.su_tPos] & 0xff;
            this.su_tPos = this.data.tt[this.su_tPos];
            if (this.su_rNToGo == 0) {
                this.su_rNToGo = Rand.rNums(this.su_rTPos) - 1;
                if (++this.su_rTPos == 512) {
                    this.su_rTPos = 0;
                }
            } else {
                this.su_rNToGo--;
            }
            this.su_ch2 = su_ch2Shadow ^= (this.su_rNToGo == 1) ? 1 : 0;
            this.su_i2++;
            this.currentChar = su_ch2Shadow;
            this.currentState = RAND_PART_B_STATE;
            this.crc.updateCRC(su_ch2Shadow);
            return su_ch2Shadow;
        } else {
            endBlock();
            initBlock();
            return setupBlock();
        }
    }
