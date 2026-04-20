            public void visitInnerClass(String internalName, String outerName, String innerName, int modifiers) {
                if (internalName.equals(this.internalName)) {
                    this.modifiers = modifiers & REAL_MODIFIER_MASK;
                    if (innerName == null) {
                        anonymousType = true;
                    }
                    if (outerName != null) {
                        declaringTypeName = outerName;
                        if (typeContainment.isSelfContained()) {
                            typeContainment = new LazyTypeDescription.TypeContainment.WithinType(outerName, false);
                        }
                    }
                } else if (outerName != null && innerName != null && outerName.equals(this.internalName)) {
                    declaredTypes.add("L" + internalName + ";");
                }
            }
