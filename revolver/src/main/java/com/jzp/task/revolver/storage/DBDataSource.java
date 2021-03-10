package com.jzp.task.revolver.storage;

public class DBDataSource {

  private String url;
  private String username;
  private String password;

  // 是否主库
  // 主修改 从读取
  private boolean master;

  public DBDataSource() {

  }

  /**
   * @param url      db url需要和业务datasource中配置的url一模一样
   * @param username
   * @param password
   */
  public DBDataSource(String url, String username, String password) {
    super();
    this.url = url;
    this.username = username;
    this.password = password;
  }

  public String getUrl() {
    return url;
  }

  /**
   * @param url db url需要和业务datasource中配置的url一模一样
   */
  public void setUrl(String url) {
    this.url = url;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public boolean isMaster() {
    return master;
  }

  public void setMaster(boolean master) {
    this.master = master;
  }

  @Override
  public String toString() {
    return "DBDataSource [url=" + url + ", username=" + username + ", password=" + password + "]";
  }


}
