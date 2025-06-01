    private double bobyqb(
            ArrayRealVector xbase,
            Array2DRowRealMatrix xpt,
            ArrayRealVector fval,
            ArrayRealVector xopt,
            ArrayRealVector gopt,
            ArrayRealVector hq,
            ArrayRealVector pq,
            Array2DRowRealMatrix bmat,
            Array2DRowRealMatrix zmat,
            ArrayRealVector sl,
            ArrayRealVector su,
            ArrayRealVector xnew,
            ArrayRealVector xalt,
            ArrayRealVector d__,
            ArrayRealVector vlag
    ) {
        // System.out.println("bobyqb"); // XXX

        final int n = currentBest.getDimension();
        final int npt = numberOfInterpolationPoints;
        final int np = n + 1;
        final int nptm = npt - np;
        final int nh = n * np / 2;

        final ArrayRealVector work1 = new ArrayRealVector(n);
        final ArrayRealVector work2 = new ArrayRealVector(npt);
        final ArrayRealVector work3 = new ArrayRealVector(npt);

        double cauchy = Double.NaN;
        double alpha = Double.NaN;
        double dsq = Double.NaN;
        double crvmin = Double.NaN;

        // System generated locals
        double d__1, d__2, d__3, d__4;

        // Local variables
        double f = 0;
        int ih, ip, jp;
        double dx;
        double den = 0, rho = 0, sum = 0, diff = 0, beta = 0, gisq = 0;
        int knew = 0;
        double temp, suma, sumb, bsum, fopt;
        double curv;
        int ksav;
        double gqsq = 0, dist = 0, sumw = 0, sumz = 0, diffa = 0, diffb = 0, diffc = 0, hdiag = 0;
        int kbase;
        double delta = 0, adelt = 0, denom = 0, fsave = 0, bdtol = 0, delsq = 0;
        int nfsav;
        double ratio = 0, dnorm = 0, vquad = 0, pqold = 0;
        int itest;
        double sumpq, scaden;
        double errbig, fracsq, biglsq, densav;
        double bdtest;
        double frhosq;
        double distsq = 0;
        int ntrits;

        // Set some constants.
        // Parameter adjustments

        // Function Body

        // The call of PRELIM sets the elements of XBASE, XPT, FVAL, GOPT, HQ, PQ,
        // BMAT and ZMAT for the first iteration, with the corresponding values of
        // of NF and KOPT, which are the number of calls of CALFUN so far and the
        // index of the interpolation point at the trust region centre. Then the
        // initial XOPT is set too. The branch to label 720 occurs if MAXFUN is
        // less than NPT. GOPT will be updated if KOPT is different from KBASE.

        trustRegionCenterInterpolationPointIndex = 0;

        prelim(currentBest, xbase,
               xpt, fval, gopt, hq, pq, bmat,
                zmat, sl, su);
        double xoptsq = ZERO;
        for (int i = 0; i < n; i++) {
            xopt.setEntry(i, xpt.getEntry(trustRegionCenterInterpolationPointIndex, i));
            // Computing 2nd power
            final double deltaOne = xopt.getEntry(i);
            xoptsq += deltaOne * deltaOne;
        }
        fsave = fval.getEntry(0);
        kbase = 0;

        // Complete the settings that are required for the iterative procedure.

        rho = initialTrustRegionRadius;
        delta = rho;
        ntrits = 0;
        diffa = ZERO;
        diffb = ZERO;
        itest = 0;
        nfsav = getEvaluations();

        // Update GOPT if necessary before the first iteration and after each
        // call of RESCUE that makes a call of CALFUN.

        int state = 20;
        for(;;) switch (state) {
        case 20: {
            if (trustRegionCenterInterpolationPointIndex != kbase) {
                ih = 0;
                for (int j = 0; j < n; j++) {
                    for (int i = 0; i <= j; i++) {
                        if (i < j) {
                            gopt.setEntry(j,  gopt.getEntry(j) + hq.getEntry(ih) * xopt.getEntry(i));
                        }
                        gopt.setEntry(i,  gopt.getEntry(i) + hq.getEntry(ih) * xopt.getEntry(j));
                        ih++;
                    }
                }
                if (getEvaluations() > npt) {
                    for (int k = 0; k < npt; k++) {
                        temp = ZERO;
                        for (int j = 0; j < n; j++) {
                            temp += xpt.getEntry(k, j) * xopt.getEntry(j);
                        }
                        temp = pq.getEntry(k) * temp;
                        for (int i = 0; i < n; i++) {
                            gopt.setEntry(i, gopt.getEntry(i) + temp * xpt.getEntry(k, i));
                        }
                    }
                    throw new PathIsExploredException(); // XXX
                }
            }

            // Generate the next point in the trust region that provides a small value
            // of the quadratic model subject to the constraints on the variables.
            // The int NTRITS is set to the number "trust region" iterations that
            // have occurred since the last "alternative" iteration. If the length
            // of XNEW-XOPT is less than HALF*RHO, however, then there is a branch to
            // label 650 or 680 with NTRITS=-1, instead of calculating F at XNEW.

        }
        case 60: {
            final ArrayRealVector gnew = new ArrayRealVector(n);
            final ArrayRealVector xbdi = new ArrayRealVector(n);
            final ArrayRealVector s = new ArrayRealVector(n);
            final ArrayRealVector hs = new ArrayRealVector(n);
            final ArrayRealVector hred = new ArrayRealVector(n);

            final double[] dsqCrvmin = trsbox(xpt, xopt, gopt, hq, pq, sl,
                                              su, delta, xnew, d__, gnew, xbdi, s,
                                              hs, hred);
            dsq = dsqCrvmin[0];
            crvmin = dsqCrvmin[1];

            // Computing MIN
            double deltaOne = delta;
            double deltaTwo = Math.sqrt(dsq);
            dnorm = Math.min(deltaOne, deltaTwo);
            if (dnorm < HALF * rho) {
                ntrits = -1;
                // Computing 2nd power
                deltaOne = TEN * rho;
                distsq = deltaOne * deltaOne;
                if (getEvaluations() <= nfsav + 2) {
                    state = 650; break;
                }

                // The following choice between labels 650 and 680 depends on whether or
                // not our work with the current RHO seems to be complete. Either RHO is
                // decreased or termination occurs if the errors in the quadratic model at
                // the last three interpolation points compare favourably with predictions
                // of likely improvements to the model within distance HALF*RHO of XOPT.

                // Computing MAX
                deltaOne = Math.max(diffa, diffb);
                errbig = Math.max(deltaOne, diffc);
                frhosq = rho * ONE_OVER_EIGHT * rho;
                if (crvmin > ZERO &&
                    errbig > frhosq * crvmin) {
                    state = 650; break;
                }
                bdtol = errbig / rho;
                for (int j = 0; j < n; j++) {
                    bdtest = bdtol;
                    if (xnew.getEntry(j) == sl.getEntry(j)) {
                        bdtest = work1.getEntry(j);
                    }
                    if (xnew.getEntry(j) == su.getEntry(j)) {
                        bdtest = -work1.getEntry(j);
                    }
                    if (bdtest < bdtol) {
                        curv = hq.getEntry((j + j * j) / 2);
                        for (int k = 0; k < npt; k++) {
                            // Computing 2nd power
                            final double d1 = xpt.getEntry(k, j);
                            curv += pq.getEntry(k) * (d1 * d1);
                        }
                        bdtest += HALF * curv * rho;
                        if (bdtest < bdtol) {
                            state = 650; break;
                        }
                        throw new PathIsExploredException(); // XXX
                    }
                }
                state = 680; break;
            }
            ++ntrits;

            // Severe cancellation is likely to occur if XOPT is too far from XBASE.
            // If the following test holds, then XBASE is shifted so that XOPT becomes
            // zero. The appropriate changes are made to BMAT and to the second
            // derivatives of the current model, beginning with the changes to BMAT
            // that do not depend on ZMAT. VLAG is used temporarily for working space.

        }
        case 90: {
            if (dsq <= xoptsq * ONE_OVER_A_THOUSAND) {
                fracsq = xoptsq * ONE_OVER_FOUR;
                sumpq = ZERO;
                // final RealVector sumVector
                //     = new ArrayRealVector(npt, -HALF * xoptsq).add(xpt.operate(xopt));
                for (int k = 0; k < npt; k++) {
                    sumpq += pq.getEntry(k);
                    sum = -HALF * xoptsq;
                    for (int i = 0; i < n; i++) {
                        sum += xpt.getEntry(k, i) * xopt.getEntry(i);
                    }
                    // sum = sumVector.getEntry(k); // XXX "testAckley" and "testDiffPow" fail.
                    work2.setEntry(k, sum);
                    temp = fracsq - HALF * sum;
                    for (int i = 0; i < n; i++) {
                        work1.setEntry(i, bmat.getEntry(k, i));
                        vlag.setEntry(i, sum * xpt.getEntry(k, i) + temp * xopt.getEntry(i));
                        ip = npt + i;
                        for (int j = 0; j <= i; j++) {
                            bmat.setEntry(ip, j,
                                          bmat.getEntry(ip, j)
                                          + work1.getEntry(i) * vlag.getEntry(j)
                                          + vlag.getEntry(i) * work1.getEntry(j));
                        }
                    }
                }

                // Then the revisions of BMAT that depend on ZMAT are calculated.

                for (int m = 0; m < nptm; m++) {
                    sumz = ZERO;
                    sumw = ZERO;
                    for (int k = 0; k < npt; k++) {
                        sumz += zmat.getEntry(k, m);
                        vlag.setEntry(k, work2.getEntry(k) * zmat.getEntry(k, m));
                        sumw += vlag.getEntry(k);
                    }
                    for (int j = 0; j < n; j++) {
                        sum = (fracsq * sumz - HALF * sumw) * xopt.getEntry(j);
                        for (int k = 0; k < npt; k++) {
                            sum += vlag.getEntry(k) * xpt.getEntry(k, j);
                        }
                        work1.setEntry(j, sum);
                        for (int k = 0; k < npt; k++) {
                            bmat.setEntry(k, j,
                                          bmat.getEntry(k, j)
                                          + sum * zmat.getEntry(k, m));
                        }
                    }
                    for (int i = 0; i < n; i++) {
                        ip = i + npt;
                        temp = work1.getEntry(i);
                        for (int j = 0; j <= i; j++) {
                            bmat.setEntry(ip, j,
                                          bmat.getEntry(ip, j)
                                          + temp * work1.getEntry(j));
                        }
                    }
                }

                // The following instructions complete the shift, including the changes
                // to the second derivative parameters of the quadratic model.

                ih = 0;
                for (int j = 0; j < n; j++) {
                    work1.setEntry(j, -HALF * sumpq * xopt.getEntry(j));
                    for (int k = 0; k < npt; k++) {
                        work1.setEntry(j, work1.getEntry(j) + pq.getEntry(k) * xpt.getEntry(k, j));
                        xpt.setEntry(k, j, xpt.getEntry(k, j) - xopt.getEntry(j));
                    }
                    for (int i = 0; i <= j; i++) {
                         hq.setEntry(ih,
                                    hq.getEntry(ih)
                                    + work1.getEntry(i) * xopt.getEntry(j)
                                    + xopt.getEntry(i) * work1.getEntry(j));
                        bmat.setEntry(npt + i, j, bmat.getEntry(npt + j, i));
                        ih++;
                    }
                }
                for (int i = 0; i < n; i++) {
                    xbase.setEntry(i, xbase.getEntry(i) + xopt.getEntry(i));
                    xnew.setEntry(i, xnew.getEntry(i) - xopt.getEntry(i));
                    sl.setEntry(i, sl.getEntry(i) - xopt.getEntry(i));
                    su.setEntry(i, su.getEntry(i) - xopt.getEntry(i));
                    xopt.setEntry(i, ZERO);
                }
                xoptsq = ZERO;
            }
            if (ntrits == 0) {
                state = 210; break;
            }
            state = 230; break;

            // XBASE is also moved to XOPT by a call of RESCUE. This calculation is
            // more expensive than the previous shift, because new matrices BMAT and
            // ZMAT are generated from scratch, which may include the replacement of
            // interpolation points whose positions seem to be causing near linear
            // dependence in the interpolation conditions. Therefore RESCUE is called
            // only if rounding errors have reduced by at least a factor of two the
            // denominator of the formula for updating the H matrix. It provides a
            // useful safeguard, but is not invoked in most applications of BOBYQA.

        }
        case 210: {
            // Pick two alternative vectors of variables, relative to XBASE, that
            // are suitable as new positions of the KNEW-th interpolation point.
            // Firstly, XNEW is set to the point on a line through XOPT and another
            // interpolation point that minimizes the predicted value of the next
            // denominator, subject to ||XNEW - XOPT|| .LEQ. ADELT and to the SL
            // and SU bounds. Secondly, XALT is set to the best feasible point on
            // a constrained version of the Cauchy step of the KNEW-th Lagrange
            // function, the corresponding value of the square of this function
            // being returned in CAUCHY. The choice between these alternatives is
            // going to be made when the denominator is calculated.

            final double[] alphaCauchy = altmov(xpt, xopt,
                                                bmat, zmat,
                                                sl, su, knew, adelt, xnew, xalt);
            alpha = alphaCauchy[0];
            cauchy = alphaCauchy[1];

            for (int i = 0; i < n; i++) {
                d__.setEntry(i, xnew.getEntry(i) - xopt.getEntry(i));
            }

            // Calculate VLAG and BETA for the current choice of D. The scalar
            // product of D with XPT(K,.) is going to be held in W(NPT+K) for
            // use when VQUAD is calculated.

        }
        case 230: {
            for (int k = 0; k < npt; k++) {
                suma = ZERO;
                sumb = ZERO;
                sum = ZERO;
                for (int j = 0; j < n; j++) {
                    suma += xpt.getEntry(k, j) * d__.getEntry(j);
                    sumb += xpt.getEntry(k, j) * xopt.getEntry(j);
                    sum += bmat.getEntry(k, j) * d__.getEntry(j);
                }
                work3.setEntry(k, suma * (HALF * suma + sumb));
                vlag.setEntry(k, sum);
                work2.setEntry(k, suma);
            }
            beta = ZERO;
            for (int m = 0; m < nptm; m++) {
                sum = ZERO;
                for (int k = 0; k < npt; k++) {
                    sum += zmat.getEntry(k, m) * work3.getEntry(k);
                }
                beta -= sum * sum;
                for (int k = 0; k < npt; k++) {
                    vlag.setEntry(k, vlag.getEntry(k) + sum * zmat.getEntry(k, m));
                }
            }
            dsq = ZERO;
            bsum = ZERO;
            dx = ZERO;
            for (int j = 0; j < n; j++) {
                // Computing 2nd power
                final double d1 = d__.getEntry(j);
                dsq += d1 * d1;
                sum = ZERO;
                for (int k = 0; k < npt; k++) {
                    sum += work3.getEntry(k) * bmat.getEntry(k, j);
                }
                bsum += sum * d__.getEntry(j);
                jp = npt + j;
                for (int i = 0; i < n; i++) {
                    sum += bmat.getEntry(jp, i) * d__.getEntry(i);
                }
                vlag.setEntry(jp, sum);
                bsum += sum * d__.getEntry(j);
                dx += d__.getEntry(j) * xopt.getEntry(j);
            }
            beta = dx * dx + dsq * (xoptsq + dx + dx + HALF * dsq) + beta - bsum;
            vlag.setEntry(trustRegionCenterInterpolationPointIndex, vlag.getEntry(trustRegionCenterInterpolationPointIndex) + ONE);

            // If NTRITS is zero, the denominator may be increased by replacing
            // the step D of ALTMOV by a Cauchy step. Then RESCUE may be called if
            // rounding errors have damaged the chosen denominator.

            if (ntrits == 0) {
                // Computing 2nd power
                final double d1 = vlag.getEntry(knew);
                denom = d1 * d1 + alpha * beta;
                if (denom < cauchy && cauchy > ZERO) {
                    for (int i = 0; i < n; i++) {
                        xnew.setEntry(i, xalt.getEntry(i));
                        d__.setEntry(i, xnew.getEntry(i) - xopt.getEntry(i));
                    }
                    cauchy = ZERO; // XXX Useful statement?
                    state = 230; break;
                }
                // Alternatively, if NTRITS is positive, then set KNEW to the index of
                // the next interpolation point to be deleted to make room for a trust
                // region step. Again RESCUE may be called if rounding errors have damaged_
                // the chosen denominator, which is the reason for attempting to select
                // KNEW before calculating the next value of the objective function.

            } else {
                delsq = delta * delta;
                scaden = ZERO;
                biglsq = ZERO;
                knew = 0;
                for (int k = 0; k < npt; k++) {
                    if (k == trustRegionCenterInterpolationPointIndex) {
                        continue;
                    }
                    hdiag = ZERO;
                    for (int m = 0; m < nptm; m++) {
                        // Computing 2nd power
                        final double d1 = zmat.getEntry(k, m);
                        hdiag += d1 * d1;
                    }
                    // Computing 2nd power
                    d__1 = vlag.getEntry(k);
                    den = beta * hdiag + d__1 * d__1;
                    distsq = ZERO;
                    for (int j = 0; j < n; j++) {
                        // Computing 2nd power
                        final double d1 = xpt.getEntry(k, j) - xopt.getEntry(j);
                        distsq += d1 * d1;
                    }
                    // Computing MAX
                    // Computing 2nd power
                    d__3 = distsq / delsq;
                    d__1 = ONE;
                    d__2 = d__3 * d__3;
                    temp = Math.max(d__1,d__2);
                    if (temp * den > scaden) {
                        scaden = temp * den;
                        knew = k;
                        denom = den;
                    }
                    // Computing MAX
                    // Computing 2nd power
                    d__3 = vlag.getEntry(k);
                    d__1 = biglsq;
                    d__2 = temp * (d__3 * d__3);
                    biglsq = Math.max(d__1, d__2);
                }
            }

            // Put the variables for the next calculation of the objective function
            //   in XNEW, with any adjustments for the bounds.

            // Calculate the value of the objective function at XBASE+XNEW, unless
            //   the limit on the number of calculations of F has been reached.

        }
        case 360: {
            for (int i = 0; i < n; i++) {
                // Computing MIN
                // Computing MAX
                d__3 = lowerBound[i];
                d__4 = xbase.getEntry(i) + xnew.getEntry(i);
                d__1 = Math.max(d__3, d__4);
                d__2 = upperBound[i];
                currentBest.setEntry(i, Math.min(d__1, d__2));
                if (xnew.getEntry(i) == sl.getEntry(i)) {
                    currentBest.setEntry(i, lowerBound[i]);
                }
                if (xnew.getEntry(i) == su.getEntry(i)) {
                    currentBest.setEntry(i, upperBound[i]);
                }
            }

            f = computeObjectiveValue(currentBest.toArray());

            if (!isMinimize)
                f = -f;
            if (ntrits == -1) {
                fsave = f;
                state = 720; break;
            }

            // Use the quadratic model to predict the change in F due to the step D,
            //   and set DIFF to the error of this prediction.

            fopt = fval.getEntry(trustRegionCenterInterpolationPointIndex);
            vquad = ZERO;
            ih = 0;
            for (int j = 0; j < n; j++) {
                vquad += d__.getEntry(j) * gopt.getEntry(j);
                for (int i = 0; i <= j; i++) {
                     temp = d__.getEntry(i) * d__.getEntry(j);
                    if (i == j) {
                        temp = HALF * temp;
                    }
                    vquad += hq.getEntry(ih) * temp;
                    ih++;
               }
            }
            for (int k = 0; k < npt; k++) {
                // Computing 2nd power
                final double d1 = work2.getEntry(k);
                final double d2 = d1 * d1; // "d1" must be squared first to prevent test failures.
                vquad += HALF * pq.getEntry(k) * d2;
            }
            diff = f - fopt - vquad;
            diffc = diffb;
            diffb = diffa;
            diffa = Math.abs(diff);
            if (dnorm > rho) {
                nfsav = getEvaluations();
            }

            // Pick the next value of DELTA after a trust region step.

            if (ntrits > 0) {
                if (vquad >= ZERO) {
                    throw new MathIllegalStateException(LocalizedFormats.TRUST_REGION_STEP_FAILED, vquad);
                }
                ratio = (f - fopt) / vquad;
                if (ratio <= ONE_OVER_TEN) {
                    // Computing MIN
                    d__1 = HALF * delta;
                    delta = Math.min(d__1,dnorm);
                } else if (ratio <= .7) {
                    // Computing MAX
                    d__1 = HALF * delta;
                    delta = Math.max(d__1,dnorm);
                } else {
                    // Computing MAX
                    d__1 = HALF * delta;
                    d__2 = dnorm + dnorm;
                    delta = Math.max(d__1,d__2);
                }
                if (delta <= rho * 1.5) {
                    delta = rho;
                }

                // Recalculate KNEW and DENOM if the new F is less than FOPT.

                if (f < fopt) {
                    ksav = knew;
                    densav = denom;
                    delsq = delta * delta;
                    scaden = ZERO;
                    biglsq = ZERO;
                    knew = 0;
                    for (int k = 0; k < npt; k++) {
                        hdiag = ZERO;
                        for (int m = 0; m < nptm; m++) {
                            // Computing 2nd power
                            final double d1 = zmat.getEntry(k, m);
                            hdiag += d1 * d1;
                        }
                        // Computing 2nd power
                        d__1 = vlag.getEntry(k);
                        den = beta * hdiag + d__1 * d__1;
                        distsq = ZERO;
                        for (int j = 0; j < n; j++) {
                            // Computing 2nd power
                            final double d1 = xpt.getEntry(k, j) - xnew.getEntry(j);
                            distsq += d1 * d1;
                        }
                        // Computing MAX
                        // Computing 2nd power
                        d__3 = distsq / delsq;
                        d__1 = ONE;
                        d__2 = d__3 * d__3;
                        temp = Math.max(d__1, d__2);
                        if (temp * den > scaden) {
                            scaden = temp * den;
                            knew = k;
                            denom = den;
                        }
                        // Computing MAX
                        // Computing 2nd power
                        d__3 = vlag.getEntry(k);
                        d__1 = biglsq;
                        d__2 = temp * (d__3 * d__3);
                        biglsq = Math.max(d__1, d__2);
                    }
                    if (scaden <= HALF * biglsq) {
                        knew = ksav;
                        denom = densav;
                    }
                }
            }

            // Update BMAT and ZMAT, so that the KNEW-th interpolation point can be
            // moved. Also update the second derivative terms of the model.

            update(bmat, zmat, vlag,
                    beta, denom, knew);

            ih = 0;
            pqold = pq.getEntry(knew);
            pq.setEntry(knew, ZERO);
            for (int i = 0; i < n; i++) {
                temp = pqold * xpt.getEntry(knew, i);
                for (int j = 0; j <= i; j++) {
                    hq.setEntry(ih, hq.getEntry(ih) + temp * xpt.getEntry(knew, j));
                    ih++;
                }
            }
            for (int m = 0; m < nptm; m++) {
                temp = diff * zmat.getEntry(knew, m);
                for (int k = 0; k < npt; k++) {
                    pq.setEntry(k, pq.getEntry(k) + temp * zmat.getEntry(k, m));
                }
            }

            // Include the new interpolation point, and make the changes to GOPT at
            // the old XOPT that are caused by the updating of the quadratic model.

            fval.setEntry(knew,  f);
            for (int i = 0; i < n; i++) {
                xpt.setEntry(knew, i, xnew.getEntry(i));
                work1.setEntry(i, bmat.getEntry(knew, i));
            }
            for (int k = 0; k < npt; k++) {
                suma = ZERO;
                for (int m = 0; m < nptm; m++) {
                    suma += zmat.getEntry(knew, m) * zmat.getEntry(k, m);
                }
                sumb = ZERO;
                for (int j = 0; j < n; j++) {
                    sumb += xpt.getEntry(k, j) * xopt.getEntry(j);
                }
                temp = suma * sumb;
                for (int i = 0; i < n; i++) {
                    work1.setEntry(i, work1.getEntry(i) + temp * xpt.getEntry(k, i));
                }
            }
            for (int i = 0; i < n; i++) {
                gopt.setEntry(i, gopt.getEntry(i) + diff * work1.getEntry(i));
            }

            // Update XOPT, GOPT and KOPT if the new calculated F is less than FOPT.

            if (f < fopt) {
                trustRegionCenterInterpolationPointIndex = knew;
                xoptsq = ZERO;
                ih = 0;
                for (int j = 0; j < n; j++) {
                    xopt.setEntry(j, xnew.getEntry(j));
                    // Computing 2nd power
                    final double d1 = xopt.getEntry(j);
                    xoptsq += d1 * d1;
                    for (int i = 0; i <= j; i++) {
                        if (i < j) {
                            gopt.setEntry(j, gopt.getEntry(j) + hq.getEntry(ih) * d__.getEntry(i));
                        }
                        gopt.setEntry(i, gopt.getEntry(i) + hq.getEntry(ih) * d__.getEntry(j));
                        ih++;
                    }
                }
                for (int k = 0; k < npt; k++) {
                    temp = ZERO;
                    for (int j = 0; j < n; j++) {
                        temp += xpt.getEntry(k, j) * d__.getEntry(j);
                    }
                    temp = pq.getEntry(k) * temp;
                    for (int i = 0; i < n; i++) {
                        gopt.setEntry(i, gopt.getEntry(i) + temp * xpt.getEntry(k, i));
                    }
                }
            }

            // Calculate the parameters of the least Frobenius norm interpolant to
            // the current data, the gradient of this interpolant at XOPT being put
            // into VLAG(NPT+I), I=1,2,...,N.

            if (ntrits > 0) {
                for (int k = 0; k < npt; k++) {
                    vlag.setEntry(k, fval.getEntry(k) - fval.getEntry(trustRegionCenterInterpolationPointIndex));
                    work3.setEntry(k, ZERO);
                }
                for (int j = 0; j < nptm; j++) {
                    sum = ZERO;
                    for (int k = 0; k < npt; k++) {
                        sum += zmat.getEntry(k, j) * vlag.getEntry(k);
                    }
                    for (int k = 0; k < npt; k++) {
                        work3.setEntry(k, work3.getEntry(k) + sum * zmat.getEntry(k, j));
                    }
                }
                for (int k = 0; k < npt; k++) {
                    sum = ZERO;
                    for (int j = 0; j < n; j++) {
                        sum += xpt.getEntry(k, j) * xopt.getEntry(j);
                    }
                    work2.setEntry(k, work3.getEntry(k));
                    work3.setEntry(k, sum * work3.getEntry(k));
                }
                gqsq = ZERO;
                gisq = ZERO;
                for (int i = 0; i < n; i++) {
                    sum = ZERO;
                    for (int k = 0; k < npt; k++) {
                        sum += bmat.getEntry(k, i) *
                            vlag.getEntry(k) + xpt.getEntry(k, i) * work3.getEntry(k);
                    }
                    if (xopt.getEntry(i) == sl.getEntry(i)) {
                        // Computing MIN
                        d__2 = ZERO;
                        d__3 = gopt.getEntry(i);
                        // Computing 2nd power
                        d__1 = Math.min(d__2, d__3);
                        gqsq += d__1 * d__1;
                        // Computing 2nd power
                        d__1 = Math.min(ZERO, sum);
                        gisq += d__1 * d__1;
                    } else if (xopt.getEntry(i) == su.getEntry(i)) {
                        // Computing MAX
                        d__2 = ZERO;
                        d__3 = gopt.getEntry(i);
                        // Computing 2nd power
                        d__1 = Math.max(d__2, d__3);
                        gqsq += d__1 * d__1;
                        // Computing 2nd power
                        d__1 = Math.max(ZERO, sum);
                        gisq += d__1 * d__1;
                    } else {
                        // Computing 2nd power
                        d__1 = gopt.getEntry(i);
                        gqsq += d__1 * d__1;
                        gisq += sum * sum;
                    }
                    vlag.setEntry(npt + i, sum);
                }

                // Test whether to replace the new quadratic model by the least Frobenius
                // norm interpolant, making the replacement if the test is satisfied.

                ++itest;
                if (gqsq < TEN * gisq) {
                    itest = 0;
                }
                if (itest >= 3) {
                    for (int i = 0, max = Math.max(npt, nh); i < max; i++) {
                        if (i < n) {
                            gopt.setEntry(i, vlag.getEntry(npt + i));
                        }
                        if (i < npt) {
                            pq.setEntry(i, work2.getEntry(i));
                        }
                        if (i < nh) {
                            hq.setEntry(i, ZERO);
                        }
                        itest = 0;
                    }
                }
            }

            // If a trust region step has provided a sufficient decrease in F, then
            // branch for another trust region calculation. The case NTRITS=0 occurs
            // when the new interpolation point was reached by an alternative step.

            if (ntrits == 0) {
                state = 60; break;
            }
            if (f <= fopt + ONE_OVER_TEN * vquad) {
                state = 60; break;
            }

            // Alternatively, find out if the interpolation points are close enough
            //   to the best point so far.

            // Computing MAX
            // Computing 2nd power
            d__3 = TWO * delta;
            // Computing 2nd power
            d__4 = TEN * rho;
            d__1 = d__3 * d__3;
            d__2 = d__4 * d__4;
            distsq = Math.max(d__1, d__2);
        }
        case 650: {
            knew = -1;
            for (int k = 0; k < npt; k++) {
                sum = ZERO;
                for (int j = 0; j < n; j++) {
                    // Computing 2nd power
                    final double d1 = xpt.getEntry(k, j) - xopt.getEntry(j);
                    sum += d1 * d1;
                }
                if (sum > distsq) {
                    knew = k;
                    distsq = sum;
                }
            }

            // If KNEW is positive, then ALTMOV finds alternative new positions for
            // the KNEW-th interpolation point within distance ADELT of XOPT. It is
            // reached via label 90. Otherwise, there is a branch to label 60 for
            // another trust region iteration, unless the calculations with the
            // current RHO are complete.

            if (knew >= 0) {
                dist = Math.sqrt(distsq);
                if (ntrits == -1) {
                    // Computing MIN
                    d__1 = ONE_OVER_TEN * delta;
                    d__2 = HALF * dist;
                    delta = Math.min(d__1,d__2);
                    if (delta <= rho * 1.5) {
                        delta = rho;
                    }
                }
                ntrits = 0;
                // Computing MAX
                // Computing MIN
                d__2 = ONE_OVER_TEN * dist;
                d__1 = Math.min(d__2, delta);
                adelt = Math.max(d__1, rho);
                dsq = adelt * adelt;
                state = 90; break;
            }
            if (ntrits == -1) {
                state = 680; break;
            }
            if (ratio > ZERO) {
                state = 60; break;
            }
            if (Math.max(delta, dnorm) > rho) {
                state = 60; break;
            }

            // The calculations with the current value of RHO are complete. Pick the
            //   next values of RHO and DELTA.
        }
        case 680: {
            if (rho > stoppingTrustRegionRadius) {
                delta = HALF * rho;
                ratio = rho / stoppingTrustRegionRadius;
                if (ratio <= SIXTEEN) {
                    rho = stoppingTrustRegionRadius;
                } else if (ratio <= TWO_HUNDRED_FIFTY) {
                    rho = Math.sqrt(ratio) * stoppingTrustRegionRadius;
                } else {
                    rho = ONE_OVER_TEN * rho;
                }
                delta = Math.max(delta, rho);
                ntrits = 0;
                nfsav = getEvaluations();
                state = 60; break;
            }

            // Return from the calculation, after another Newton-Raphson step, if
            //   it is too short to have been tried before.

            if (ntrits == -1) {
                state = 360; break;
            }
        }
        case 720: {
            if (fval.getEntry(trustRegionCenterInterpolationPointIndex) <= fsave) {
                for (int i = 0; i < n; i++) {
                    // Computing MIN
                    // Computing MAX
                    d__3 = lowerBound[i];
                    d__4 = xbase.getEntry(i) + xopt.getEntry(i);
                    d__1 = Math.max(d__3, d__4);
                    d__2 = upperBound[i];
                    currentBest.setEntry(i, Math.min(d__1, d__2));
                    if (xopt.getEntry(i) == sl.getEntry(i)) {
                        currentBest.setEntry(i, lowerBound[i]);
                    }
                    if (xopt.getEntry(i) == su.getEntry(i)) {
                        currentBest.setEntry(i, upperBound[i]);
                    }
                }
                f = fval.getEntry(trustRegionCenterInterpolationPointIndex);
            }
            return f;
        }
        default: {
            throw new MathIllegalStateException(LocalizedFormats.SIMPLE_MESSAGE, "bobyqb");
        }}
    } // bobyqb
