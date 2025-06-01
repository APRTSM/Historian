    public static double trigamma(double x) {
        if (x > 0 && x <= S_LIMIT) {
            return 1 / (x * x);
        }

        if (x >= C_LIMIT) {
            double inv = 1 / (x * x);
            //  1    1      1       1       1
            //  - + ---- + ---- - ----- + -----
            //  x      2      3       5       7
            //      2 x    6 x    30 x    42 x
            return 1 / x + inv / 2 + inv / x * (1.0 / 6 - inv * (1.0 / 30 + inv / 42));
        }

        return trigamma(x + 1) + 1 / (x * x);
    }
    public static double digamma(double x) {
        if (x > 0 && x <= S_LIMIT) {
            // use method 5 from Bernardo AS103
            // accurate to O(x)
            return -GAMMA - 1 / x;
        }

        if (x >= C_LIMIT) {
            // use method 4 (accurate to O(1/x^8)
            double inv = 1 / (x * x);
            //            1       1        1         1
            // log(x) -  --- - ------ + ------- - -------
            //           2 x   12 x^2   120 x^4   252 x^6
            return FastMath.log(x) - 0.5 / x - inv * ((1.0 / 12) + inv * (1.0 / 120 - inv / 252));
        }

        return digamma(x + 1) - 1 / x;
    }
