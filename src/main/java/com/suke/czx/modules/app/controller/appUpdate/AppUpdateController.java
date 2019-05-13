package com.suke.czx.modules.app.controller.appUpdate;

import com.google.gson.Gson;
import com.suke.czx.common.utils.AppBaseResult;
import com.suke.czx.common.utils.PageUtils;
import com.suke.czx.common.utils.Query;
import com.suke.czx.common.validator.Assert;
import com.suke.czx.modules.app.service.appUpdate.AppUpdateService;
import com.suke.czx.modules.sys.controller.AbstractController;
import com.suke.czx.modules.sys.entity.SysUserEntity;
import com.suke.czx.modules.sys.service.SysUserService;
import com.suke.czx.modules.sys.service.SysUserTokenService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import net.sf.json.JSONObject;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * APP版本管理
 * 
 * @author czx
 * @email object_czx@163.com
 * @date 2018-01-05 15:28:57
 */
@Api(value = "API - AppUpdateController ", description = "APP版本管理")
@RestController
@RequestMapping("/app")
public class AppUpdateController extends AbstractController {

    private org.slf4j.Logger logger = LoggerFactory.getLogger(getClass());

    @Resource(name = "appUpdateService")
	private AppUpdateService appUpdateService;

	@Autowired
	private SysUserService sysUserService;

    @Autowired
    private SysUserTokenService sysUserTokenService;
	
	/**
	 * 列表
	 */
    @ApiOperation(value="列表", notes="列表")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token", required = true,dataType = "string", paramType = "query", defaultValue = "")})
	@PostMapping("/appUpdate/list")
	public AppBaseResult list(@RequestBody AppBaseResult appBaseResult)throws Exception{

        HashMap<String,Object> params = new Gson().fromJson(appBaseResult.decryptData(),HashMap.class);
		//查询列表数据
        Query query = new Query(params);
        query.isPaging(true);
		List<HashMap<String,Object>> appUpdateList = appUpdateService.queryList(query);
		PageUtils pageUtil = new PageUtils(appUpdateList, query.getTotle(), query.getLimit(), query.getPage());
        return AppBaseResult.success().setEncryptData(pageUtil);
	}
	
	
	/**
	 * 信息
	 */
    @ApiOperation(value="信息", notes="信息")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token", required = true,dataType = "string", paramType = "query", defaultValue = "")})
	@PostMapping("/appUpdate/info")
	public AppBaseResult info(@RequestBody AppBaseResult appBaseResult)throws Exception{

        HashMap<String,Object> params = new Gson().fromJson(appBaseResult.decryptData(),HashMap.class);
        HashMap<String,Object> data = appUpdateService.queryObject(params);
        return AppBaseResult.success().setEncryptData(data);
	}
	
	/**
	 * 保存
	 */
    @ApiOperation(value="保存", notes="保存")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token", required = true,dataType = "string", paramType = "query", defaultValue = "")})
	@PostMapping("/appUpdate/save")
	public AppBaseResult save(@RequestBody AppBaseResult appBaseResult)throws Exception{

        HashMap<String,Object> params = new Gson().fromJson(appBaseResult.decryptData(),HashMap.class);
		appUpdateService.saveInfo(params);
        return AppBaseResult.success();
	}
	
	/**
	 * 修改用戶信息
	 */
    @ApiOperation(value="修改", notes="修改")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token", required = true,dataType = "string", paramType = "query", defaultValue = "")})
	@PostMapping("/appUpdate/update")
	public AppBaseResult update(@RequestBody AppBaseResult appBaseResult)throws Exception{

		HashMap<String,Object> pd = new Gson().fromJson(appBaseResult.toString(),HashMap.class);
		JSONObject jsonObject=JSONObject.fromObject(pd.get("data"));
		Assert.isNull(jsonObject.get("mobile"), "手机号不能为空");
		//Assert.isNull(jsonObject.get("password"), "密码不能为空");
		SysUserEntity user =new SysUserEntity();
		user.setUserId(jsonObject.getLong("userId"));
		user.setUsername(jsonObject.getString("username"));
		//user.setPassword(jsonObject.getString("password"));
		user.setSex(jsonObject.getLong("sex"));
		user.setAge(jsonObject.getLong("age"));
		user.setEmail(jsonObject.getString("email"));
		user.setMobile(jsonObject.getString("mobile"));
		//user.setStatus(Integer.parseInt((String)jsonObject.get("status")));
		//得到用戶角色信息
		Long roleId = jsonObject.getLong("roleId");
		List<Long> roleIdList= new ArrayList<Long>();
		roleIdList.add(roleId);
		user.setRoleIdList(roleIdList);
		//ValidatorUtils.validateEntity(user, AddGroup.class);

		//user.setCreateUserId(getUserId());
		sysUserService.update(user);
		return AppBaseResult.success();


        /*logger.info("AppUpdateController 修改",appBaseResult.decryptData());
        HashMap<String,Object> params = new Gson().fromJson(appBaseResult.decryptData(),HashMap.class);
		appUpdateService.updateInfo(params);
        return AppBaseResult.success();*/
	}

	/**
	 * 修改用戶密码
	 */
	@ApiOperation(value="修改密码", notes="修改密码")
	@ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token", required = true,dataType = "string", paramType = "query", defaultValue = "")})
	@PostMapping("/appUpdate/updatePassword")
	public AppBaseResult updatePassword(@RequestBody AppBaseResult appBaseResult)throws Exception{

		HashMap<String,Object> pd = new Gson().fromJson(appBaseResult.toString(),HashMap.class);
		JSONObject jsonObject=JSONObject.fromObject(pd.get("data"));
		String password=jsonObject.getString("password");
		String newPassword=jsonObject.getString("newPassword");
		Assert.isBlank(newPassword, "新密码不为能空");
		Long userId=jsonObject.getLong("userId");
		SysUserEntity user = sysUserService.queryObject(userId);
		//sha256加密
		password = new Sha256Hash(password, user.getSalt()).toHex();
		//sha256加密
		newPassword = new Sha256Hash(newPassword, user.getSalt()).toHex();

		//更新密码
		int count = sysUserService.updatePassword(userId, password, newPassword);
		if(count == 0){
			return AppBaseResult.error("原密码不正确");
		}

		return AppBaseResult.success();
	}

    /**
     * 退出登录
     */
    @ApiOperation(value="退出登录", notes="退出登录")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token", required = true,dataType = "string", paramType = "query", defaultValue = "")})
    @PostMapping("/appUpdate/logout")
    public AppBaseResult logout(@RequestBody AppBaseResult appBaseResult)throws Exception{

        HashMap<String,Object> pd = new Gson().fromJson(appBaseResult.toString(),HashMap.class);
        JSONObject jsonObject=JSONObject.fromObject(pd.get("data"));
        Long userId=jsonObject.getLong("userid");
        sysUserTokenService.logout(userId);

        return AppBaseResult.success();
    }



	/**
	 * 删除
	 */
    @ApiOperation(value="删除", notes="删除")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token", required = true,dataType = "string", paramType = "query", defaultValue = "")})
	@PostMapping("/appUpdate/delete")
	public AppBaseResult delete(@RequestBody AppBaseResult appBaseResult)throws Exception{

        HashMap<String,Object> params = new Gson().fromJson(appBaseResult.decryptData(),HashMap.class);
		appUpdateService.deleteInfo(params);
        return AppBaseResult.success();
	}
	
}
