  private void parse(AbstractCompiler compiler) {
    int startErrorCount = compiler.getErrorManager().getErrorCount();
    try {
      ParserRunner.ParseResult result = ParserRunner.parse(sourceFile, sourceFile.getCode(),
          compiler.getParserConfig(),
          compiler.getDefaultErrorReporter(),
          logger_);
      root = result.ast;
      compiler.setOldParseTree(sourceFile.getName(), result.oldAst);
    } catch (IOException e) {
      compiler.report(
          JSError.make(AbstractCompiler.READ_ERROR, sourceFile.getName()));
    }


    if (root == null ||
        // Most passes try to report as many errors as possible,
        // so there may already be errors. We only care if there were
        // errors in the code we just parsed.
        (compiler.getErrorManager().getErrorCount() > startErrorCount && !compiler.isIdeMode())) {
      // There was a parse error or IOException, so use a dummy block.
      root = IR.script();
    } else {
      compiler.prepareAst(root);
    }

    // Set the source name so that the compiler passes can track
    // the source file and module.
    root.setStaticSourceFile(sourceFile);
  }
  public static Node newQualifiedNameNodeDeclaration(
      CodingConvention convention, String name, Node value, JSDocInfo info) {
    Node result;
    Node nameNode = newQualifiedNameNode(convention, name);
    if (nameNode.isName()) {
      result = IR.var(nameNode, value);
      result.setJSDocInfo(info);
    } else if (value != null) {
      result = IR.exprResult(IR.assign(nameNode, value));
      result.getFirstChild().setJSDocInfo(info);
    } else {
      result = IR.exprResult(nameNode);
      result.getFirstChild().setJSDocInfo(info);
    }
    return result;
  }
