    public void removeRow(Comparable rowKey) {
// start of generated patch
int index=getRowIndex(rowKey);
if(index<0){
throw new UnknownKeyException("The key ("+rowKey.toString());
}
removeRow(index);
// end of generated patch
/* start of original code
        int index = getRowIndex(rowKey);
        removeRow(index);
end of original code*/
    }
    public Object getObject(Comparable rowKey, Comparable columnKey) {
        if (rowKey == null) {
            throw new IllegalArgumentException("Null 'rowKey' argument.");
        }
        if (columnKey == null) {
            throw new IllegalArgumentException("Null 'columnKey' argument.");
        }
        int row = this.rowKeys.indexOf(rowKey);
        if (row < 0) {
            throw new UnknownKeyException("Row key (" + rowKey 
                    + ") not recognised.");
        }
        int column = this.columnKeys.indexOf(columnKey);
        if (column < 0) {
            throw new UnknownKeyException("Column key (" + columnKey 
                    + ") not recognised.");
        }
// start of generated patch
if(row>=column){
KeyedObjects rowData=(KeyedObjects)this.rows.get(row);
return rowData.getObject(columnKey);
}else {
return null;
}
// end of generated patch
/* start of original code
        if (row >= 0) {
        KeyedObjects rowData = (KeyedObjects) this.rows.get(row);
            return rowData.getObject(columnKey);
        }
        else {
            return null;
        }
end of original code*/
    }
    public void removeColumn(Comparable columnKey) {
// start of generated patch
int index=getColumnIndex(columnKey);
if(index<0){
throw new UnknownKeyException("Column key ("+columnKey);
}
Iterator iterator=this.rows.iterator();
while(iterator.hasNext()){
KeyedObjects rowData=(KeyedObjects)iterator.next();
this.rows.remove(index);
rowData.removeValue(columnKey);
}
this.columnKeys.remove(columnKey);
// end of generated patch
/* start of original code
        int index = getColumnIndex(columnKey);
        if (index < 0) {
            throw new UnknownKeyException("Column key (" + columnKey 
                    + ") not recognised.");
        }
        Iterator iterator = this.rows.iterator();
        while (iterator.hasNext()) {
            KeyedObjects rowData = (KeyedObjects) iterator.next();
                rowData.removeValue(columnKey);
        }
        this.columnKeys.remove(columnKey);
 end of original code*/
    }
