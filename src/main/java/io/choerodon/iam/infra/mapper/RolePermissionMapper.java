package io.choerodon.iam.infra.mapper;

import io.choerodon.iam.infra.dataobject.RolePermissionDO;
import io.choerodon.mybatis.common.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author wuguokai
 */
public interface RolePermissionMapper extends BaseMapper<RolePermissionDO> {

    List<Long> queryPermissionIdByRoles(@Param("list") List<Long> roleIds);
}
