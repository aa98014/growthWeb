package com.suke.czx.modules.app.controller;


import com.google.gson.Gson;
import com.suke.czx.common.utils.*;
import com.suke.czx.common.validator.Assert;
import com.suke.czx.modules.sys.controller.AbstractController;
import com.suke.czx.modules.sys.entity.SysAdviceEntity;
import com.suke.czx.modules.sys.entity.SysUserEntity;
import com.suke.czx.modules.sys.service.SysGrowUpService;
import com.suke.czx.modules.user.entity.UserEntity;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 *
 */
@Api(value = "API - AppGradeController ", description = "APP版本管理")
@RestController
@RequestMapping("/app")
public class AppGrowUpController extends AbstractController{

    private org.slf4j.Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private SysGrowUpService sysGrowUpService;


    /**
     * 成长册列表
     */
    @ApiOperation(value="成长册列表", notes="成长册列表")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token", required = true,dataType = "string", paramType = "query", defaultValue = "")})
    @PostMapping("/growUp/list")
    public AppBaseResult list(@RequestBody AppBaseResult appBaseResult)throws Exception{

        HashMap<String,Object> params = new Gson().fromJson(appBaseResult.toString(),HashMap.class);
        JSONObject jsonObject=JSONObject.fromObject(params.get("data"));
        //查询列表数据
        Query query = new Query(jsonObject);
        query.isPaging(true);
        List<Map<String,Object>> growUpList = sysGrowUpService.queryList(query);

        /*List<HashMap<String,Object>> appUpdateList = appUpdateService.queryList(query);*/
        PageUtils pageUtil = new PageUtils(growUpList, query.getTotle(), query.getLimit(), query.getPage());
        return AppBaseResult.success().setEncryptData(pageUtil);
    }

    /**
     * 创建班级
     */
    @ApiOperation(value="新增成长册", notes="新增成长册")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token", required = true,dataType = "string", paramType = "query", defaultValue = "")})
    @PostMapping("/growUp/save")
    public AppBaseResult save(@RequestBody AppBaseResult appBaseResult)throws Exception{
        HashMap<String,Object> pd = new Gson().fromJson(appBaseResult.toString(),HashMap.class);
        JSONObject jsonObject=JSONObject.fromObject(pd.get("data"));
        MultipartFile multipartFile =null;
        String uuid = UUID.randomUUID().toString();
        if(jsonObject!=null&&jsonObject.containsKey("file")){
            JSONArray jsonArray=jsonObject.getJSONArray("file");
            String username =jsonObject.getString("username");
            if(jsonArray!=null){
                for (Object files:jsonArray) {
                    JSONObject file=(JSONObject) files;
                    String fileCode =file.getString("fileCode");
                    String fileName =file.getString("fileName");
                    if(StringUtils.isNotEmpty(fileCode)&&StringUtils.isNotEmpty(fileName)) {
                        multipartFile = BASE64DecodedMultipartFile.base64ToMultipart(fileCode);
                        sysGrowUpService.saveFile(multipartFile,fileName,username,uuid);
                    }
                }
            }
        }
        jsonObject.put("id",uuid);
        sysGrowUpService.save(jsonObject);
        return AppBaseResult.success();
    }

}
