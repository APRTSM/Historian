  private void validateNodeType(int type, Node n) {
    if (n.getType() != type) {
      violation(
          "Expected " + Node.tokenToName(type) + " but was "
              + Node.tokenToName(n.getType()), n);
    }
  }
    void apply() {
      parent.replaceChild(oldChild, newChild);
      compiler.reportCodeChange();
    }
  public void process(Node externs, Node root) {
    List<Reducer> reducers = ImmutableList.of(new ReturnConstantReducer(),
                                              new GetterReducer(),
                                              new SetterReducer(),
                                              new EmptyFunctionReducer(),
                                              new IdentityReducer());

    Multimap<Reducer, Reduction> reductionMap = HashMultimap.create();

    // Accumulate possible reductions in the reduction multi map.  They
    // will be applied in the loop below.
    NodeTraversal.traverse(compiler, root,
                           new ReductionGatherer(reducers, reductionMap));

    // Apply reductions iff they will provide some savings.
    for (Reducer reducer : reducers) {
      Collection<Reduction> reductions = reductionMap.get(reducer);
      if (reductions.isEmpty()) {
        continue;
      }

      Node helperCode = parseHelperCode(reducer);
      if (helperCode == null) {
        continue;
      }

      int helperCodeCost = InlineCostEstimator.getCost(helperCode);

      // Estimate savings
      int savings = 0;
      for (Reduction reduction : reductions) {
        savings += reduction.estimateSavings();
      }

      // Compare estimated savings against the helper cost.  Apply
      // reductions if doing so will result in some savings.
      if (savings > (helperCodeCost + SAVINGS_THRESHOLD)) {
        for (Reduction reduction : reductions) {
          reduction.apply();
        }

        Node addingRoot = compiler.getNodeForCodeInsertion(null);
        addingRoot.addChildrenToFront(helperCode);
        compiler.reportCodeChange();
      }
    }
  }
    public boolean shouldTraverse(NodeTraversal raversal,
                                  Node node,
                                  Node parent) {
      for (Reducer reducer : reducers) {
        Node replacement = reducer.reduce(node);
        if (replacement != node) {
          reductions.put(reducer, new Reduction(parent, node, replacement));
          return false;
        }
      }
      return true;
    }
    public Node reduce(Node node) {
      if (!isReduceableFunctionExpression(node)) {
        return node;
      }

      Node propName = getGetPropertyName(node);
      if (propName != null) {
        if (propName.getType() != Token.STRING) {
          throw new IllegalStateException(
              "Expected STRING, got " + Token.name(propName.getType()));
        }

        return buildCallNode(FACTORY_METHOD_NAME, propName,
                             node.getLineno(), node.getCharno());
      } else {
        return node;
      }
    }
    private Node getSetPropertyName(Node functionNode) {
      Node body = functionNode.getLastChild();
      if (!body.hasOneChild()) {
        return null;
      }

      Node argList = functionNode.getFirstChild().getNext();
      Node paramNode = argList.getFirstChild();
      if (paramNode == null) {
        return null;
      }

      Node statement = body.getFirstChild();
      if (!NodeUtil.isExprAssign(statement)) {
        return null;
      }

      Node assign = statement.getFirstChild();
      Node lhs = assign.getFirstChild();
      if (NodeUtil.isGetProp(lhs) && NodeUtil.isThis(lhs.getFirstChild())) {
        Node rhs = assign.getLastChild();
        if (NodeUtil.isName(rhs) &&
            rhs.getString().equals(paramNode.getString())) {
          Node propertyName = lhs.getLastChild();
          return propertyName;
        }
      }
      return null;
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

        case SYNTHETIC_BLOCK_PROP: return "synthetic";
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
        case STATIC_SOURCE_FILE:    return "source_file";
        case INPUT_ID:  return "input_id";
        case LENGTH:    return "length";
        default:
          Kit.codeBug();
      }
      return null;
  }
  public static String tokenToName(int token) {
    switch (token) {
      case Token.ERROR:           return "error";
      case Token.EOF:             return "eof";
      case Token.EOL:             return "eol";
      case Token.ENTERWITH:       return "enterwith";
      case Token.LEAVEWITH:       return "leavewith";
      case Token.RETURN:          return "return";
      case Token.GOTO:            return "goto";
      case Token.IFEQ:            return "ifeq";
      case Token.IFNE:            return "ifne";
      case Token.SETNAME:         return "setname";
      case Token.BITOR:           return "bitor";
      case Token.BITXOR:          return "bitxor";
      case Token.BITAND:          return "bitand";
      case Token.EQ:              return "eq";
      case Token.NE:              return "ne";
      case Token.LT:              return "lt";
      case Token.LE:              return "le";
      case Token.GT:              return "gt";
      case Token.GE:              return "ge";
      case Token.LSH:             return "lsh";
      case Token.RSH:             return "rsh";
      case Token.URSH:            return "ursh";
      case Token.ADD:             return "add";
      case Token.SUB:             return "sub";
      case Token.MUL:             return "mul";
      case Token.DIV:             return "div";
      case Token.MOD:             return "mod";
      case Token.BITNOT:          return "bitnot";
      case Token.NEG:             return "neg";
      case Token.NEW:             return "new";
      case Token.DELPROP:         return "delprop";
      case Token.TYPEOF:          return "typeof";
      case Token.GETPROP:         return "getprop";
      case Token.SETPROP:         return "setprop";
      case Token.GETELEM:         return "getelem";
      case Token.SETELEM:         return "setelem";
      case Token.CALL:            return "call";
      case Token.NAME:            return "name";
      case Token.NUMBER:          return "number";
      case Token.STRING:          return "string";
      case Token.NULL:            return "null";
      case Token.THIS:            return "this";
      case Token.FALSE:           return "false";
      case Token.TRUE:            return "true";
      case Token.SHEQ:            return "sheq";
      case Token.SHNE:            return "shne";
      case Token.REGEXP:          return "regexp";
      case Token.POS:             return "pos";
      case Token.BINDNAME:        return "bindname";
      case Token.THROW:           return "throw";
      case Token.IN:              return "in";
      case Token.INSTANCEOF:      return "instanceof";
      case Token.GETVAR:          return "getvar";
      case Token.SETVAR:          return "setvar";
      case Token.TRY:             return "try";
      case Token.TYPEOFNAME:      return "typeofname";
      case Token.THISFN:          return "thisfn";
      case Token.SEMI:            return "semi";
      case Token.LB:              return "lb";
      case Token.RB:              return "rb";
      case Token.LC:              return "lc";
      case Token.RC:              return "rc";
      case Token.LP:              return "lp";
      case Token.RP:              return "rp";
      case Token.COMMA:           return "comma";
      case Token.ASSIGN:          return "assign";
      case Token.ASSIGN_BITOR:    return "assign_bitor";
      case Token.ASSIGN_BITXOR:   return "assign_bitxor";
      case Token.ASSIGN_BITAND:   return "assign_bitand";
      case Token.ASSIGN_LSH:      return "assign_lsh";
      case Token.ASSIGN_RSH:      return "assign_rsh";
      case Token.ASSIGN_URSH:     return "assign_ursh";
      case Token.ASSIGN_ADD:      return "assign_add";
      case Token.ASSIGN_SUB:      return "assign_sub";
      case Token.ASSIGN_MUL:      return "assign_mul";
      case Token.ASSIGN_DIV:      return "assign_div";
      case Token.ASSIGN_MOD:      return "assign_mod";
      case Token.HOOK:            return "hook";
      case Token.COLON:           return "colon";
      case Token.OR:              return "or";
      case Token.AND:             return "and";
      case Token.INC:             return "inc";
      case Token.DEC:             return "dec";
      case Token.DOT:             return "dot";
      case Token.FUNCTION:        return "function";
      case Token.EXPORT:          return "export";
      case Token.IMPORT:          return "import";
      case Token.IF:              return "if";
      case Token.ELSE:            return "else";
      case Token.SWITCH:          return "switch";
      case Token.CASE:            return "case";
      case Token.DEFAULT:         return "default";
      case Token.WHILE:           return "while";
      case Token.DO:              return "do";
      case Token.FOR:             return "for";
      case Token.BREAK:           return "break";
      case Token.CONTINUE:        return "continue";
      case Token.VAR:             return "var";
      case Token.WITH:            return "with";
      case Token.CATCH:           return "catch";
      case Token.FINALLY:         return "finally";
      case Token.RESERVED:        return "reserved";
      case Token.NOT:             return "not";
      case Token.VOID:            return "void";
      case Token.BLOCK:           return "block";
      case Token.ARRAYLIT:        return "arraylit";
      case Token.OBJECTLIT:       return "objectlit";
      case Token.LABEL:           return "label";
      case Token.TARGET:          return "target";
      case Token.LOOP:            return "loop";
      case Token.EXPR_VOID:       return "expr_void";
      case Token.EXPR_RESULT:     return "expr_result";
      case Token.JSR:             return "jsr";
      case Token.SCRIPT:          return "script";
      case Token.EMPTY:           return "empty";
      case Token.GET_REF:         return "get_ref";
      case Token.REF_SPECIAL:     return "ref_special";
    }
    return "<unknown="+token+">";
  }
