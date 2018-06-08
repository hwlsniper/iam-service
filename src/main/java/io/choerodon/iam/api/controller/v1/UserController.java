package io.choerodon.iam.api.controller.v1;

import io.choerodon.core.base.BaseController;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.NotFoundException;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.iam.api.dto.OrganizationDTO;
import io.choerodon.iam.api.dto.ProjectDTO;
import io.choerodon.iam.api.dto.UserDTO;
import io.choerodon.iam.api.dto.UserPasswordDTO;
import io.choerodon.iam.app.service.UserService;
import io.choerodon.iam.infra.common.utils.ParamUtils;
import io.choerodon.iam.infra.dataobject.UserDO;
import io.choerodon.mybatis.pagehelper.annotation.SortDefault;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.mybatis.pagehelper.domain.Sort;
import io.choerodon.swagger.annotation.CustomPageRequest;
import io.choerodon.swagger.annotation.Permission;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

/**
 * @author superlee
 */
@RestController
@RequestMapping(value = "/v1/users")
public class UserController extends BaseController {

    private UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Permission(level = ResourceLevel.SITE, permissionLogin = true)
    @ApiOperation(value = "查询当前用户信息")
    @GetMapping(value = "/self")
    public ResponseEntity<UserDTO> querySelf() {
        return new ResponseEntity<>(userService.querySelf(), HttpStatus.OK);
    }

    @Permission(level = ResourceLevel.SITE, permissionLogin = true)
    @ApiOperation(value = "根据id查询用户信息")
    @GetMapping(value = "/{id}/info")
    public ResponseEntity<UserDTO> queryInfo(@PathVariable Long id) {
        return Optional.ofNullable(userService.queryInfo(id))
                .map(result -> new ResponseEntity<>(result, HttpStatus.OK))
                .orElseThrow(NotFoundException::new);
    }

    @Permission(level = ResourceLevel.SITE, permissionLogin = true)
    @ApiOperation(value = "修改用户信息")
    @PutMapping(value = "/{id}/info")
    public ResponseEntity<UserDTO> updateInfo(@PathVariable Long id,
                                              @RequestBody UserDTO userDTO) {
        userDTO.setId(id);
        userDTO.updateCheck();
        userDTO.setAdmin(null);
        //不能修改状态
        userDTO.setEnabled(null);
        userDTO.setLdap(null);
        userDTO.setOrganizationId(null);
        userDTO.setLoginName(null);
        return new ResponseEntity<>(userService.updateInfo(userDTO), HttpStatus.OK);
    }

    /**
     * 上传头像到文件服务返回头像url
     */
    @Permission(level = ResourceLevel.SITE, permissionLogin = true)
    @ApiOperation(value = "用户头像上传")
    @PostMapping(value = "/{id}/photo")
    public ResponseEntity<String> uploadPhoto(@PathVariable Long id, @RequestPart MultipartFile file) {
        return new ResponseEntity<>(userService.uploadPhoto(id, file), HttpStatus.OK);
    }

    @Permission(level = ResourceLevel.ORGANIZATION, permissionLogin = true)
    @ApiOperation(value = "查询用户所在组织列表")
    @GetMapping(value = "/{id}/organizations")
    public ResponseEntity<List<OrganizationDTO>> queryOrganizations(@PathVariable Long id,
                                                                    @RequestParam(required = false, name = "included_disabled")
                                                                            boolean includedDisabled) {
        return new ResponseEntity<>(userService.queryOrganizations(id, includedDisabled), HttpStatus.OK);
    }

    @Permission(level = ResourceLevel.ORGANIZATION, permissionLogin = true)
    @ApiOperation(value = "查询用户所在项目列表")
    @GetMapping(value = "/{id}/projects")
    public ResponseEntity<List<ProjectDTO>> queryProjects(@PathVariable Long id,
                                                          @RequestParam(required = false, name = "included_disabled")
                                                                  boolean includedDisabled) {
        return new ResponseEntity<>(userService.queryProjects(id, includedDisabled), HttpStatus.OK);
    }

