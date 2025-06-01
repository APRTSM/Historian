    private Integer getPivotRow(SimplexTableau tableau, final int col) {
        // create a list of all the rows that tie for the lowest score in the minimum ratio test
        List<Integer> minRatioPositions = new ArrayList<Integer>();
        double minRatio = Double.MAX_VALUE;
        for (int i = tableau.getNumObjectiveFunctions(); i < tableau.getHeight(); i++) {
            final double rhs = tableau.getEntry(i, tableau.getWidth() - 1);
            final double entry = tableau.getEntry(i, col);
            if (MathUtils.compareTo(entry, 0, epsilon) > 0) {
                final double ratio = rhs / entry;
                if (MathUtils.equals(ratio, minRatio, epsilon)) {
                    minRatioPositions.add(i);
                } else if (ratio < minRatio) {
                    minRatio = ratio;
                    minRatioPositions = new ArrayList<Integer>();
                    minRatioPositions.add(i);
                }
            }
        }

        if (minRatioPositions.size() == 0) {
          return null;
        } else if (minRatioPositions.size() > 1) {
          // there's a degeneracy as indicated by a tie in the minimum ratio test
          // check if there's an artificial variable that can be forced out of the basis
          for (Integer row : minRatioPositions) {
            for (int i = 0; i < tableau.getNumArtificialVariables(); i++) {
              int column = i + tableau.getArtificialVariableOffset();
              if (MathUtils.equals(tableau.getEntry(row, column), 1, epsilon) &&
                  row.equals(tableau.getBasicRow(column))) {
                return row;
              }
            }
          }
        }
        return minRatioPositions.get(0);
    }
