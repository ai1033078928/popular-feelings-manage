// 校验手机号
function checkPhone(phone){
    if(!(/^(0|86|17951)?(13[0-9]|15[012356789]|166|17[3678]|18[0-9]|14[57])[0-9]{8}$/.test(phone))){
        //alert("手机号码不合法，请重新输入");
        return false;
    } else {
        return true;
    }
}

// 校验密码
function checkPassWord(password){
    ////密码为七位及以上并且字母、数字、特殊字符三项中有两项
    if(!(/^(?=.{7,})(((?=.*[A-Z])(?=.*[a-z]))|((?=.*[A-Z])(?=.*[0-9]))|((?=.*[a-z])(?=.*[0-9]))).*$/.test(password))){
        //alert("手机号码不合法，请重新输入");
        return false;
    } else {
        return true;
    }
}
