        private Split pow(final long e) {

            // prepare result
            Split result = new Split(1);

            // d^(2p)
            Split d2p = new Split(full, high, low);

            for (long p = e; p != 0; p >>= 1) {

                if ((p & 0x1) != 0) {
                    // accurate multiplication result = result * d^(2p) using Veltkamp TwoProduct algorithm
                    result = result.multiply(d2p);
                }

                // accurate squaring d^(2(p+1)) = d^(2p) * d^(2p) using Veltkamp TwoProduct algorithm
                d2p = d2p.multiply(d2p);

            }

            if (Double.isNaN(result.full)) {
                if (Double.isNaN(full)) {
                    return Split.NAN;
                } else {
                    // some intermediate numbers exceeded capacity,
                    // and the low order bits became NaN (because infinity - infinity = NaN)
                    if (FastMath.abs(full) < 1) {
                        return new Split(FastMath.copySign(0.0, full), 0.0);
                    } else if (full < 0 && (e & 0x1) == 1) {
                        return Split.NEGATIVE_INFINITY;
                    } else {
                        return Split.POSITIVE_INFINITY;
                    }
                }
            } else {
                return result;
            }

        }
