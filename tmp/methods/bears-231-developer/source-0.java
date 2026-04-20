    private T assign(Line line, T objectToAssign) {

        for (Cell cell : line) {
            String sName = cell.getName();
            if (sName == null || sName.isEmpty())
                continue;

            try {
                beanFactory.assignCellToBean(line.getLineType(), objectToAssign, cell);
            } catch (BeanComposeException
                    | IllegalArgumentException
                    | IllegalAccessException
                    | InvocationTargetException
                    | InstantiationException e) {
                errorEventListener.errorEvent(new ErrorEvent(this, new ComposeException(e.getMessage() + " while handling cell " + cell, e)));
            }
        }
        return objectToAssign;
    }
