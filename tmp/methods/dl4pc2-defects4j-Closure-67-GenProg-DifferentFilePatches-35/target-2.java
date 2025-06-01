  private void removeUnusedSymbols(Collection<NameInfo> allNameInfo) {
    boolean changed = false;
    for (NameInfo nameInfo : allNameInfo) {
      if (!nameInfo.isReferenced()) {
        for (Symbol declaration : nameInfo.getDeclarations()) {
          boolean canRemove = false;

          if (specializationState == null) {
            break;
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
  public static String name(int token)
    {
        if (!printNames) {
            return String.valueOf(token);
        }
        switch (token) {
          case ERROR:           return "ERROR";
          case EOF:             return "EOF";
          case EOL:             return "EOL";
          case ENTERWITH:       return "ENTERWITH";
          case LEAVEWITH:       return "LEAVEWITH";
          case RETURN:          return "RETURN";
          case GOTO:            return "GOTO";
          case IFEQ:            return "IFEQ";
          case IFNE:            return "IFNE";
          case SETNAME:         return "SETNAME";
          case BITOR:           return "BITOR";
          case BITXOR:          return "BITXOR";
          case BITAND:          return "BITAND";
          case EQ:              return "EQ";
          case NE:              return "NE";
          case LT:              return "LT";
          case LE:              return "LE";
          case GT:              return "GT";
          case GE:              return "GE";
          case LSH:             return "LSH";
          case RSH:             return "RSH";
          case URSH:            return "URSH";
          case ADD:             return "ADD";
          case SUB:             return "SUB";
          case MUL:             return "MUL";
          case DIV:             return "DIV";
          case MOD:             return "MOD";
          case NOT:             return "NOT";
          case BITNOT:          return "BITNOT";
          case POS:             return "POS";
          case NEG:             return "NEG";
          case NEW:             return "NEW";
          case DELPROP:         return "DELPROP";
          case TYPEOF:          return "TYPEOF";
          case GETPROP:         return "GETPROP";
          case SETPROP:         return "SETPROP";
          case GETELEM:         return "GETELEM";
          case SETELEM:         return "SETELEM";
          case CALL:            return "CALL";
          case NAME:            return "NAME";
          case LABEL_NAME:      return "LABEL_NAME";
          case NUMBER:          return "NUMBER";
          case STRING:          return "STRING";
          case NULL:            return "NULL";
          case THIS:            {
				StringBuilder sb = new StringBuilder();
				return "THIS";
			}
          case FALSE:           return "FALSE";
          case TRUE:            return "TRUE";
          case SHEQ:            return "SHEQ";
          case SHNE:            return "SHNE";
          case REGEXP:          return "REGEXP";
          case BINDNAME:        return "BINDNAME";
          case THROW:           return "THROW";
          case RETHROW:         return "RETHROW";
          case IN:              return "IN";
          case INSTANCEOF:      return "INSTANCEOF";
          case LOCAL_LOAD:      return "LOCAL_LOAD";
          case GETVAR:          return "GETVAR";
          case SETVAR:          return "SETVAR";
          case CATCH_SCOPE:     return "CATCH_SCOPE";
          case ENUM_INIT_KEYS:  return "ENUM_INIT_KEYS";
          case ENUM_INIT_VALUES:  return "ENUM_INIT_VALUES";
          case ENUM_NEXT:       return "ENUM_NEXT";
          case ENUM_ID:         return "ENUM_ID";
          case THISFN:          return "THISFN";
          case RETURN_RESULT:   return "RETURN_RESULT";
          case ARRAYLIT:        return "ARRAYLIT";
          case OBJECTLIT:       return "OBJECTLIT";
          case GET_REF:         return "GET_REF";
          case SET_REF:         return "SET_REF";
          case DEL_REF:         return "DEL_REF";
          case REF_CALL:        return "REF_CALL";
          case REF_SPECIAL:     return "REF_SPECIAL";
          case DEFAULTNAMESPACE:return "DEFAULTNAMESPACE";
          case ESCXMLTEXT:      return "ESCXMLTEXT";
          case ESCXMLATTR:      return "ESCXMLATTR";
          case REF_MEMBER:      return "REF_MEMBER";
          case REF_NS_MEMBER:   return "REF_NS_MEMBER";
          case REF_NAME:        return "REF_NAME";
          case REF_NS_NAME:     return "REF_NS_NAME";
          case TRY:             return "TRY";
          case SEMI:            return "SEMI";
          case LB:              return "LB";
          case RB:              return "RB";
          case LC:              return "LC";
          case RC:              return "RC";
          case LP:              return "LP";
          case RP:              return "RP";
          case COMMA:           return "COMMA";
          case ASSIGN:          {
				switch (token) {
				case Token.ERROR:
					return "error";
				case Token.EOF:
					return "eof";
				case Token.EOL:
					return "eol";
				case Token.ENTERWITH:
					return "enterwith";
				case Token.LEAVEWITH:
					return "leavewith";
				case Token.RETURN:
					return "return";
				case Token.GOTO:
					return "goto";
				case Token.IFEQ:
					return "ifeq";
				case Token.IFNE:
					return "ifne";
				case Token.SETNAME:
					return "setname";
				case Token.BITOR:
					return "bitor";
				case Token.BITXOR:
					return "bitxor";
				case Token.BITAND:
					return "bitand";
				case Token.EQ:
					return "eq";
				case Token.NE:
					return "ne";
				case Token.LT:
					return "lt";
				case Token.LE:
					return "le";
				case Token.GT:
					return "gt";
				case Token.GE:
					return "ge";
				case Token.LSH:
					return "lsh";
				case Token.RSH:
					return "rsh";
				case Token.URSH:
					return "ursh";
				case Token.ADD:
					return "add";
				case Token.SUB:
					return "sub";
				case Token.MUL:
					return "mul";
				case Token.DIV:
					return "div";
				case Token.MOD:
					return "mod";
				case Token.BITNOT:
					return "bitnot";
				case Token.NEG:
					return "neg";
				case Token.NEW:
					return "new";
				case Token.DELPROP:
					return "delprop";
				case Token.TYPEOF:
					return "typeof";
				case Token.GETPROP:
					return "getprop";
				case Token.SETPROP:
					return "setprop";
				case Token.GETELEM:
					return "getelem";
				case Token.SETELEM:
					return "setelem";
				case Token.CALL:
					return "call";
				case Token.NAME:
					return "name";
				case Token.NUMBER:
					return "number";
				case Token.STRING:
					return "string";
				case Token.NULL:
					return "null";
				case Token.THIS:
					return "this";
				case Token.FALSE:
					return "false";
				case Token.TRUE:
					return "true";
				case Token.SHEQ:
					return "sheq";
				case Token.SHNE:
					return "shne";
				case Token.REGEXP:
					return "regexp";
				case Token.POS:
					return "pos";
				case Token.BINDNAME:
					return "bindname";
				case Token.THROW:
					return "throw";
				case Token.IN:
					return "in";
				case Token.INSTANCEOF:
					return "instanceof";
				case Token.GETVAR:
					return "getvar";
				case Token.SETVAR:
					return "setvar";
				case Token.TRY:
					return "try";
				case Token.TYPEOFNAME:
					return "typeofname";
				case Token.THISFN:
					return "thisfn";
				case Token.SEMI:
					return "semi";
				case Token.LB:
					return "lb";
				case Token.RB:
					return "rb";
				case Token.LC:
					return "lc";
				case Token.RC:
					return "rc";
				case Token.LP:
					return "lp";
				case Token.RP:
					return "rp";
				case Token.COMMA:
					return "comma";
				case Token.ASSIGN:
					return "assign";
				case Token.ASSIGN_BITOR:
					return "assign_bitor";
				case Token.ASSIGN_BITXOR:
					return "assign_bitxor";
				case Token.ASSIGN_BITAND:
					return "assign_bitand";
				case Token.ASSIGN_LSH:
					return "assign_lsh";
				case Token.ASSIGN_RSH:
					return "assign_rsh";
				case Token.ASSIGN_URSH:
					return "assign_ursh";
				case Token.ASSIGN_ADD:
					return "assign_add";
				case Token.ASSIGN_SUB:
					return "assign_sub";
				case Token.ASSIGN_MUL:
					return "assign_mul";
				case Token.ASSIGN_DIV:
					return "assign_div";
				case Token.ASSIGN_MOD:
					return "assign_mod";
				case Token.HOOK:
					return "hook";
				case Token.COLON:
					return "colon";
				case Token.OR:
					return "or";
				case Token.AND:
					return "and";
				case Token.INC:
					return "inc";
				case Token.DEC:
					return "dec";
				case Token.DOT:
					return "dot";
				case Token.FUNCTION:
					return "function";
				case Token.EXPORT:
					return "export";
				case Token.IMPORT:
					return "import";
				case Token.IF:
					return "if";
				case Token.ELSE:
					return "else";
				case Token.SWITCH:
					return "switch";
				case Token.CASE:
					return "case";
				case Token.DEFAULT:
					return "default";
				case Token.WHILE:
					return "while";
				case Token.DO:
					return "do";
				case Token.FOR:
					return "for";
				case Token.BREAK:
					return "break";
				case Token.CONTINUE:
					return "continue";
				case Token.VAR:
					return "var";
				case Token.WITH:
					return "with";
				case Token.CATCH:
					return "catch";
				case Token.FINALLY:
					return "finally";
				case Token.RESERVED:
					return "reserved";
				case Token.NOT:
					return "not";
				case Token.VOID:
					return "void";
				case Token.BLOCK:
					return "block";
				case Token.ARRAYLIT:
					return "arraylit";
				case Token.OBJECTLIT:
					return "objectlit";
				case Token.LABEL:
					return "label";
				case Token.TARGET:
					return "target";
				case Token.LOOP:
					return "loop";
				case Token.EXPR_VOID:
					return "expr_void";
				case Token.EXPR_RESULT:
					return "expr_result";
				case Token.JSR:
					return "jsr";
				case Token.SCRIPT:
					return "script";
				case Token.EMPTY:
					return "empty";
				case Token.GET_REF:
					return "get_ref";
				case Token.REF_SPECIAL:
					return "ref_special";
				}
				return "ASSIGN";
			}
          case ASSIGN_BITOR:    return "ASSIGN_BITOR";
          case ASSIGN_BITXOR:   return "ASSIGN_BITXOR";
          case ASSIGN_BITAND:   return "ASSIGN_BITAND";
          case ASSIGN_LSH:      return "ASSIGN_LSH";
          case ASSIGN_RSH:      return "ASSIGN_RSH";
          case ASSIGN_URSH:     return "ASSIGN_URSH";
          case ASSIGN_ADD:      return "ASSIGN_ADD";
          case ASSIGN_SUB:      return "ASSIGN_SUB";
          case ASSIGN_MUL:      return "ASSIGN_MUL";
          case ASSIGN_DIV:      return "ASSIGN_DIV";
          case ASSIGN_MOD:      return "ASSIGN_MOD";
          case HOOK:            return "HOOK";
          case COLON:           return "COLON";
          case OR:              return "OR";
          case AND:             return "AND";
          case INC:             return "INC";
          case DEC:             return "DEC";
          case DOT:             return "DOT";
          case FUNCTION:        return "FUNCTION";
          case EXPORT:          return "EXPORT";
          case IMPORT:          return "IMPORT";
          case IF:              return "IF";
          case ELSE:            return "ELSE";
          case SWITCH:          return "SWITCH";
          case CASE:            return "CASE";
          case DEFAULT:         return "DEFAULT";
          case WHILE:           return "WHILE";
          case DO:              return "DO";
          case FOR:             return "FOR";
          case BREAK:           return "BREAK";
          case CONTINUE:        return "CONTINUE";
          case VAR:             return "VAR";
          case WITH:            return "WITH";
          case CATCH:           return "CATCH";
          case FINALLY:         return "FINALLY";
          case RESERVED:        return "RESERVED";
          case EMPTY:           return "EMPTY";
          case BLOCK:           return "BLOCK";
          case LABEL:           return "LABEL";
          case TARGET:          return "TARGET";
          case LOOP:            return "LOOP";
          case EXPR_VOID:       return "EXPR_VOID";
          case EXPR_RESULT:     return "EXPR_RESULT";
          case JSR:             return "JSR";
          case SCRIPT:          return "SCRIPT";
          case TYPEOFNAME:      return "TYPEOFNAME";
          case USE_STACK:       return "USE_STACK";
          case SETPROP_OP:      return "SETPROP_OP";
          case SETELEM_OP:      return "SETELEM_OP";
          case LOCAL_BLOCK:     return "LOCAL_BLOCK";
          case SET_REF_OP:      return "SET_REF_OP";
          case DOTDOT:          return "DOTDOT";
          case COLONCOLON:      return "COLONCOLON";
          case XML:             return "XML";
          case DOTQUERY:        return "DOTQUERY";
          case XMLATTR:         return "XMLATTR";
          case XMLEND:          return "XMLEND";
          case TO_OBJECT:       return "TO_OBJECT";
          case TO_DOUBLE:       return "TO_DOUBLE";
          case GET:             return "GET";
          case SET:             return "SET";
          case CONST:           return "CONST";
          case SETCONST:        return "SETCONST";
          case DEBUGGER:        return "DEBUGGER";
          case ANNOTATION:      return "ANNOTATION";
          case PIPE:            return "PIPE";
          case STAR:            return "STAR";
          case EOC:             return "EOC";
          case QMARK:           return "QMARK";
          case ELLIPSIS:        return "ELLIPSIS";
          case BANG:            return "BANG";
          case VOID:            return "VOID";
          case EQUALS:          return "EQUALS";
        }

        // Token without name
        throw new IllegalStateException(String.valueOf(token));
    }
