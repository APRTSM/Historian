  private static double[] mat2quat(final double[][] ort) {

      final double[] quat = new double[4];

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
          quat[0] = 0.5 * FastMath.sqrt(s + 1.0);
          double inv = 0.25 / quat[0];
          quat[1] = inv * (ort[1][2] - ort[2][1]);
          quat[2] = inv * (ort[2][0] - ort[0][2]);
          quat[3] = inv * (ort[0][1] - ort[1][0]);
      } else {
          s = ort[0][0] - ort[1][1] - ort[2][2];
          if (s > -0.19) {
              // compute q1 and deduce q0, q2 and q3
              quat[1] = 0.5 * FastMath.sqrt(s + 1.0);
              double inv = 0.25 / quat[1];
              quat[0] = inv * (ort[1][2] - ort[2][1]);
              quat[2] = inv * (ort[0][1] + ort[1][0]);
              quat[3] = inv * (ort[0][2] + ort[2][0]);
          } else {
              s = ort[1][1] - ort[0][0] - ort[2][2];
              if (s > -0.19) {
                  // compute q2 and deduce q0, q1 and q3
                  quat[2] = 0.5 * FastMath.sqrt(s + 1.0);
                  double inv = 0.25 / quat[2];
                  quat[0] = inv * (ort[2][0] - ort[0][2]);
                  quat[1] = inv * (ort[0][1] + ort[1][0]);
                  quat[3] = inv * (ort[2][1] + ort[1][2]);
              } else {
                  // compute q3 and deduce q0, q1 and q2
                  s = ort[2][2] - ort[0][0] - ort[1][1];
                  quat[3] = 0.5 * FastMath.sqrt(s + 1.0);
                  double inv = 0.25 / quat[3];
                  quat[0] = inv * (ort[0][1] - ort[1][0]);
                  quat[1] = inv * (ort[0][2] + ort[2][0]);
                  quat[2] = inv * (ort[2][1] + ort[1][2]);
              }
          }
      }

      return quat;

  }
  public Rotation(Vector3D u1, Vector3D u2, Vector3D v1, Vector3D v2)
      throws MathIllegalArgumentException {

      // build orthonormalized base from u1, u2
      // this fails when vectors are null or colinear, which is forbidden to define a rotation
      final Vector3D u3 = u1.crossProduct(u2).normalize();
      u2 = u3.crossProduct(u1).normalize();
      u1 = u1.normalize();

      // build an orthonormalized base from v1, v2
      // this fails when vectors are null or colinear, which is forbidden to define a rotation
      final Vector3D v3 = v1.crossProduct(v2).normalize();
      v2 = v3.crossProduct(v1).normalize();
      v1 = v1.normalize();

      // buid a matrix transforming the first base into the second one
      final double[][] m = new double[][] {
          {
              MathArrays.linearCombination(u1.getX(), v1.getX(), u2.getX(), v2.getX(), u3.getX(), v3.getX()),
              MathArrays.linearCombination(u1.getY(), v1.getX(), u2.getY(), v2.getX(), u3.getY(), v3.getX()),
              MathArrays.linearCombination(u1.getZ(), v1.getX(), u2.getZ(), v2.getX(), u3.getZ(), v3.getX())
          },
          {
              MathArrays.linearCombination(u1.getX(), v1.getY(), u2.getX(), v2.getY(), u3.getX(), v3.getY()),
              MathArrays.linearCombination(u1.getY(), v1.getY(), u2.getY(), v2.getY(), u3.getY(), v3.getY()),
              MathArrays.linearCombination(u1.getZ(), v1.getY(), u2.getZ(), v2.getY(), u3.getZ(), v3.getY())
          },
          {
              MathArrays.linearCombination(u1.getX(), v1.getZ(), u2.getX(), v2.getZ(), u3.getX(), v3.getZ()),
              MathArrays.linearCombination(u1.getY(), v1.getZ(), u2.getY(), v2.getZ(), u3.getY(), v3.getZ()),
              MathArrays.linearCombination(u1.getZ(), v1.getZ(), u2.getZ(), v2.getZ(), u3.getZ(), v3.getZ())
          }
      };

      double[] quat = mat2quat(m);
      q0 = quat[0];
      q1 = quat[1];
      q2 = quat[2];
      q3 = quat[3];

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

    double[] quat = mat2quat(ort);
    q0 = quat[0];
    q1 = quat[1];
    q2 = quat[2];
    q3 = quat[3];

  }
