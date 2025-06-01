  public JSType getLeastSupertype(JSType that) {
// start of generated patch
if(!that.isRecordType()){
return super.getLeastSupertype(that);
}
RecordTypeBuilder builder=new RecordTypeBuilder(registry);
for(String property : properties.keySet()){
if(that.toMaybeRecordType().hasProperty(property)&&that.toMaybeRecordType().getPropertyType(property).isEquivalentTo(getPropertyType(property))){
builder.addProperty(property,getPropertyType(property),getPropertyNode(property));
}
}
return getLeastSupertype(this,that);
// end of generated patch
/* start of original code
    if (!that.isRecordType()) {
      return super.getLeastSupertype(that);
    }
    RecordTypeBuilder builder = new RecordTypeBuilder(registry);
    for (String property : properties.keySet()) {
      if (that.toMaybeRecordType().hasProperty(property) &&
          that.toMaybeRecordType().getPropertyType(property).isEquivalentTo(
              getPropertyType(property))) {
        builder.addProperty(property, getPropertyType(property),
            getPropertyNode(property));
      }
    }
    return builder.build();
 end of original code*/
  }
