  private ExtractionInfo extractBlockComment(JsDocToken token) {
    StringBuilder builder = new StringBuilder();

    boolean ignoreStar = true;

    do {
      switch (token) {
        case ANNOTATION:
        case EOC:
        case EOF:
          return new ExtractionInfo(builder.toString().trim(), token);

        case STAR:
          if (!ignoreStar) {
            if (builder.length() > 0) {
              builder.append(' ');
            }

            builder.append('*');
          }

          token = next();
          continue;

        case EOL:
          ignoreStar = true;
          builder.append('\n');
          token = next();
          continue;

        default:
          if (!ignoreStar && builder.length() > 0) {
            builder.append(' ');
          }

          ignoreStar = false;

          builder.append(toString(token));

          String line = stream.getRemainingJSDocLine();
// start of generated patch
line=trimEnd(line);
builder.append(line);
jsdocBuilder.recordDescription(line);
token=next();
// end of generated patch
/* start of original code
          line = trimEnd(line);
          builder.append(line);
          token = next();
 end of original code*/
      }
    } while (true);
  }
