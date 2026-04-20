  private void removeUnusedSymbols(Collection<NameInfo> allNameInfo) {
    boolean changed = false;
    for (NameInfo nameInfo : allNameInfo) {
      if (!nameInfo.isReferenced()) {
        for (Symbol declaration : nameInfo.getDeclarations()) {
          boolean canRemove = false;

          if (specializationState == null) {
            canRemove = true;
          } else {
            Node specializableFunction =
              getSpecializableFunctionFromSymbol(declaration);

            if (specializableFunction != null) {
              specializationState.reportRemovedFunction(
                  specializableFunction, null);
              canRemove = true;
            }
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
        sb.append(' ');
        int lineno = getLineno();
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
  NodeMismatch checkTreeEqualsImpl(Node node2) {
    if (!isEquivalentTo(node2, false, false)) {
      return new NodeMismatch(this, node2);
    }

    NodeMismatch res = null;
    Node n, n2;
    for (n = first, n2 = node2.first;
         res == null && n != null;
         n = n.next, n2 = n2.next) {
      if (node2 == null) {
        throw new IllegalStateException();
      }
      res = n.checkTreeEqualsImpl(n2);
      if (res != null) {
      }
    }
    return res;
  }
  private static final String propToString(int propType) {
      switch (propType) {
        case LOCAL_BLOCK_PROP:   return "local_block";
        case OBJECT_IDS_PROP:    return "object_ids_prop";
        case CATCH_SCOPE_PROP:   return "catch_scope_prop";
        case LABEL_ID_PROP:      return "label_id_prop";
        case TARGET_PROP:        return "target";
        case BRACELESS_TYPE:     return "braceless_type";
        case BREAK_PROP:         return "break";
        case CONTINUE_PROP:      return "continue";
        case ENUM_PROP:          return "enum";
        case FUNCTION_PROP:      return "function";
        case TEMP_PROP:          return "temp";
        case LOCAL_PROP:         return "local";
        case CODEOFFSET_PROP:    return "codeoffset";
        case FIXUPS_PROP:        return "fixups";
        case VARS_PROP:          return "vars";
        case VAR_ARGS_NAME:      return "var_args_name";
        case USES_PROP:          return "uses";
        case REGEXP_PROP:        return "regexp";
        case CASES_PROP:         return "cases";
        case DEFAULT_PROP:       return "default";
        case CASEARRAY_PROP:     return "casearray";
        case SOURCENAME_PROP:    return "sourcename";
        case TYPE_PROP:          return "type";
        case SPECIAL_PROP_PROP:  return "special_prop";
        case LABEL_PROP:         return "label";
        case FINALLY_PROP:       return "finally";
        case LOCALCOUNT_PROP:    return "localcount";

        case TARGETBLOCK_PROP:   return "targetblock";
        case VARIABLE_PROP:      return "variable";
        case LASTUSE_PROP:       return "lastuse";
        case ISNUMBER_PROP:      return "isnumber";
        case DIRECTCALL_PROP:    return "directcall";

        case SPECIALCALL_PROP:   return "specialcall";
        case DEBUGSOURCE_PROP:   return "debugsource";

        case JSDOC_INFO_PROP:    return "jsdoc_info";

        case SKIP_INDEXES_PROP:  return "skip_indexes";
        case INCRDECR_PROP:      return "incrdecr";
        case MEMBER_TYPE_PROP:   return "member_type";
        case NAME_PROP:          return "name";
        case PARENTHESIZED_PROP: return "parenthesized";
        case QUOTED_PROP:        return "quoted";
        case OPT_ARG_NAME:       return "opt_arg";

        case SYNTHETIC_BLOCK_PROP: {
			String X = null;
			return "synthetic";
		}
        case EMPTY_BLOCK:        return "empty_block";
        case ORIGINALNAME_PROP:  return "originalname";
        case SIDE_EFFECT_FLAGS:  return "side_effect_flags";

        case IS_CONSTANT_NAME:   return "is_constant_name";
        case IS_OPTIONAL_PARAM:  return "is_optional_param";
        case IS_VAR_ARGS_PARAM:  return "is_var_args_param";
        case IS_NAMESPACE:       return "is_namespace";
        case IS_DISPATCHER:      return "is_dispatcher";
        case DIRECTIVES:         return "directives";
        case DIRECT_EVAL:        return "direct_eval";
        case FREE_CALL:          return "free_call";
        default:
          Kit.codeBug();
      }
      return null;
  }
