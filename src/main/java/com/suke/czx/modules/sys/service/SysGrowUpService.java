package com.suke.czx.modules.sys.service;

import com.suke.czx.modules.sys.entity.SysLogEntity;

import java.util.List;
import java.util.Map;

public interface SysGrowUpService {

    /**
     * 保存成长册信息
     */
    void save(Map<String,Object> map);

    List<Map<String,Object>> queryList(Map<String,Object> map);
}
