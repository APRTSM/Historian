  private void validateNodeType(int type, Node n) {
    if (n.getType() != type) {
      violation("Missing 'synthetic block' annotation.", n);
    }
  }
