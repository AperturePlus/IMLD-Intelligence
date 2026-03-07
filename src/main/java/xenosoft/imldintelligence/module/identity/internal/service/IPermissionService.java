package xenosoft.imldintelligence.module.identity.internal.service;

import xenosoft.imldintelligence.module.identity.internal.model.UserSubject;

import java.util.Map;
import java.util.Set;

/**
 * 权限服务接口，提供 RBAC 与 ABAC 组合授权判定能力。
 */
public interface IPermissionService {

    /**
     * 判断用户是否被允许对指定资源执行指定动作。
     *
     * @param tenantId 租户标识
     * @param userId 用户主键
     * @param res 资源类型或资源标识
     * @param action 待执行的动作编码
     * @param resAttr 参与 ABAC 判定的资源属性集合
     * @return 允许访问时返回 {@code true}，否则返回 {@code false}
     */
    boolean isAllowed(Long tenantId, Long userId, String res, String action,
                             Map<String, Object> resAttr);

    /**
     * 查询用户在指定租户内的生效角色编码集合。
     *
     * @param tenantId 租户标识
     * @param userId 用户主键
     * @return 用户当前生效的角色编码集合
     */
    Set<String> getEffectiveRoleCodes(Long tenantId, Long userId);

    /**
     * 加载授权判定所需的用户主体信息。
     *
     * @param tenantId 租户标识
     * @param userId 用户主键
     * @return 用于权限计算的用户主体对象
     */
    UserSubject loadSubject(Long tenantId, Long userId);

}
