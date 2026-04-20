  private Node tryFoldTypeof(Node originalTypeofNode) {
    Preconditions.checkArgument(originalTypeofNode.getType() == Token.TYPEOF);

    Node argumentNode = originalTypeofNode.getFirstChild();
    if (argumentNode == null || !NodeUtil.isLiteralValue(argumentNode)) {
      return originalTypeofNode;
    }

    String typeNameString = null;

    switch (argumentNode.getType()) {
      case Token.STRING:
        typeNameString = "string";
        break;
      case Token.NUMBER:
        typeNameString = "number";
        break;
      case Token.TRUE:
      case Token.FALSE:
        typeNameString = "boolean";
        break;
      case Token.NULL:
      case Token.OBJECTLIT:
      case Token.ARRAYLIT:
        typeNameString = "object";
        break;
      case Token.NAME:
        // We assume here that programs don't change the value of the
        // keyword undefined to something other than the value undefined.
        if ("undefined".equals(argumentNode.getString())) {
          typeNameString = "undefined";
        }
        break;
    }

    if (typeNameString != null) {
      Node newNode = Node.newString(typeNameString);
      originalTypeofNode.getParent().replaceChild(originalTypeofNode, newNode);
      reportCodeChange();

      return newNode;
    }

    return originalTypeofNode;
  }
  void addMapping(Node node, Position startPosition, Position endPosition) {
    String sourceFile = (String)node.getProp(Node.SOURCEFILE_PROP);

    // If the node does not have an associated source file or
    // its line number is -1, then the node does not have sufficient
    // information for a mapping to be useful.
    if (sourceFile == null || node.getLineno() < 0) {
      return;
    }

    String escapedSourceFile;
    if (lastSourceFile != sourceFile) {  // yes, "s1 != s2" not "!s1.equals(s2)"
      lastSourceFile = sourceFile;
      lastSourceFileEscaped = escapeString(sourceFile);
    }
    escapedSourceFile = lastSourceFileEscaped;
    // Create the new mapping.
    Mapping mapping = new Mapping();
    mapping.id = mappings.size();
    mapping.sourceFile = escapedSourceFile;
    mapping.originalPosition = new Position(node.getLineno(), node.getCharno());

    String originalName = (String)node.getProp(Node.ORIGINALNAME_PROP);
    if (originalName != null) {
      mapping.originalName = escapeString(originalName);
    }

      // If the mapping is found on the first line, we need to offset
      // its character position by the number of characters found on
      // the *last* line of the source file to which the code is
      // being generated.
      int offsetLine = offsetPosition.getLineNumber();
      int startOffsetPosition = offsetPosition.getCharacterIndex();
      int endOffsetPosition = offsetPosition.getCharacterIndex();

      if (startPosition.getLineNumber() > 0) {
        startOffsetPosition = 0;
      }

      if (endPosition.getLineNumber() > 0) {
        endOffsetPosition = 0;
      }

      mapping.startPosition =
          new Position(startPosition.getLineNumber() + offsetLine,
                       startPosition.getCharacterIndex() + startOffsetPosition);

      mapping.endPosition =
          new Position(endPosition.getLineNumber() + offsetLine,
                       endPosition.getCharacterIndex() + endOffsetPosition);

    mappings.add(mapping);
  }
  public void appendTo(Appendable out, String name) throws IOException {
    // Write the mappings out to the file. The format of the generated
    // source map is three sections, each deliminated by a magic comment.
    //
    // The first section contains an array for each line of the generated
    // code, where each element in the array is the ID of the mapping which
    // best represents the index-th character found on that line of the
    // generated source code.
    //
    // The second section contains an array per generated line. Unused.
    //
    // The third and final section contains an array per line, each of which
    // represents a mapping with a unique ID. The mappings are added in order.
    // The array itself contains a tuple representing
    // ['source file', line, col (, 'original name')]
    //
    // Example for 2 lines of generated code (with line numbers added for
    // readability):
    //
    // 1)  /** Begin line maps. **/{ "count": 2 }
    // 2)  [0,0,0,0,0,0,1,1,1,1,2]
    // 3)  [2,2,2,2,2,2,3,4,4,4,4,4]
    // 4)  /** Begin file information. **/
    // 5)  []
    // 6)  []
    // 7)  /** Begin mapping definitions. **/
    // 8)  ["a.js", 1, 34]
    // 9)  ["a.js", 5, 2]
    // 10) ["b.js", 1, 3, "event"]
    // 11) ["c.js", 1, 4]
    // 12) ["d.js", 3, 78, "foo"]

    int maxLine = findLastLine();

    // Add the line character maps.
    out.append("/** Begin line maps. **/{ \"file\" : ");
    out.append(escapeString(name));
    out.append(", \"count\": ");
    out.append(String.valueOf(maxLine + 1));
    out.append(" }\n");
    (new LineMapper(out)).appendLineMappings();

    // Add the source file maps.
    out.append("/** Begin file information. **/\n");

    // This section is unused but we need one entry per line to
    // prevent changing the format.
    for (int i = 0; i <= maxLine; ++i) {
      out.append("[]\n");
    }

    // Add the mappings themselves.
    out.append("/** Begin mapping definitions. **/\n");

    for (Mapping mapping : mappings) {
      mapping.appendTo(out);
      out.append("\n");
    }
  }
    private void writeCharsBetween(Mapping prev, Mapping next)
        throws IOException {
      int nextLine = getAdjustedLine(next.startPosition);
      int nextCol = getAdjustedCol(next.startPosition);
      // If the previous value is null, no mapping exists.
      int id = (prev != null) ? prev.id : UNMAPPED;
      writeCharsUpTo(nextLine, nextCol, id);
    }
    void appendTo(Appendable out) throws IOException {
      out.append("[");

      out.append(sourceFile);
      // The source file rarely changes, so cache the escaped string.

      out.append(",");


      out.append(String.valueOf(originalPosition.getLineNumber()));

      out.append(",");
      out.append(String.valueOf(originalPosition.getCharacterIndex()));

      if (originalName != null) {
        out.append(",");
        out.append(originalName);
      }

      out.append("]");
    }
    private void closeLine() throws IOException {
      out.append("]\n");
    }
    private void writeCharsUpTo(
        int nextLine, int nextCol, int id)
        throws IOException {
      Preconditions.checkState(line <= nextLine, "");
      Preconditions.checkState(line < nextLine || col <= nextCol);

      if (line == nextLine && col == nextCol) {
        // Nothing to do.
        return;
      }

      String idString = (id == UNMAPPED) ? UNMAPPED_STRING : String.valueOf(id);
      for (int i = line; i <= nextLine; i++) {
        if (i == nextLine) {
          for (int j = col; j < nextCol; j++) {
            addCharEntry(idString);
          }
          break;
        }
        closeLine();
        openLine();
      }

      line = nextLine;
      col = nextCol;
    }
    private void openLine() throws IOException {
      out.append("[");
      this.firstChar = true;
    }
  private int findLastLine() {
    int maxLine = 0;
    for (Mapping mapping : mappings) {
      int endPositionLine = mapping.endPosition.getLineNumber();
      maxLine = Math.max(maxLine, endPositionLine);
    }
    return maxLine + prefixPosition.getLineNumber();
  }
    void appendLineMappings() throws IOException {
      Preconditions.checkState(!mappings.isEmpty());

      // Start the first line.
      openLine();


      // And close the final line.

    /**
     * Begin the entry for a new line.
     */

    /**
     * End the entry for a line.
     */

    /**
     * Add a new char position entry.
     * @param id The mapping id to record.
     */

  /**
   * Mark any visited mapping as "used".
   */
    /**
     * @throws IOException
     */

    /**
     * @param m The mapping for the current code segment. null if the segment
     *     is unmapped.
     * @param line The starting line for this code segment.
     * @param col The starting column for this code segment.
     * @param endLine The ending line
     * @param endCol The ending column
     * @throws IOException
     */

  /**
   * Walk the mappings and visit each segment of the mappings, unmapped
   * segments are visited with a null mapping, unused mapping are not visited.
   */
    // The last line and column written


    // Append the line mapping entries.

      // The mapping list is ordered as a pre-order traversal.  The mapping
      // positions give us enough information to rebuild the stack and this
      // allows the building of the source map in O(n) time.
      Deque<Mapping> stack = new ArrayDeque<Mapping>();
      for (Mapping m : mappings) {
        // Find the closest ancestor of the current mapping:
        // An overlapping mapping is an ancestor of the current mapping, any
        // non-overlapping mappings are siblings (or cousins) and must be
        // closed in the reverse order of when they encountered.
        while (!stack.isEmpty() && !isOverlapped(stack.peek(), m)) {
          Mapping previous = stack.pop();
          writeClosedMapping(previous);
        }

        // Any gaps between the current line position and the start of the
        // current mapping belong to the parent.
        Mapping parent = stack.peek();
        writeCharsBetween(parent, m);

        stack.push(m);
      }

      // There are no more children to be had, simply close the remaining
      // mappings in the reverse order of when they encountered.
      while (!stack.isEmpty()) {
        Mapping m = stack.pop();
        writeClosedMapping(m);
      }
      closeLine();
    }
    private void addCharEntry(String id) throws IOException {
      if (firstChar) {
        firstChar = false;
      } else {
        out.append(",");
      }
      out.append(id);
    }
    private void writeClosedMapping(Mapping m) throws IOException {
      int nextLine = getAdjustedLine(m.endPosition);
      int nextCol = getAdjustedCol(m.endPosition);
      // If this anything remaining in this mapping beyond the
      // current line and column position, write it out now.
      if (line < nextLine || (line == nextLine && col < nextCol)) {
        writeCharsUpTo(nextLine, nextCol, m.id);
      }
    }
