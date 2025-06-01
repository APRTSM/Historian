    private T assign(Line line, T objectToAssign) {

        for (Cell cell : line) {
            String sName = cell.getName();
            if (sName == null || sName.isEmpty() || cell.isEmpty())
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
    public void assign(Object bean, Cell cell)
            throws BeanComposeException, InvocationTargetException, IllegalAccessException, InstantiationException {
        if(cell.isEmpty()) {
            return;
        }
        if (isLeaf()) {
            assignProperty(bean, cell);
            return;
        }
        Bean2Cell childBean2Cell = children.getBean2CellByName(cell.getName());
        if (childBean2Cell != null) {
            Method getter = this.propertyDescriptor.getReadMethod();
            if (getter == null)
                throw new BeanComposeException(
                        "The property " + propertyDescriptor.getName() + " of class " + children.getLineClass()
                                .getName() + " has no getter method.");
            Object child = getter.invoke(bean);
            if (child == null) {
                child = children.getLineClass().newInstance();
                Method setter = this.propertyDescriptor.getWriteMethod();
                if (setter == null)
                    throw new BeanComposeException(
                            "The property " + propertyDescriptor.getName() + " of class " + children.getLineClass()
                                    .getName() + " has no setter method.");
                setter.invoke(bean, child);
            }
            childBean2Cell.assign(child, cell);
        }
    }
