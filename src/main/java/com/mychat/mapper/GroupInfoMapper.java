package com.mychat.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mychat.entity.po.GroupInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
* @author Administrator
* @description 针对表【group_info(群组表)】的数据库操作Mapper
* @createDate 2025-04-19 23:21:32
* @Entity com.mychat.entity.po.GroupInfo
*/
public interface GroupInfoMapper extends BaseMapper<GroupInfo> {

    /**
     * 获取群组列表
     * @param page              分页接口实现类Page对象
     * @param groupId           群组id
     * @param groupNameFuzzy    群组名称（支持模糊搜索）
     * @param groupOwnerId      群主id
     * @return IPage<GroupInfo>
     */
    IPage<GroupInfo> loadGroupList(Page<GroupInfo> page, @Param("groupId") String groupId, @Param("groupNameFuzzy") String groupNameFuzzy, @Param("groupOwnerId") String groupOwnerId);
}




