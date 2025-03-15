package com.hrbu.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.*;
import java.util.Date;

/**
 * com.fasterxml.jackson.databind.exc.InvalidDefinitionException: No serializer found for class java.util.logging.ErrorManager and no properties discovered to create BeanSerializer
 *
 * 原因：因为jsonplugin用的是java的内审机制.被管理的pojo会加入一个hibernateLazyInitializer属性,
 * jsonplugin会对hibernateLazyInitializer拿出来操作,并读取里面一个不能被反射操作的属性就产生了异常
 */
//@JsonIgnoreProperties(value = {"hibernateLazyInitializer", "handler"})
public class SysLog implements Serializable {
    private Long id;

    private String username; //用户名

    private String operation; //操作

    private String method; //方法名

    private String params; //参数

    private String ip; //ip地址

    private Date createDate; //操作时间

    public SysLog(Long id) {
        this.id = id;
    }

    //创建getter和setter方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getParams() {
        return params;
    }

    public void setParams(String params) {
        this.params = params;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    @Override
    public String toString() {
        return "SysLog{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", operation='" + operation + '\'' +
                ", method='" + method + '\'' +
                ", params='" + params + '\'' +
                ", ip='" + ip + '\'' +
                ", createDate=" + createDate +
                '}';
    }

    public void saveLog(String f) {
        File file = new File(f);
        //如果文件不存在,就动态创建文件
        if(!file.exists()){
            try {
                file.createNewFile();
            } catch (IOException e) {
                System.out.println("创建文件失败");
                //e.printStackTrace();
            }
        }

        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(file, true)));
            bw.write( this + "\n");
            bw.flush();
        } catch (FileNotFoundException e) {
            System.out.println("文件未找到");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            // 关闭资源
            try {
                if (bw != null){
                    bw.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}