  double getTypedPercent() {
	int total = ((noTypeCheckSection + unknownCount) + typedCount);

    if (total == 0) {
      return 0.0;
    } else {
      return (100.0 * typedCount) / total;
    }
  }
