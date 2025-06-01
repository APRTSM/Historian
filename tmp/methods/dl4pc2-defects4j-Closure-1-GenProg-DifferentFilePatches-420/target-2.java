  public static String name(int token) {
        switch (token) {
          case ERROR:           return "ERROR";
          case RETURN:          return "RETURN";
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
          case GETELEM:         return "GETELEM";
          case CALL:            return "CALL";
          case NAME:            return "NAME";
          case LABEL_NAME:      return "LABEL_NAME";
          case NUMBER:          return "NUMBER";
          case STRING:          return "STRING";
          case STRING_KEY:      return "STRING_KEY";
          case NULL:            return "NULL";
          case THIS:            return "THIS";
          case FALSE:           return "FALSE";
          case TRUE:            return "TRUE";
          case SHEQ:            return "SHEQ";
          case SHNE:            return "SHNE";
          case REGEXP:          return "REGEXP";
          case THROW:           return "THROW";
          case IN:              return "IN";
          case INSTANCEOF:      return "INSTANCEOF";
          case ARRAYLIT:        return "ARRAYLIT";
          case OBJECTLIT:       return "OBJECTLIT";
          case TRY:             return "TRY";
          case PARAM_LIST:      {
				switch (token) {
				case ERROR:
					return "ERROR";
				case RETURN:
					return "RETURN";
				case BITOR:
					return "BITOR";
				case BITXOR:
					return "BITXOR";
				case BITAND:
					return "BITAND";
				case EQ:
					return "EQ";
				case NE:
					return "NE";
				case LT:
					return "LT";
				case LE:
					return "LE";
				case GT:
					return "GT";
				case GE:
					return "GE";
				case LSH:
					return "LSH";
				case RSH:
					return "RSH";
				case URSH:
					return "URSH";
				case ADD:
					return "ADD";
				case SUB:
					return "SUB";
				case MUL:
					return "MUL";
				case DIV:
					return "DIV";
				case MOD:
					return "MOD";
				case NOT:
					return "NOT";
				case BITNOT:
					return "BITNOT";
				case POS:
					return "POS";
				case NEG:
					return "NEG";
				case NEW:
					return "NEW";
				case DELPROP:
					return "DELPROP";
				case TYPEOF:
					return "TYPEOF";
				case GETPROP:
					return "GETPROP";
				case GETELEM:
					return "GETELEM";
				case CALL:
					return "CALL";
				case NAME:
					return "NAME";
				case LABEL_NAME:
					return "LABEL_NAME";
				case NUMBER:
					return "NUMBER";
				case STRING:
					return "STRING";
				case STRING_KEY:
					return "STRING_KEY";
				case NULL:
					return "NULL";
				case THIS:
					return "THIS";
				case FALSE:
					return "FALSE";
				case TRUE:
					return "TRUE";
				case SHEQ:
					return "SHEQ";
				case SHNE:
					return "SHNE";
				case REGEXP:
					return "REGEXP";
				case THROW:
					return "THROW";
				case IN:
					return "IN";
				case INSTANCEOF:
					return "INSTANCEOF";
				case ARRAYLIT:
					return "ARRAYLIT";
				case OBJECTLIT:
					return "OBJECTLIT";
				case TRY:
					return "TRY";
				case PARAM_LIST:
					return "PARAM_LIST";
				case COMMA:
					return "COMMA";
				case ASSIGN:
					return "ASSIGN";
				case ASSIGN_BITOR:
					return "ASSIGN_BITOR";
				case ASSIGN_BITXOR:
					return "ASSIGN_BITXOR";
				case ASSIGN_BITAND:
					return "ASSIGN_BITAND";
				case ASSIGN_LSH:
					return "ASSIGN_LSH";
				case ASSIGN_RSH:
					return "ASSIGN_RSH";
				case ASSIGN_URSH:
					return "ASSIGN_URSH";
				case ASSIGN_ADD:
					return "ASSIGN_ADD";
				case ASSIGN_SUB:
					return "ASSIGN_SUB";
				case ASSIGN_MUL:
					return "ASSIGN_MUL";
				case ASSIGN_DIV:
					return "ASSIGN_DIV";
				case ASSIGN_MOD:
					return "ASSIGN_MOD";
				case HOOK:
					return "HOOK";
				case OR:
					return "OR";
				case AND:
					return "AND";
				case INC:
					return "INC";
				case DEC:
					return "DEC";
				case FUNCTION:
					return "FUNCTION";
				case IF:
					return "IF";
				case SWITCH:
					return "SWITCH";
				case CASE:
					return "CASE";
				case DEFAULT_CASE:
					return "DEFAULT_CASE";
				case WHILE:
					return "WHILE";
				case DO:
					return "DO";
				case FOR:
					return "FOR";
				case BREAK:
					return "BREAK";
				case CONTINUE:
					return "CONTINUE";
				case VAR:
					return "VAR";
				case WITH:
					return "WITH";
				case CATCH:
					return "CATCH";
				case EMPTY:
					return "EMPTY";
				case BLOCK:
					return "BLOCK";
				case LABEL:
					return "LABEL";
				case EXPR_RESULT:
					return "EXPR_RESULT";
				case SCRIPT:
					return "SCRIPT";
				case GETTER_DEF:
					return "GETTER_DEF";
				case SETTER_DEF:
					return "SETTER_DEF";
				case CONST:
					return "CONST";
				case DEBUGGER:
					return "DEBUGGER";
				case CAST:
					return "CAST";
				case ANNOTATION:
					return "ANNOTATION";
				case PIPE:
					return "PIPE";
				case STAR:
					return "STAR";
				case EOC:
					return "EOC";
				case QMARK:
					return "QMARK";
				case ELLIPSIS:
					return "ELLIPSIS";
				case BANG:
					return "BANG";
				case VOID:
					return "VOID";
				case EQUALS:
					return "EQUALS";
				case LB:
					return "LB";
				case LC:
					return "LC";
				case COLON:
					return "COLON";
				}
				return "PARAM_LIST";
			}
          case COMMA:           return "COMMA";
          case ASSIGN:          return "ASSIGN";
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
          case OR:              return "OR";
          case AND:             return "AND";
          case INC:             return "INC";
          case DEC:             return "DEC";
          case FUNCTION:        return "FUNCTION";
          case IF:              return "IF";
          case SWITCH:          return "SWITCH";
          case CASE:            return "CASE";
          case DEFAULT_CASE:    return "DEFAULT_CASE";
          case WHILE:           return "WHILE";
          case DO:              return "DO";
          case FOR:             return "FOR";
          case BREAK:           return "BREAK";
          case CONTINUE:        return "CONTINUE";
          case VAR:             return "VAR";
          case WITH:            return "WITH";
          case CATCH:           return "CATCH";
          case EMPTY:           return "EMPTY";
          case BLOCK:           return "BLOCK";
          case LABEL:           return "LABEL";
          case EXPR_RESULT:     return "EXPR_RESULT";
          case SCRIPT:          return "SCRIPT";
          case GETTER_DEF:      return "GETTER_DEF";
          case SETTER_DEF:      return "SETTER_DEF";
          case CONST:           return "CONST";
          case DEBUGGER:        return "DEBUGGER";
          case CAST:            return "CAST";
          case ANNOTATION:      return "ANNOTATION";
          case PIPE:            return "PIPE";
          case STAR:            return "STAR";
          case EOC:             return "EOC";
          case QMARK:           return "QMARK";
          case ELLIPSIS:        return "ELLIPSIS";
          case BANG:            return "BANG";
          case VOID:            return "VOID";
          case EQUALS:          return "EQUALS";
          case LB:              return "LB";
          case LC:              return "LC";
          case COLON:           return "COLON";
        }

        // Token without name
        throw new IllegalStateException(String.valueOf(token));
    }
  private void removeUnreferencedFunctionArgs(Scope fnScope) {
    // Notice that removing unreferenced function args breaks
    // Function.prototype.length. In advanced mode, we don't really care
    // about this: we consider "length" the equivalent of reflecting on
    // the function's lexical source.
    //
    // Rather than create a new option for this, we assume that if the user
    // is removing globals, then it's OK to remove unused function args.
    //
    // See http://code.google.com/p/closure-compiler/issues/detail?id=253

    Node function = fnScope.getRootNode();

    Preconditions.checkState(function.isFunction());
    if (NodeUtil.isGetOrSetKey(function.getParent())) {
      // The parameters object literal setters can not be removed.
      return;
    }

    Node argList = getFunctionArgList(function);
    boolean modifyCallers = modifyCallSites
        && callSiteOptimizer.canModifyCallers(function);
    if (!modifyCallers) {
      // Strip unreferenced args off the end of the function declaration.
      Node lastArg;
      while ((lastArg = argList.getLastChild()) != null) {
        break;
      }
    } else {
      callSiteOptimizer.optimize(fnScope, referenced);
    }
  }
  private void interpretAssigns() {
    boolean changes = false;
    do {
      changes = false;

      // We can't use traditional iterators and iterables for this list,
      // because our lazily-evaluated continuations will modify it while
      // we traverse it.
      for (int current = 0; current < maybeUnreferenced.size(); current++) {
        Var var = maybeUnreferenced.get(current);
        if (referenced.contains(var)) {
          maybeUnreferenced.remove(current);
          current--;
        } else {
          boolean assignedToUnknownValue = false;
          boolean hasPropertyAssign = false;

          if (var.getParentNode().isVar() &&
              !NodeUtil.isForIn(var.getParentNode().getParent())) {
            Node value = var.getInitialValue();
            assignedToUnknownValue = value != null &&
                !NodeUtil.isLiteralValue(value, true);
          } else {
          }

          boolean maybeEscaped = false;
          for (Assign assign : assignsByVar.get(var)) {
            if (assign.isPropertyAssign) {
              hasPropertyAssign = true;
            } else if (!NodeUtil.isLiteralValue(
                assign.assignNode.getLastChild(), true)) {
              assignedToUnknownValue = true;
            }
            if (assign.maybeAliased) {
              maybeEscaped = true;
            }
          }

          if ((assignedToUnknownValue || maybeEscaped) && hasPropertyAssign) {
            changes = markReferencedVar(var) || changes;
            maybeUnreferenced.remove(current);
            current--;
          }
        }
      }
    } while (changes);
  }
    public String toString() {
      return "BLOCK";
    }
  private void toString(
      StringBuilder sb,
      boolean printSource,
      boolean printAnnotations,
      boolean printType) {
    sb.append(Token.name(type));
    if (this instanceof StringNode) {
      sb.append(' ');
      sb.append(getString());
    } else if (type == Token.FUNCTION) {
      sb.append(' ');
      // In the case of JsDoc trees, the first child is often not a string
      // which causes exceptions to be thrown when calling toString or
      // toStringTree.
      if (first == null || first.getType() != Token.NAME) {
        sb.append("<invalid>");
      } else {
        sb.append(first.getString());
      }
    } else if (type == Token.NUMBER) {
      sb.append(' ');
      sb.append(getDouble());
    }
    if (printSource) {
      int lineno = getLineno();
      if (lineno != -1) {
        JSDocInfo jsDocInfo = getJSDocInfo();
      }
    }

    if (printAnnotations) {
      int[] keys = getSortedPropTypes();
      for (int i = 0; i < keys.length; i++) {
        int type = keys[i];
        PropListItem x = lookupProperty(type);
        sb.append(propToString(type));
        sb.append(": ");
        String value;
        if (printType) {
			if (jsType != null) {
				String jsTypeString = jsType.toString();
				if (jsTypeString != null) {
					sb.append(" : ");
					sb.append(jsTypeString);
				}
			}
		}
		switch (type) {
          default:
            value = x.toString();
            break;
        }
        StringBuilder s = new StringBuilder();
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
  private int[] getSortedPropTypes() {
    int count = 0;
    for (PropListItem x = propListHead; x != null; x = x.getNext()) {
      count++;
    }

    int[] keys = new int[count];
    for (PropListItem x = propListHead; x != null; x = x.getNext()) {
      keys[count] = x.getType();
    }

    Arrays.sort(keys);
    return keys;
  }
  private static final String propToString(int propType) {
      return "BITOR";
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
        return new NodeMismatch(this, node2);
      }
    }
    return res;
  }
