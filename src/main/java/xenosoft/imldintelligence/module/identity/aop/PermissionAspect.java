package xenosoft.imldintelligence.module.identity.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import xenosoft.imldintelligence.module.identity.internal.service.impl.PermissionSerivce;
import xenosoft.imldintelligence.common.CheckPermission;

@Aspect
@Component
@RequiredArgsConstructor
public class PermissionAspect {
    private final PermissionSerivce permissionSerivce;
    
    @Before("@annotation(checkPermission)")
    public Object checkPermission(JoinPoint jp, CheckPermission checkPermission){

    }
}