    /**
     * @deprecated 已过期
     */
    @ApiIgnore
    @Deprecated
    @Permission(level = ResourceLevel.ORGANIZATION, permissionLogin = true)
    @ApiOperation(value = "查询当前用户在某组织下所在的项目列表")
    @GetMapping(value = "/{id}/organizations/{organization_id}/projects")
    public ResponseEntity<List<ProjectDTO>> queryProjectsByOrganizationId(@PathVariable Long id,
                                                                          @PathVariable(name = "organization_id") Long organizationId) {
        return new ResponseEntity<>(userService.queryProjectsByOrganizationId(id, organizationId), HttpStatus.OK);
    }

    /**
     * @deprecated 已过期
     */
    @ApiIgnore
    @Deprecated
    @Permission(level = ResourceLevel.SITE, permissionLogin = true)
    @ApiOperation(value = "查询当前用户所在组织列表以及用户在该组织下所在的项目列表")
    @GetMapping(value = "/self/organizations_projects")
    public ResponseEntity<List<OrganizationDTO>> queryOrganizationWithProjects() {
        return new ResponseEntity<>(userService.queryOrganizationWithProjects(), HttpStatus.OK);
    }

    @Permission(level = ResourceLevel.ORGANIZATION, permissionLogin = true)
    @ApiOperation(value = "根据用户名查询用户信息")
    @GetMapping
    public ResponseEntity<UserDTO> query(@RequestParam(name = "login_name") String loginName) {
        return Optional.ofNullable(userService.queryByLoginName(loginName))
                .map(result -> new ResponseEntity<>(result, HttpStatus.OK))
                .orElseThrow(NotFoundException::new);
    }

    @Permission(level = ResourceLevel.SITE, permissionLogin = true)
    @ApiOperation(value = "修改密码")
    @PutMapping(value = "/{id}/password")
    public ResponseEntity selfUpdatePassword(@PathVariable Long id,
                                             @RequestBody @Valid UserPasswordDTO userPasswordDTO) {
        userService.selfUpdatePassword(id, userPasswordDTO, true);
        return new ResponseEntity(HttpStatus.OK);
    }

    @Permission(level = ResourceLevel.SITE, permissionLogin = true)
    @ApiOperation(value = "用户信息校验")
    @PostMapping(value = "/check")
    public ResponseEntity check(@RequestBody UserDTO user) {
        userService.check(user);
        return new ResponseEntity(HttpStatus.OK);
    }

    /**
     * 分页查询所有的admin用户
     *
     * @param pageRequest 分页信息
     * @return 分页的admin用户
     */
    @Permission(level = ResourceLevel.SITE)
    @ApiOperation(value = "分页模糊查询管理员用户列表")
    @CustomPageRequest
    @GetMapping("/admin")
    public ResponseEntity<Page<UserDTO>> pagingQueryAdminUsers(
            @ApiIgnore
            @SortDefault(value = "id", direction = Sort.Direction.ASC) PageRequest pageRequest,
            @RequestParam(required = false, name = "loginName") String loginName,
            @RequestParam(required = false, name = "realName") String realName,
            @RequestParam(required = false, name = "enabled") Boolean enabled,
            @RequestParam(required = false, name = "locked") Boolean locked,
            @RequestParam(required = false, name = "params") String[] params
    ) {
        UserDO userDO = new UserDO();
        userDO.setLoginName(loginName);
        userDO.setRealName(realName);
        userDO.setEnabled(enabled);
        userDO.setLocked(locked);
        return new ResponseEntity<>(userService.pagingQueryAdminUsers(pageRequest, userDO, ParamUtils.arrToStr(params)), HttpStatus.OK);
    }


    @Permission(level = ResourceLevel.SITE)
    @ApiOperation(value = "批量给用户添加管理员身份")
    @PostMapping("/admin")
    public ResponseEntity<Page<UserDTO>> addDefaultUsers(@ModelAttribute("id") long[] ids) {
        userService.addAdminUsers(ids);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Permission(level = ResourceLevel.SITE)
    @ApiOperation(value = "清除用户的管理员身份")
    @DeleteMapping("/admin/{id}")
    public ResponseEntity<Page<UserDTO>> deleteDefaultUser(@PathVariable long id) {
        userService.deleteAdminUser(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation(value = "批量查询用户信息列表")
    @PostMapping(value = "/ids")
    public ResponseEntity<List<UserDTO>> listUsersByIds(@RequestBody Long[] ids) {
        return new ResponseEntity<>(userService.listUsersByIds(ids), HttpStatus.OK);
    }


}
