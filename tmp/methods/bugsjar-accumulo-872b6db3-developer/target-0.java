    Node parse_(byte[] expression) {
      Node result = null;
      Node expr = null;
      int wholeTermStart = index;
      int subtermStart = index;
      boolean subtermComplete = false;
      
      while (index < expression.length) {
        switch (expression[index++]) {
          case '&': {
            expr = processTerm(subtermStart, index - 1, expr, expression);
            if (result != null) {
              if (!result.type.equals(NodeType.AND))
                throw new BadArgumentException("cannot mix & and |", new String(expression), index - 1);
            } else {
              result = new Node(NodeType.AND, wholeTermStart);
            }
            result.add(expr);
            expr = null;
            subtermStart = index;
            subtermComplete = false;
            break;
          }
          case '|': {
            expr = processTerm(subtermStart, index - 1, expr, expression);
            if (result != null) {
              if (!result.type.equals(NodeType.OR))
                throw new BadArgumentException("cannot mix | and &", new String(expression), index - 1);
            } else {
              result = new Node(NodeType.OR, wholeTermStart);
            }
            result.add(expr);
            expr = null;
            subtermStart = index;
            subtermComplete = false;
            break;
          }
          case '(': {
            parens++;
            if (subtermStart != index - 1 || expr != null)
              throw new BadArgumentException("expression needs & or |", new String(expression), index - 1);
            expr = parse_(expression);
            subtermStart = index;
            subtermComplete = false;
            break;
          }
          case ')': {
            parens--;
            Node child = processTerm(subtermStart, index - 1, expr, expression);
            if (child == null && result == null)
              throw new BadArgumentException("empty expression not allowed", new String(expression), index);
            if (result == null)
              return child;
            if (result.type == child.type)
              for (Node c : child.children)
                result.add(c);
            else
              result.add(child);
            result.end = index - 1;
            return result;
          }
          case '"': {
            if (subtermStart != index - 1)
              throw new BadArgumentException("expression needs & or |", new String(expression), index - 1);
            
            while (index < expression.length && expression[index] != '"') {
              if (expression[index] == '\\') {
                index++;
                if (expression[index] != '\\' && expression[index] != '"')
                  throw new BadArgumentException("invalid escaping within quotes", new String(expression), index - 1);
              }
              index++;
            }
            
            if (index == expression.length)
              throw new BadArgumentException("unclosed quote", new String(expression), subtermStart);
            
            if (subtermStart + 1 == index)
              throw new BadArgumentException("empty term", new String(expression), subtermStart);
            
            index++;
            
            subtermComplete = true;
            
            break;
          }
          default: {
            if (subtermComplete)
              throw new BadArgumentException("expression needs & or |", new String(expression), index - 1);
            
            byte c = expression[index - 1];
            if (!Authorizations.isValidAuthChar(c))
              throw new BadArgumentException("bad character (" + c + ")", new String(expression), index - 1);
          }
        }
      }
      Node child = processTerm(subtermStart, index, expr, expression);
      if (result != null) {
        result.add(child);
        result.end = index;
      } else
        result = child;
      if (result.type != NodeType.TERM)
        if (result.children.size() < 2)
          throw new BadArgumentException("missing term", new String(expression), index);
      return result;
    }
