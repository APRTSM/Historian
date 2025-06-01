  public boolean equals(Object o){
    if (o == null || !(o instanceof LoginResponseMessage)) {
      return false;
    }
    LoginResponseMessage other = (LoginResponseMessage) o;
    return success == other.success && nickname.equals(other.nickname);
  }
  public boolean equals(Object o){
    if (o == null || !(o instanceof MenuMessageResponse)) {
      return false;
    }
    MenuMessageResponse other = (MenuMessageResponse) o;
    return menuType.equals(other.menuType) && Arrays.equals(information, other.information);
  }
  public boolean equals(Object o){
    if (o == null || !(o instanceof RegisterResponseMessage)) {
      return false;
    }
    RegisterResponseMessage other = (RegisterResponseMessage) o;
    return success == other.success && Arrays.equals(messages, other.messages);
  }
  public boolean equals(Object o){
    if (o == null || !(o instanceof UnregisterResponseMessage)) {
      return false;
    }
    UnregisterResponseMessage other = (UnregisterResponseMessage) o;
    return success == other.success && Arrays.equals(messages, other.messages);
  }
