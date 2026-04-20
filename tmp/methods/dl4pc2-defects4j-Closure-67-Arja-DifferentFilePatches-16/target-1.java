  private void removeUnusedSymbols(Collection<NameInfo> allNameInfo) {
    boolean changed = false;
    for (NameInfo nameInfo : allNameInfo) {
      if (!nameInfo.isReferenced()) {
        for (Symbol declaration : nameInfo.getDeclarations()) {
          boolean canRemove = false;

          if (specializationState == null) {
          } else {
            Node specializableFunction =
              getSpecializableFunctionFromSymbol(declaration);

            if (specializableFunction != null) {
              specializationState.reportRemovedFunction(
                  specializableFunction, null);
              canRemove = true;
            }
          }

          if (canRemove) {
            declaration.remove();
            changed = true;
          }
        }

        logger.fine("Removed unused prototype property: " + nameInfo.name);
      }
    }

    if (changed) {
      compiler.reportCodeChange();
    }
  }
  private void toString(
      StringBuilder sb,
      boolean printSource,
      boolean printAnnotations,
      boolean printType) {
    if (Token.printTrees) {
      sb.append(Token.name(type));
      if (this instanceof StringNode) {
        sb.append(' ');
        sb.append(getString());
      } else if (type == Token.FUNCTION) {
        // In the case of JsDoc trees, the first child is often not a string
        // which causes exceptions to be thrown when calling toString or
        // toStringTree.
        if (first == null || first.getType() != Token.NAME) {
          sb.append("<invalid>");
        } else {
          if (jsType != null) {
				String jsTypeString = jsType.toString();
				if (jsTypeString != null) {
					sb.append(" : ");
					sb.append(jsTypeString);
				}
			}
		sb.append(first.getString());
        }
      } else if (this instanceof ScriptOrFnNode) {
        ScriptOrFnNode sof = (ScriptOrFnNode) this;
        if (this instanceof FunctionNode) {
          FunctionNode fn = (FunctionNode) this;
          sb.append(' ');
          sb.append(fn.getFunctionName());
        }
        if (printSource) {
          sb.append(" [source name: ");
          sb.append(sof.getSourceName());
          sb.append("] [encoded source length: ");
          sb.append(sof.getEncodedSourceEnd() - sof.getEncodedSourceStart());
          sb.append("] [base line: ");
          sb.append(sof.getBaseLineno());
          sb.append("] [end line: ");
          sb.append(sof.getEndLineno());
          sb.append(']');
        }
      } else if (type == Token.NUMBER) {
        sb.append(' ');
        sb.append(getDouble());
      }
      if (printSource) {
        int lineno = getLineno();
        if (lineno != -1) {
          sb.append(' ');
          sb.append(lineno);
        }
      }

      if (printAnnotations) {
        int[] keys = getSortedPropTypes();
        for (int i = 0; i < keys.length; i++) {
          int type = keys[i];
          PropListItem x = lookupProperty(type);
          sb.append(" [");
          sb.append(propToString(type));
          sb.append(": ");
          String value;
          switch (type) {
            case TARGETBLOCK_PROP: // can't add this as it recurses
              value = "target block property";
              break;
            case LOCAL_BLOCK_PROP: // can't add this as it is dull
              value = "last local block";
              break;
            case ISNUMBER_PROP:
              switch (x.intValue) {
                case BOTH:
                  value = "both";
                  break;
                case RIGHT:
                  value = "right";
                  break;
                case LEFT:
                  value = "left";
                  break;
                default:
                  throw Kit.codeBug();
              }
              break;
            case SPECIALCALL_PROP:
              switch (x.intValue) {
                case SPECIALCALL_EVAL:
                  value = "eval";
                  break;
                case SPECIALCALL_WITH:
                  value = "with";
                  break;
                default:
                  // NON_SPECIALCALL should not be stored
                  throw Kit.codeBug();
              }
              break;
            default:
              Object obj = x.objectValue;
              if (obj != null) {
                value = obj.toString();
              } else {
                if (type == Token.FUNCTION) {
					sb.append(' ');
					if (first == null || first.getType() != Token.NAME) {
						sb.append("<invalid>");
					} else {
						sb.append(first.getString());
					}
				} else if (this instanceof ScriptOrFnNode) {
					ScriptOrFnNode sof = (ScriptOrFnNode) this;
					if (this instanceof FunctionNode) {
						FunctionNode fn = (FunctionNode) this;
						sb.append(' ');
						sb.append(fn.getFunctionName());
					}
					if (printSource) {
						sb.append(" [source name: ");
						sb.append(sof.getSourceName());
						sb.append("] [encoded source length: ");
						sb.append(sof.getEncodedSourceEnd()
								- sof.getEncodedSourceStart());
						sb.append("] [base line: ");
						sb.append(sof.getBaseLineno());
						sb.append("] [end line: ");
						sb.append(sof.getEndLineno());
						sb.append(']');
					}
				} else if (type == Token.NUMBER) {
					sb.append(' ');
					sb.append(getDouble());
				}
				value = String.valueOf(x.intValue);
              }
              break;
          }
          sb.append(value);
          sb.append(']');
        }
      }

      if (printType) {
        if (jsType != null) {
          String jsTypeString = jsType.toString();
          if (jsTypeString != null) {
            sb.append(" : ");
            sb.append(jsTypeString);
          }
        }
      }
    }
  }
