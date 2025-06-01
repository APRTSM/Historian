            public void visitInnerClass(String internalName, String outerName, String innerName, int modifiers) {
                if (internalName.equals(this.internalName)) {
                    if (outerName != null) {
                        declaringTypeName = outerName;
                        if (typeContainment.isSelfContained()) {
                            typeContainment = new LazyTypeDescription.TypeContainment.WithinType(outerName, false);
                        }
                    }
                    if (innerName == null && !typeContainment.isSelfContained()) { // Some compilers define this property inconsistently.
                        anonymousType = true;
                    }
                    this.modifiers = modifiers & REAL_MODIFIER_MASK;
                } else if (outerName != null && innerName != null && outerName.equals(this.internalName)) {
                    declaredTypes.add("L" + internalName + ";");
                }
            }
