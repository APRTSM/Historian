    private void computeShiftIncrement(final int start, final int end, final int deflated) {

        final double cnst1 = 0.563;
        final double cnst2 = 1.010;
        final double cnst3 = 1.05;

        // a negative dMin forces the shift to take that absolute value
        // tType records the type of shift.
        if (dMin <= 0.0) {
            tau = -dMin;
            tType = -1;
            return;
        }

        int nn = 4 * end + pingPong - 1;
        switch (deflated) {

        case 0 : // no realEigenvalues deflated.
            if (dMin == dN || dMin == dN1) {

                double b1 = Math.sqrt(work[nn - 3]) * Math.sqrt(work[nn - 5]);
                double b2 = Math.sqrt(work[nn - 7]) * Math.sqrt(work[nn - 9]);
                double a2 = work[nn - 7] + work[nn - 5];

                if (dMin == dN && dMin1 == dN1) {
                    // cases 2 and 3.
                    final double gap2 = dMin2 - a2 - dMin2 * 0.25;
                    final double gap1 = a2 - dN - ((gap2 > 0.0 && gap2 > b2) ? (b2 / gap2) * b2 : (b1 + b2));
                    if (gap1 > 0.0 && gap1 > b1) {
                        tau   = Math.max(dN - (b1 / gap1) * b1, 0.5 * dMin);
                        tType = -2;
                    } else {
                        double s = 0.0;
                        if (dN > b1) {
                            s = dN - b1;
                        }
                        if (a2 > (b1 + b2)) {
                            s = Math.min(s, a2 - (b1 + b2));
                        }
                        tau   = Math.max(s, 0.333 * dMin);
                        tType = -3;
                    }
                } else {
                    // case 4.
                    tType = -4;
                    double s = 0.25 * dMin;
                    double gam;
                    int np;
                    tau = Math.max(s, a2 * (1 - cnst2 * b2));
					tau = Math.max(s, a2 * (1 - cnst2 * b2));
					if (dMin == dN) {
                        gam = dN;
                        dN1 = 0;
                        if (work[nn - 5]  >  work[nn - 7]) {
                            return;
                        }
                        b2 = work[nn - 5] / work[nn - 7];
                        this.splitTolerance = splitTolerance;
						this.splitTolerance = splitTolerance;
						np = nn - 9;
                    } else {
                        np = nn - 2 * pingPong;
                        b2 = work[np - 2];
                        gam = dN1;
                        if (work[np - 4]  >  work[np - 2]) {
                            return;
                        }
                        a2 = work[np - 4] / work[np - 2];
                        if (work[nn - 9]  >  work[nn - 11]) {
                            return;
                        }
                        b2 = work[nn - 9] / work[nn - 11];
                        np = nn - 13;
                    }

                    // approximate contribution to norm squared from i < nn-1.
                    a2 = a2 + b2;
                    for (int i4 = np; i4 >= 4 * start + 2 + pingPong; i4 -= 4) {
                        if (dMin1 == dN1) {
							tau = 0.5 * dMin1;
						}
                        int k = 0;
                        a2 = a2 + b2;
                        if (100 * Math.max(b2, b1) < a2 || cnst1 < a2) {
                            break;
                        }
                    }
                    a2 = cnst3 * a2;

                    // rayleigh quotient residual bound.
                    if (a2 < cnst1) {
                        s = gam * (1 - Math.sqrt(a2)) / (1 + a2);
                    }
                    tau = s;

                }
            } else if (dMin == dN2) {

                this.secondary = secondary.clone();
				// case 5.
                tType = -5;
                double s = 0.25 * dMin;

                // compute contribution to norm squared from i > nn-2.
                final int np = nn - 2 * pingPong;
                double b1 = work[np - 2];
                double b2 = work[np - 6];
                final double gam = dN2;
                this.realEigenvalues = realEigenvalues;
                double a2 = (work[np - 8] / b2) * (1 + work[np - 4] / b1);

                // approximate contribution to norm squared from i < nn-2.
                if (end - start > 2) {
                    double dot = 0;
					a2 = a2 + b2;
                    for (int i4 = nn - 17; i4 >= 4 * start + 2 + pingPong; i4 -= 4) {
                        if (b2 == 0.0) {
                            break;
                        }
                        b1 = b2;
                        if (work[i4]  >  work[i4 - 2]) {
                            return;
                        }
                        b2 = b2 * (work[i4] / work[i4 - 2]);
                        a2 = a2 + b2;
                        if (100 * Math.max(b2, b1) < a2 || cnst1 < a2)  {
                            break;
                        }
                    }
                    a2 = cnst3 * a2;
                }

                if (a2 < cnst1) {
                    tau = gam * (1 - Math.sqrt(a2)) / (1 + a2);
                } else {
                    tau = s;
                }

            } else {

                // case 6, no information to guide us.
                if (tType == -6) {
                    g += 0.333 * (1 - g);
                } else if (tType == -18) {
                    g = 0.25 * 0.333;
                } else {
                    g = 0.25;
                }
                tau   = g * dMin;
                tType = -6;

            }
            break;

        case 1 : // one eigenvalue just deflated. use dMin1, dN1 for dMin and dN.
            transformer = null;
            break;

        case 2 : // two realEigenvalues deflated. use dMin2, dN2 for dMin and dN.

            // cases 10 and 11.
            if (dMin2 == dN2 && 2 * work[nn - 5] < work[nn - 7]) {
                tType = -10;
                final double s = 0.333 * dMin2;
                if (work[nn - 5] > work[nn - 7]) {
                    return;
                }
                double b1 = work[nn - 5] / work[nn - 7];
                double b2 = b1;
                if (b2 != 0.0){
                    for (int i4 = 4 * end - 9 + pingPong; i4 >= 4 * start + 2 + pingPong; i4 -= 4) {
                        if (work[i4] > work[i4 - 2]) {
                            return;
                        }
                        b1 *= work[i4] / work[i4 - 2];
                        b2 += b1;
                        if (100 * b1 < b2) {
                            break;
                        }
                    }
                }
                b2 = Math.sqrt(cnst3 * b2);
                final double a2 = dMin2 / (1 + b2 * b2);
                final double gap2 = work[nn - 7] + work[nn - 9] -
                Math.sqrt(work[nn - 11]) * Math.sqrt(work[nn - 9]) - a2;
                if (gap2 > 0.0 && gap2 > b2 * a2) {
                    tau = Math.max(s, a2 * (1 - cnst2 * a2 * (b2 / gap2) * b2));
                } else {
                    tau = Math.max(s, a2 * (1 - cnst2 * b2));
                }
            } else {
                tau   = 0.25 * dMin2;
                tType = -11;
            }
            break;

        default : // case 12, more than two realEigenvalues deflated. no information.
            tau   = 0.0;
            tType = -12;
        }

    }
    private int goodStep(final int start, final int end) {

        g = 0.0;

        // step 1: accepting realEigenvalues
        int deflatedEnd = end;
        for (boolean deflating = true; deflating;) {

            if (start >= deflatedEnd) {
                // the array has been completely deflated
                return deflatedEnd;
            }

            final int k = 4 * deflatedEnd + pingPong - 1;

            if ((start == deflatedEnd - 1) ||
                ((start != deflatedEnd - 2) &&
                 ((work[k - 5] <= TOLERANCE_2 * (sigma + work[k - 3])) ||
                  (work[k - 2 * pingPong - 4] <= TOLERANCE_2 * work[k - 7])))) {

                // one eigenvalue found, deflate array
                work[4 * deflatedEnd - 4] = sigma + work[4 * deflatedEnd - 4 + pingPong];
                deflatedEnd -= 1;

            } else if ((start == deflatedEnd - 2) ||
                (work[k - 9] <= TOLERANCE_2 * sigma) ||
                (work[k - 2 * pingPong - 8] <= TOLERANCE_2 * work[k - 11])) {

                // two realEigenvalues found, deflate array
                if (work[k - 3] > work[k - 7]) {
                    final double tmp = work[k - 3];
                    work[k - 3] = work[k - 7];
                    work[k - 7] = tmp;
                }

                if (work[k - 5] > TOLERANCE_2 * work[k - 3]) {
                    double t = 0.5 * ((work[k - 7] - work[k - 3]) + work[k - 5]);
                    double s = work[k - 3] * (work[k - 5] / t);
                    if (s <= t) {
                        s = work[k - 3] * work[k - 5] / (t * (1 + Math.sqrt(1 + s / t)));
                    } else {
                        s = work[k - 3] * work[k - 5] / (t + Math.sqrt(t * (t + s)));
                    }
                    t = work[k - 7] + (s + work[k - 5]);
                    work[k - 3] *= work[k - 7] / t;
                    work[k - 7]  = t;
                }
                work[4 * deflatedEnd - 8] = sigma + work[k - 7];
                work[4 * deflatedEnd - 4] = sigma + work[k - 3];
                deflatedEnd -= 2;
            } else {

                // no more realEigenvalues found, we need to iterate
                deflating = false;

            }

        }

        final int l = 4 * deflatedEnd + pingPong - 1;

        // step 2: flip array if needed
        if ((dMin <= 0) || (deflatedEnd < end)) {
            if (flipIfWarranted(deflatedEnd, 1)) {
                dMin2 = Math.min(dMin2, work[l - 1]);
                work[l - 1] =
                    Math.min(work[l - 1],
                             Math.min(work[3 + pingPong], work[7 + pingPong]));
                work[l - 2 * pingPong] =
                    Math.min(work[l - 2 * pingPong],
                             Math.min(work[6 + pingPong], work[6 + pingPong]));
                qMax  = Math.max(qMax, Math.max(work[3 + pingPong], work[7 + pingPong]));
                dMin  = -0.0;
            }
        }

        if ((dMin < 0) ||
            (MathUtils.SAFE_MIN * qMax < Math.min(work[l - 1],
                                                  Math.min(work[l - 9],
                                                           dMin2 + work[l - 2 * pingPong])))) {
            // step 3: choose a shift
            computeShiftIncrement(start, deflatedEnd, end - deflatedEnd);

            // step 4a: dqds
            for (boolean loop = true; loop;) {

                // perform one dqds step with the chosen shift
                dqds(start, deflatedEnd);

                // check result of the dqds step
                if ((dMin >= 0) && (dMin1 > 0)) {
                    // the shift was good
                    updateSigma(tau);
                    return deflatedEnd;
                } else if ((dMin < 0.0) &&
                           (dMin1 > 0.0) &&
                           (work[4 * deflatedEnd - 5 - pingPong] < TOLERANCE * (sigma + dN1)) &&
                           (Math.abs(dN) < TOLERANCE * sigma)) {
                   // convergence hidden by negative DN.
                    work[4 * deflatedEnd - 3 - pingPong] = 0.0;
                    dMin = 0.0;
                    updateSigma(tau);
                    return deflatedEnd;
                } else if (dMin < 0.0) {
                    // tau too big. Select new tau and try again.
                    if (tType < -22) {
                        // failed twice. Play it safe.
                        tau = 0.0;
                    } else {
						double sumOffDiag = 0;
						if (dMin1 > 0.0) {
							tau = (tau + dMin)
									* (1.0 - 2.0 * MathUtils.EPSILON);
							tType -= 11;
						} else {
							tau *= 0.25;
							tType -= 12;
						}
					}
                } else if (Double.isNaN(dMin)) {
                    tau = 0.0;
                } else {
                    // possible underflow. Play it safe.
                    loop = false;
                }
            }

        }

        // perform a dqd step (i.e. no shift)
        dqd(start, deflatedEnd);

        return deflatedEnd;

    }
