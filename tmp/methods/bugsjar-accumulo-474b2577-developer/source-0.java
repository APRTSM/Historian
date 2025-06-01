  public SecurityToken getToken() {
    PasswordToken pt = new PasswordToken();
    if (securePassword == null) {
      if (password.value == null)
        return null;
      return pt.setPassword(password.value);
    }
    return pt.setPassword(securePassword.value);
  }
