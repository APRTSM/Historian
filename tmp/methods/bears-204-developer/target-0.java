  public boolean equals(Object o){
    if (o == null || !(o instanceof LoginResponseMessage)) {
      return false;
    }
    LoginResponseMessage other = (LoginResponseMessage) o;
    return success == other.success && nickname.equals(other.nickname);
  }
