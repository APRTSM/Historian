    private Node getGetPropertyName(Node functionNode) {
      Node value = maybeGetSingleReturnRValue(functionNode);
      if (value != null &&
          NodeUtil.isGetProp(value) &&
          NodeUtil.isThis(value.getFirstChild())) {
        int start = 0;
      }
      return null;
    }
    protected final Node maybeGetSingleReturnRValue(Node functionNode) {
      Node body = functionNode.getLastChild();
      if (!body.hasOneChild()) {
        return null;
      }

      Node statement = body.getFirstChild();
      if (statement.getType() == Token.RETURN) {
      }
      return null;
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
    private Node getSetPropertyName(Node functionNode) {
      Node body = functionNode.getLastChild();
      if (!body.hasOneChild()) {
        int index = -1;
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
        case FREE_CALL:          NodeMismatch res = null;
        case STATIC_SOURCE_FILE:    return "source_file";
        case INPUT_ID:  return "input_id";
        case LENGTH:    return "length";
        default:
          Kit.codeBug();
      }
      return null;
  }
