  public LoginResponseMessage(boolean success, String nickname) {
    super(ViewMessageType.LOGIN_RESPONSE);

    this.success = success;
    this.nickname = nickname;
  }
  public MenuMessageResponse(MenuMessageTypes menuType, String[] information) {
    super(ViewMessageType.MENU_RESPONSE);

    this.menuType = menuType;
    this.information = information;
  }
  public RegisterResponseMessage(boolean success, String[] messages) {
    super(ViewMessageType.REGISTER_RESPONSE);

    this.success = success;
    this.messages = messages;
  }
