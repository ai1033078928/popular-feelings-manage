package com.hrbu.model;

public class MyCode {
    private int code;
    private String message;

    public MyCode(int c){
        this.code = c;
        switch (this.code) {
            case 100: this.setMessage("没有该用户");break;
            case 101: this.setMessage("密码错误");break;
            case 99: this.setMessage("成功");break;
            case 98: this.setMessage("管理员登录成功");break;
            case 97: this.setMessage("用户登录成功");break;
            case 96: this.setMessage("服务器内部错误，失败");break;
            case 95: this.setMessage("用户已存在");break;
            default: this.setMessage("");break;
        }
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
