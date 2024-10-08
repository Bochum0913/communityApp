package ca.bytetube.communityApp.entity;

import java.util.Date;

public class Area {
    //Primitive data types have default values that may affect the results
    private Integer areaId;

    private String areaName;

    //Priority, the bigger it is, the more it will be displayed in the front row
    private Integer priority;

    private Date createTime;

    private Date lastEditTime;

    public Integer getAreaId() {return areaId;}

    public void setAreaId(Integer areaId) { this.areaId = areaId;  }

    public String getAreaName() { return areaName; }

    public void setAreaName(String areaName) {  this.areaName = areaName;  }

    public Integer getPriority() {  return priority;  }

    public void setPriority(Integer priority) {  this.priority = priority;  }

    public Date getCreateTime() {  return createTime;  }

    public void setCreateTime(Date createTime) {  this.createTime = createTime;  }

    public Date getLastEditTime() { return lastEditTime;  }

    public void setLastEditTime(Date lastEditTime) {  this.lastEditTime = lastEditTime; }


}
