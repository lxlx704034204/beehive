package com.gustz.beehive.model;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * UserDto DTO
 *
 * @author zhangzhenfeng
 * @since 2015-12-25
 */
public class UserDto implements Serializable {

    @NotNull(message = "user.id.null")
    private Integer id;

    @NotNull(message = "user.gid.null")
    private String gid;

    private String name;

    private String pwd;

    private Integer createTime;

    public UserDto() {
        // null
    }

    /**
     * @param gid
     * @param name
     * @param pwd
     * @param createTime
     */
    public UserDto(String gid, String name, String pwd, Integer createTime) {
        this.gid = gid;
        this.name = name;
        this.pwd = pwd;
        this.createTime = createTime;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getGid() {
        return gid;
    }

    public void setGid(String gid) {
        this.gid = gid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public Integer getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Integer createTime) {
        this.createTime = createTime;
    }
}
