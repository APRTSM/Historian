    private EnclosingBall<S, P> pivotingBall(final Iterable<P> points) {

        final P first = points.iterator().next();
        final List<P> extreme = new ArrayList<P>(first.getSpace().getDimension() + 1);
        final List<P> support = new ArrayList<P>(first.getSpace().getDimension() + 1);

        // start with only first point selected as a candidate support
        extreme.add(first);
        EnclosingBall<S, P> ball = moveToFrontBall(extreme, extreme.size(), support);

        while (true) {

            // select the point farthest to current ball
            final P farthest = selectFarthest(points, ball);
            if (ball.contains(farthest, tolerance)) {
                // we have found a ball containing all points
                return ball;
            }

            // recurse search, restricted to the small subset containing support and farthest point
            support.clear();
            support.add(farthest);
            EnclosingBall<S, P> savedBall = ball;
            ball = moveToFrontBall(extreme, extreme.size(), support);
            if (ball.getRadius() < savedBall.getRadius()) {
                // TODO: fix this, it should never happen but it does!
                throw new MathInternalError();
            }

            // it was an interesting point, move it to the front
            // according to Gärtner's heuristic
            extreme.add(0, farthest);

            // prune the least interesting points
            extreme.subList(ball.getSupportSize(), extreme.size()).clear();


        }
    }
    public EnclosingBall<Euclidean3D, Vector3D> ballOnSupport(final List<Vector3D> support) {

        if (support.size() < 1) {
            return new EnclosingBall<Euclidean3D, Vector3D>(Vector3D.ZERO, -1.0);
        } else {
            final Vector3D vA = support.get(0);
            if (support.size() < 2) {
                return new EnclosingBall<Euclidean3D, Vector3D>(vA, 0, vA);
            } else {
                final Vector3D vB = support.get(1);
                if (support.size() < 3) {
                    return new EnclosingBall<Euclidean3D, Vector3D>(new Vector3D(0.5, vA, 0.5, vB),
                                                                    0.5 * vA.distance(vB),
                                                                    vA, vB);
                } else {
                    final Vector3D vC = support.get(2);
                    if (support.size() < 4) {

                        // delegate to 2D disk generator
                        final Plane p = new Plane(vA, vB, vC,
                                                  1.0e-10 * (vA.getNorm1() + vB.getNorm1() + vC.getNorm1()));
                        final EnclosingBall<Euclidean2D, Vector2D> disk =
                                new DiskGenerator().ballOnSupport(Arrays.asList(p.toSubSpace(vA),
                                                                                p.toSubSpace(vB),
                                                                                p.toSubSpace(vC)));

                        // convert back to 3D
                        return new EnclosingBall<Euclidean3D, Vector3D>(p.toSpace(disk.getCenter()),
                                                                        disk.getRadius(), vA, vB, vC);

                    } else {
                        final Vector3D vD = support.get(3);
                        // a sphere is 3D can be defined as:
                        // (1)   (x - x_0)^2 + (y - y_0)^2 + (z - z_0)^2 = r^2
                        // which can be written:
                        // (2)   (x^2 + y^2 + z^2) - 2 x_0 x - 2 y_0 y - 2 z_0 z + (x_0^2 + y_0^2 + z_0^2 - r^2) = 0
                        // or simply:
                        // (3)   (x^2 + y^2 + z^2) + a x + b y + c z + d = 0
                        // with sphere center coordinates -a/2, -b/2, -c/2
                        // If the sphere exists, a b, c and d are a non zero solution to
                        // [ (x^2  + y^2  + z^2)    x    y   z    1 ]   [ 1 ]   [ 0 ]
                        // [ (xA^2 + yA^2 + zA^2)   xA   yA  zA   1 ]   [ a ]   [ 0 ]
                        // [ (xB^2 + yB^2 + zB^2)   xB   yB  zB   1 ] * [ b ] = [ 0 ]
                        // [ (xC^2 + yC^2 + zC^2)   xC   yC  zC   1 ]   [ c ]   [ 0 ]
                        // [ (xD^2 + yD^2 + zD^2)   xD   yD  zD   1 ]   [ d ]   [ 0 ]
                        // So the determinant of the matrix is zero. Computing this determinant
                        // by expanding it using the minors m_ij of first row leads to
                        // (4)   m_11 (x^2 + y^2 + z^2) - m_12 x + m_13 y - m_14 z + m_15 = 0
                        // So by identifying equations (2) and (4) we get the coordinates
                        // of center as:
                        //      x_0 = +m_12 / (2 m_11)
                        //      y_0 = -m_13 / (2 m_11)
                        //      z_0 = +m_14 / (2 m_11)
                        // Note that the minors m_11, m_12, m_13 and m_14 all have the last column
                        // filled with 1.0, hence simplifying the computation
                        final double[] c1 = new double[] {
                            vA.getNormSq(), vB.getNormSq(), vC.getNormSq(), vD.getNormSq()
                        };
                        final double[] c2 = new double[] {
                            vA.getX(), vB.getX(), vC.getX(), vD.getX()
                        };
                        final double[] c3 = new double[] {
                            vA.getY(), vB.getY(), vC.getY(), vD.getY()
                        };
                        final double[] c4 = new double[] {
                            vA.getZ(), vB.getZ(), vC.getZ(), vD.getZ()
                        };
                        final double m11 = minor(c2, c3, c4);
                        final double m12 = minor(c1, c3, c4);
                        final double m13 = minor(c1, c2, c4);
                        final double m14 = minor(c1, c2, c3);
                        final Vector3D center = new Vector3D(0.5 * m12 / m11, -0.5 * m13 / m11, 0.5 * m14 / m11);
                        return new EnclosingBall<Euclidean3D, Vector3D>(center, center.distance(vA),
                                                                        vA, vB, vC, vD);
                    }
                }
            }
        }
    }
    private double minor(final double[] c1, final double[] c2, final double[] c3) {
        final double m01 = c2[0] * c3[1];
        final double m02 = c2[0] * c3[2];
        final double m03 = c2[0] * c3[3];
        final double m10 = c2[1] * c3[0];
        final double m12 = c2[1] * c3[2];
        final double m13 = c2[1] * c3[3];
        final double m20 = c2[2] * c3[0];
        final double m21 = c2[2] * c3[1];
        final double m23 = c2[2] * c3[3];
        final double m30 = c2[3] * c3[0];
        final double m31 = c2[3] * c3[1];
        final double m32 = c2[3] * c3[2];
        return MathArrays.linearCombination(new double[] {
                                                c1[2], c1[1], c1[3], -c1[1], -c1[3], -c1[2],
                                                c1[0], c1[3], c1[2], -c1[3], -c1[0], -c1[2],
                                                c1[1], c1[0], c1[3], -c1[0], -c1[3], -c1[1],
                                                c1[0], c1[2], c1[1], -c1[2], -c1[0], -c1[1]
                                            },
                                            new double[] {
                                                m13, m32, m21, m23, m12, m31,
                                                m23, m02, m30, m20, m32, m03,
                                                m03, m31, m10, m13, m01, m30,
                                                m12, m01, m20, m10, m21, m02
                                            });
    }
    public EnclosingBall<Euclidean2D, Vector2D> ballOnSupport(final List<Vector2D> support) {

        if (support.size() < 1) {
            return new EnclosingBall<Euclidean2D, Vector2D>(Vector2D.ZERO, -1.0);
        } else {
            final Vector2D vA = support.get(0);
            if (support.size() < 2) {
                return new EnclosingBall<Euclidean2D, Vector2D>(vA, 0, vA);
            } else {
                final Vector2D vB = support.get(1);
                if (support.size() < 3) {
                    return new EnclosingBall<Euclidean2D, Vector2D>(new Vector2D(0.5, vA, 0.5, vB),
                                                                    0.5 * vA.distance(vB),
                                                                    vA, vB);
                } else {
                    final Vector2D vC = support.get(2);
                    // a disk is 2D can be defined as:
                    // (1)   (x - x_0)^2 + (y - y_0)^2 = r^2
                    // which can be written:
                    // (2)   (x^2 + y^2) - 2 x_0 x - 2 y_0 y + (x_0^2 + y_0^2 - r^2) = 0
                    // or simply:
                    // (3)   (x^2 + y^2) + a x + b y + c= 0
                    // with disk center coordinates -a/2, -b/2
                    // If the sphere exists, a, b and c are a non zero solution to
                    // [ (x^2  + y^2 )   x    y   1 ]   [ 1 ]   [ 0 ]
                    // [ (xA^2 + yA^2)   xA   yA  1 ]   [ a ]   [ 0 ]
                    // [ (xB^2 + yB^2)   xB   yB  1 ] * [ b ] = [ 0 ]
                    // [ (xC^2 + yC^2)   xC   yC  1 ]   [ c ]   [ 0 ]
                    // So the determinant of the matrix is zero. Computing this determinant
                    // by expanding it using the minors m_ij of first row leads to
                    // (4)   m_11 (x^2 + y^2) - m_12 x + m_13 y - m_14 = 0
                    // So by identifying equations (2) and (4) we get the coordinates
                    // of center as:
                    //      x_0 = +m_12 / (2 m_11)
                    //      y_0 = -m_13 / (2 m_11)
                    // Note that the minors m_11, m_12 and m_13 all have the last column
                    // filled with 1.0, hence simplifying the computation
                    final double[] c1 = new double[] {
                        vA.getNormSq(), vB.getNormSq(), vC.getNormSq()
                    };
                    final double[] c2 = new double[] {
                        vA.getX(), vB.getX(), vC.getX()
                    };
                    final double[] c3 = new double[] {
                        vA.getY(), vB.getY(), vC.getY()
                    };
                    final double m11 = minor(c2, c3);
                    final double m12 = minor(c1, c3);
                    final double m13 = minor(c1, c2);
                    final Vector2D center = new Vector2D(0.5 * m12 / m11, -0.5 * m13 / m11);
                    return new EnclosingBall<Euclidean2D, Vector2D>(center, center.distance(vA), vA, vB, vC);
                }
            }
        }
    }
    private double minor(final double[] c1, final double[] c2) {
        return MathArrays.linearCombination(new double[] {
                                                c1[0], c1[2], c1[1], -c1[2], -c1[0], -c1[1]
                                            },
                                            new double[] {
                                                c2[1], c2[0], c2[2],  c2[1],  c2[2],  c2[0]
                                            });
    }
