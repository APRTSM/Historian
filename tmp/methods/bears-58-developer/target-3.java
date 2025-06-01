	public <T> void visitCtClass(CtClass<T> ctClass) {
		context.pushCurrentThis(ctClass);
		if (ctClass.getSimpleName() != null && !CtType.NAME_UNKNOWN.equals(ctClass.getSimpleName()) && !ctClass.isAnonymous()) {
			visitCtType(ctClass);
			if (ctClass.isLocalType()) {
				printer.write("class " + ctClass.getSimpleName().replaceAll("^[0-9]*", ""));
			} else {
				printer.write("class " + ctClass.getSimpleName());
			}

			elementPrinterHelper.writeFormalTypeParameters(ctClass);
			elementPrinterHelper.writeExtendsClause(ctClass);
			elementPrinterHelper.writeImplementsClause(ctClass);
		}
		// lst.addAll(elementPrinterHelper.getComments(ctClass, CommentOffset.INSIDE));
		printer.write(" {").incTab();
		elementPrinterHelper.writeElementList(ctClass.getTypeMembers());
		printer.adjustEndPosition(ctClass).decTab().writeTabs().write("}");
		context.popCurrentThis();
	}
	public <T> void visitCtInvocation(CtInvocation<T> invocation) {
		enterCtStatement(invocation);
		enterCtExpression(invocation);
		if (invocation.getExecutable().isConstructor()) {
			// It's a constructor (super or this)
			elementPrinterHelper.writeActualTypeArguments(invocation.getExecutable());
			CtType<?> parentType;
			try {
				parentType = invocation.getParent(CtType.class);
			} catch (ParentNotInitializedException e) {
				parentType = null;
			}
			if (parentType != null && parentType.getQualifiedName() != null && parentType.getQualifiedName().equals(invocation.getExecutable().getDeclaringType().getQualifiedName())) {
				printer.write("this");
			} else {
				printer.snapshotLength();
				scan(invocation.getTarget());
				if (printer.hasNewContent()) {
					printer.write(".");
				}
				printer.write("super");
			}
		} else {
			// It's a method invocation
			printer.snapshotLength();
			try (Writable _context = context.modify()) {
				if (invocation.getTarget() instanceof CtTypeAccess) {
					_context.ignoreGenerics(true);
				}
				scan(invocation.getTarget());
			}
			if (printer.hasNewContent()) {
				printer.write(".");
			}

			elementPrinterHelper.writeActualTypeArguments(invocation);
			if (env.isPreserveLineNumbers()) {
				printer.adjustStartPosition(invocation);
			}
			printer.write(invocation.getExecutable().getSimpleName());
		}
		printer.write("(");
		boolean remove = false;
		for (CtExpression<?> e : invocation.getArguments()) {
			scan(e);
			printer.write(", ");
			remove = true;
		}
		if (remove) {
			printer.removeLastChar();
		}
		printer.write(")");
		exitCtExpression(invocation);
	}
	public <T> void visitCtCatchVariable(CtCatchVariable<T> catchVariable) {
		if (env.isPreserveLineNumbers()) {
			printer.adjustStartPosition(catchVariable);
		}
		elementPrinterHelper.writeModifiers(catchVariable);
		scan(catchVariable.getType());
		printer.write(" ");
		printer.write(catchVariable.getSimpleName());
	}
	public DefaultJavaPrettyPrinter scan(CtElement e) {
		if (e != null) {
			context.elementStack.push(e);
			if (env.isPreserveLineNumbers()) {
				if (!(e instanceof CtNamedElement)) {
					printer.adjustStartPosition(e);
				}
			}
			e.accept(this);
			context.elementStack.pop();
		}
		return this;
	}
	public void calculate(CompilationUnit sourceCompilationUnit, List<CtType<?>> types) {
		this.sourceCompilationUnit = sourceCompilationUnit;

		// reset the importsContext to avoid errors with multiple CU
		if (env.isAutoImports()) {
			this.importsContext = new ImportScannerImpl();
		} else {
			this.importsContext = new MinimalImportScanner();
		}

		Set<CtReference> imports = new HashSet<>();
		for (CtType<?> t : types) {
			imports.addAll(computeImports(t));
		}
		elementPrinterHelper.writeHeader(types, imports);
		for (CtType<?> t : types) {
			scan(t);
			if (!env.isPreserveLineNumbers()) {
				// saving lines and chars
				printer.writeln().writeln().writeTabs();
			} else {
				printer.adjustEndPosition(t);
			}
		}
	}
	public <T> void visitCtLocalVariable(CtLocalVariable<T> localVariable) {
		if (!context.noTypeDecl()) {
			enterCtStatement(localVariable);
		}
		if (env.isPreserveLineNumbers()) {
			printer.adjustStartPosition(localVariable);
		}
		if (!context.noTypeDecl()) {
			elementPrinterHelper.writeModifiers(localVariable);
			scan(localVariable.getType());
			printer.write(" ");
		}
		printer.write(localVariable.getSimpleName());
		if (localVariable.getDefaultExpression() != null) {
			printer.write(" = ");
			scan(localVariable.getDefaultExpression());
		}
	}
	public <R> void visitCtBlock(CtBlock<R> block) {
		enterCtStatement(block);
		if (!block.isImplicit()) {
			printer.write("{");
		}
		printer.incTab();
		for (CtStatement statement : block.getStatements()) {
			if (!statement.isImplicit()) {
				printer.writeln().writeTabs();
				elementPrinterHelper.writeStatement(statement);
			}
		}
		printer.decTab();
		printer.adjustEndPosition(block);
		if (env.isPreserveLineNumbers()) {
			if (!block.isImplicit()) {
				printer.writeTabs().write("}");
			}
		} else {
			printer.writeln().writeTabs();
			if (!block.isImplicit()) {
				printer.write("}");
			}
		}
	}
	public void visitCtNamedElement(CtNamedElement namedElement, CompilationUnit sourceCompilationUnit) {
		writeAnnotations(namedElement);
		if (env.isPreserveLineNumbers()) {
			printer.adjustStartPosition(namedElement);
		}
	}
	public void writeHeader(List<CtType<?>> types, Collection<CtReference> imports) {
		if (!types.isEmpty()) {
			for (CtType<?> ctType : types) {
				writeComment(ctType, CommentOffset.TOP_FILE);
			}
			// writing the header package
			if (!types.get(0).getPackage().isUnnamedPackage()) {
				printer.write("package " + types.get(0).getPackage().getQualifiedName() + ";");
			}
			printer.writeln().writeln().writeTabs();
			for (CtReference ref : imports) {
				String importStr = "import";
				String importTypeStr = "";

				if (ref instanceof CtTypeReference) {
					CtTypeReference typeRef = (CtTypeReference) ref;
					importTypeStr = typeRef.getQualifiedName();
				} else if (ref instanceof CtExecutableReference) {
					importStr += " static";
					CtExecutableReference execRef = (CtExecutableReference) ref;
					if (execRef.getDeclaringType() != null) {
						importTypeStr = this.removeInnerTypeSeparator(execRef.getDeclaringType().getQualifiedName()) + "." + execRef.getSimpleName();
					}
				} else if (ref instanceof CtFieldReference) {
					importStr += " static";
					CtFieldReference fieldRef = (CtFieldReference) ref;
					importTypeStr = this.removeInnerTypeSeparator(fieldRef.getDeclaringType().getQualifiedName()) + "." + fieldRef.getSimpleName();
				}

				if (!importTypeStr.equals("") && !isJavaLangClasses(importTypeStr)) {
					printer.write(importStr + " " + importTypeStr + ";").writeln().writeTabs();
				}
			}
			printer.writeln().writeTabs();
		}
	}
	public PrinterHelper adjustStartPosition(CtElement e) {
		if (e.getPosition() != null && !e.isImplicit()) {
			// we should add some lines
			while (line < e.getPosition().getLine()) {
				writeln();
			}
			// trying to remove some lines
			while (line > e.getPosition().getLine()) {
				if (!removeLine()) {
					break;
				}
			}
		}
		return this;
	}
	public PrinterHelper adjustEndPosition(CtElement e) {
		if (env.isPreserveLineNumbers() && e.getPosition() != null) {
			// let's add lines if required
			while (line < e.getPosition().getEndLine()) {
				writeln();
			}
		}
		return this;
	}
	protected void generateProcessedSourceFilesUsingCUs() {

		factory.getEnvironment().debugMessage("Generating source using compilation units...");
		// Check output directory
		if (outputDirectory == null) {
			throw new RuntimeException("You should set output directory before generating source files");
		}
		// Create spooned directory
		if (outputDirectory.isFile()) {
			throw new RuntimeException("Output must be a directory");
		}
		if (!outputDirectory.exists()) {
			if (!outputDirectory.mkdirs()) {
				throw new RuntimeException("Error creating output directory");
			}
		}

		try {
			outputDirectory = outputDirectory.getCanonicalFile();
		} catch (IOException e1) {
			throw new SpoonException(e1);
		}

		factory.getEnvironment().debugMessage("Generating source files to: " + outputDirectory);

		List<File> printedFiles = new ArrayList<>();
		for (spoon.reflect.cu.CompilationUnit cu : factory.CompilationUnit().getMap().values()) {

			if (cu.getDeclaredTypes().size() == 0) { // case of package-info
				continue;
			}

			CtType<?> element = cu.getMainType();

			CtPackage pack = element.getPackage();

			// create package directory
			File packageDir;
			if (pack.isUnnamedPackage()) {
				packageDir = new File(outputDirectory.getAbsolutePath());
			} else {
				// Create current package directory
				packageDir = new File(outputDirectory.getAbsolutePath() + File.separatorChar + pack.getQualifiedName().replace('.', File.separatorChar));
			}
			if (!packageDir.exists()) {
				if (!packageDir.mkdirs()) {
					throw new RuntimeException("Error creating output directory");
				}
			}

			// print type
			try {
				File file = new File(packageDir.getAbsolutePath() + File.separatorChar + element.getSimpleName() + DefaultJavaPrettyPrinter.JAVA_FILE_EXTENSION);
				file.createNewFile();

				// the path must be given relatively to to the working directory
				InputStream is = getCompilationUnitInputStream(cu.getFile().getPath());

				IOUtils.copy(is, new FileOutputStream(file));


				if (!printedFiles.contains(file)) {
					printedFiles.add(file);
				}

			} catch (Exception e) {
				Launcher.LOGGER.error(e.getMessage(), e);
			}
		}
	}
