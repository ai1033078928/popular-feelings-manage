package com.hrbu.component;

import com.hrbu.annotation.Operation;
import com.hrbu.model.RootUser;
import com.hrbu.model.SysLog;
import com.hrbu.model.User;
import com.hrbu.util.IpAdrressUtil;
import com.hrbu.util.JacksonUtil;
import com.hrbu.util.UniqueOrderGenerate;
import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * 实现日志AOP类
 *
 * 系统日志, 切面处理类
 */


@Aspect  // 定义切面类
/**
 * 元注解，意思是可以注解其他类注解
 *
 * 官方的原话是：带此注解的类看为组件，当使用基于注解的配置和类路径扫描的时候，这些类就会被实例化。
 * 其他类级别的注解也可以被认定为是一种特殊类型的组件，比如@Repository @Aspect。
 * 所以，@Component可以注解其他类注解。
 */
@Component
public class SysLogAspect {

    // 定义切点 @Pointcut
    // 在注解的位置切入代码  匹配指定注解为切入点的方法
    @Pointcut("@annotation(com.hrbu.annotation.Operation)")
    public void logPoinCut() { }

    // springAop @AfterReturning注解 获取返回值
    // 切面 配置通知
    @AfterReturning("logPoinCut()")
    public void saveSysLog(JoinPoint joinPoint) {
        // 保存日志
        // Twitter_Snowflake生成id
        UniqueOrderGenerate uniqueOrderGenerate = new UniqueOrderGenerate(0, 0);
        SysLog sysLog = new SysLog(uniqueOrderGenerate.nextId());

        //从切面织入点处通过反射机制获取织入点处的方法
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        //获取切入点所在的方法
        Method method = signature.getMethod();

        //获取操作
        Operation operation = method.getAnnotation(Operation.class);
        if (operation != null) {
            String value = operation.value();
            sysLog.setOperation(value);//保存获取的操作
        }

        //获取请求的类名
        String className = joinPoint.getTarget().getClass().getName();

        //获取请求的方法名
        String methodName = method.getName();
        sysLog.setMethod(className + "." + methodName);

        //请求的参数
        // Object[] args = joinPoint.getArgs();
        List<String> args = new LinkedList();
        for (Object arg : joinPoint.getArgs()) {
            String str = arg.toString();
            args.add(str);
        }

        //将参数所在的数组转换成json
        String params = null;
        try {
            params = JacksonUtil.obj2json(args);
        } catch (Exception e) {
            e.printStackTrace();
        }
        sysLog.setParams(params);

        //请求的时间
        sysLog.setCreateDate(new Date());

        //获取用户名
        /*
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            sysLog.setUsername(authentication.getName());
        }
        */
        HttpSession session = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                .getRequest().getSession();
        if (null != session.getAttribute("loginUser")) {
            if (session.getAttribute("loginUser") instanceof RootUser) {
                sysLog.setUsername(((RootUser) session.getAttribute("loginUser")).getId());
            } else if (session.getAttribute("loginUser") instanceof User) {
                sysLog.setUsername(((User) session.getAttribute("loginUser")).getUserName());
            } else {
                sysLog.setUsername("未知");
            }
        }

        //获取用户ip地址
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                .getRequest();
        sysLog.setIp(IpAdrressUtil.getIpAdrress(request));

        // 调用service保存SysLog实体类到数据库
        // sysLogService.saveLog(sysLog);

        // 保存到文件
        //String file = "D:\\Program Files\\JetBrains\\ideaProjects\\popular-feelings-manage\\popular-feelings-manage-ssm\\log\\log.txt";
        //sysLog.saveLog(file);
        //System.out.println(sysLog);
    }
}
