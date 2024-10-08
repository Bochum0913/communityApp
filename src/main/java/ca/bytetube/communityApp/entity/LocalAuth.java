package ca.bytetube.communityApp.entity;

import java.util.Date;

public class LocalAuth {
    private Long localAuthId;

    private String username;

    private String password;

    private Date createTIme;

    private Date lastEditTime;

    //personInfo, one-to-one relationship
    private PersonInfo personInfo;

    public Long getLocalAuthId() {  return localAuthId;  }

    public void setLocalAuthId(Long localAuthId) {  this.localAuthId = localAuthId;  }

    public String getUsername() {   return username;  }

    public void setUsername(String username) {  this.username = username;  }

    public String getPassword() {  return password; }

    public void setPassword(String password) {  this.password = password; }

    public Date getCreateTIme() { return createTIme; }

    public void setCreateTIme(Date createTIme) { this.createTIme = createTIme; }

    public Date getLastEditTime() {  return lastEditTime; }

    public void setLastEditTime(Date lastEditTime) {  this.lastEditTime = lastEditTime; }

    public PersonInfo getPersonInfo() { return personInfo; }

    public void setPersonInfo(PersonInfo personInfo) { this.personInfo = personInfo; }
}
