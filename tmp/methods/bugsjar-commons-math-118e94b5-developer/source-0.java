  public Rotation(Vector3D u1, Vector3D u2, Vector3D v1, Vector3D v2) {

  // norms computation
  double u1u1 = u1.getNormSq();
  double u2u2 = u2.getNormSq();
  double v1v1 = v1.getNormSq();
  double v2v2 = v2.getNormSq();
  if ((u1u1 == 0) || (u2u2 == 0) || (v1v1 == 0) || (v2v2 == 0)) {
    throw new MathIllegalArgumentException(LocalizedFormats.ZERO_NORM_FOR_ROTATION_DEFINING_VECTOR);
  }

  // normalize v1 in order to have (v1'|v1') = (u1|u1)
  v1 = new Vector3D(FastMath.sqrt(u1u1 / v1v1), v1);

  // adjust v2 in order to have (u1|u2) = (v1'|v2') and (v2'|v2') = (u2|u2)
  double u1u2   = u1.dotProduct(u2);
  double v1v2   = v1.dotProduct(v2);
  double coeffU = u1u2 / u1u1;
  double coeffV = v1v2 / u1u1;
  double beta   = FastMath.sqrt((u2u2 - u1u2 * coeffU) / (v2v2 - v1v2 * coeffV));
  double alpha  = coeffU - beta * coeffV;
  v2 = new Vector3D(alpha, v1, beta, v2);

  // preliminary computation
  Vector3D uRef  = u1;
  Vector3D vRef  = v1;
  Vector3D v1Su1 = v1.subtract(u1);
  Vector3D v2Su2 = v2.subtract(u2);
  Vector3D k     = v1Su1.crossProduct(v2Su2);
  Vector3D u3    = u1.crossProduct(u2);
  double c       = k.dotProduct(u3);
  final double inPlaneThreshold = 0.001;
  if (c <= inPlaneThreshold * k.getNorm() * u3.getNorm()) {
    // the (q1, q2, q3) vector is close to the (u1, u2) plane
    // we try other vectors
    Vector3D v3 = Vector3D.crossProduct(v1, v2);
    Vector3D v3Su3 = v3.subtract(u3);
    k = v1Su1.crossProduct(v3Su3);
    Vector3D u2Prime = u1.crossProduct(u3);
    c = k.dotProduct(u2Prime);

    if (c <= inPlaneThreshold * k.getNorm() * u2Prime.getNorm()) {
      // the (q1, q2, q3) vector is also close to the (u1, u3) plane,
      // it is almost aligned with u1: we try (u2, u3) and (v2, v3)
      k = v2Su2.crossProduct(v3Su3);
      c = k.dotProduct(u2.crossProduct(u3));

      if (c <= 0) {
        // the (q1, q2, q3) vector is aligned with everything
        // this is really the identity rotation
        q0 = 1.0;
        q1 = 0.0;
        q2 = 0.0;
        q3 = 0.0;
        return;
      }

      // we will have to use u2 and v2 to compute the scalar part
      uRef = u2;
      vRef = v2;

    }

  }

  // compute the vectorial part
  c = FastMath.sqrt(c);
  double inv = 1.0 / (c + c);
  q1 = inv * k.getX();
  q2 = inv * k.getY();
  q3 = inv * k.getZ();

  // compute the scalar part
   k = new Vector3D(uRef.getY() * q3 - uRef.getZ() * q2,
                    uRef.getZ() * q1 - uRef.getX() * q3,
                    uRef.getX() * q2 - uRef.getY() * q1);
  q0 = vRef.dotProduct(k) / (2 * k.getNormSq());

  }
  public Rotation(double[][] m, double threshold)
    throws NotARotationMatrixException {

    // dimension check
    if ((m.length != 3) || (m[0].length != 3) ||
        (m[1].length != 3) || (m[2].length != 3)) {
      throw new NotARotationMatrixException(
              LocalizedFormats.ROTATION_MATRIX_DIMENSIONS,
              m.length, m[0].length);
    }

    // compute a "close" orthogonal matrix
    double[][] ort = orthogonalizeMatrix(m, threshold);

    // check the sign of the determinant
    double det = ort[0][0] * (ort[1][1] * ort[2][2] - ort[2][1] * ort[1][2]) -
                 ort[1][0] * (ort[0][1] * ort[2][2] - ort[2][1] * ort[0][2]) +
                 ort[2][0] * (ort[0][1] * ort[1][2] - ort[1][1] * ort[0][2]);
    if (det < 0.0) {
      throw new NotARotationMatrixException(
              LocalizedFormats.CLOSEST_ORTHOGONAL_MATRIX_HAS_NEGATIVE_DETERMINANT,
              det);
    }

    // There are different ways to compute the quaternions elements
    // from the matrix. They all involve computing one element from
    // the diagonal of the matrix, and computing the three other ones
    // using a formula involving a division by the first element,
    // which unfortunately can be zero. Since the norm of the
    // quaternion is 1, we know at least one element has an absolute
    // value greater or equal to 0.5, so it is always possible to
    // select the right formula and avoid division by zero and even
    // numerical inaccuracy. Checking the elements in turn and using
    // the first one greater than 0.45 is safe (this leads to a simple
    // test since qi = 0.45 implies 4 qi^2 - 1 = -0.19)
    double s = ort[0][0] + ort[1][1] + ort[2][2];
    if (s > -0.19) {
      // compute q0 and deduce q1, q2 and q3
      q0 = 0.5 * FastMath.sqrt(s + 1.0);
      double inv = 0.25 / q0;
      q1 = inv * (ort[1][2] - ort[2][1]);
      q2 = inv * (ort[2][0] - ort[0][2]);
      q3 = inv * (ort[0][1] - ort[1][0]);
    } else {
      s = ort[0][0] - ort[1][1] - ort[2][2];
      if (s > -0.19) {
        // compute q1 and deduce q0, q2 and q3
        q1 = 0.5 * FastMath.sqrt(s + 1.0);
        double inv = 0.25 / q1;
        q0 = inv * (ort[1][2] - ort[2][1]);
        q2 = inv * (ort[0][1] + ort[1][0]);
        q3 = inv * (ort[0][2] + ort[2][0]);
      } else {
        s = ort[1][1] - ort[0][0] - ort[2][2];
        if (s > -0.19) {
          // compute q2 and deduce q0, q1 and q3
          q2 = 0.5 * FastMath.sqrt(s + 1.0);
          double inv = 0.25 / q2;
          q0 = inv * (ort[2][0] - ort[0][2]);
          q1 = inv * (ort[0][1] + ort[1][0]);
          q3 = inv * (ort[2][1] + ort[1][2]);
        } else {
          // compute q3 and deduce q0, q1 and q2
          s = ort[2][2] - ort[0][0] - ort[1][1];
          q3 = 0.5 * FastMath.sqrt(s + 1.0);
          double inv = 0.25 / q3;
          q0 = inv * (ort[0][1] - ort[1][0]);
          q1 = inv * (ort[0][2] + ort[2][0]);
          q2 = inv * (ort[2][1] + ort[1][2]);
        }
      }
    }

  }
