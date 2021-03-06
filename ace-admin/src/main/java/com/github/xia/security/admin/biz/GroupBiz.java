package com.github.xia.security.admin.biz;

import com.github.xia.security.admin.entity.BaseGroup;
import com.github.xia.security.admin.entity.BaseMenu;
import com.github.xia.security.admin.entity.BaseResourceAuthority;
import com.github.xia.security.admin.mapper.BaseGroupMapper;
import com.github.xia.security.admin.mapper.BaseMenuMapper;
import com.github.xia.security.admin.mapper.BaseResourceAuthorityMapper;
import com.github.xia.security.admin.mapper.BaseUserMapper;
import com.github.xia.security.admin.vo.AuthorityMenuTree;
import com.github.xia.security.admin.vo.GroupUsers;
import com.github.xia.security.common.biz.BaseBiz;
import com.github.xia.security.common.constant.CommonConstant;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @explain：
 * @author: XIA
 * @date: 2020-03-11
 * @since: JDK 1.8
 * @version: 1.0
 */
@Service
public class GroupBiz extends BaseBiz<BaseGroupMapper, BaseGroup> {

    @Autowired
    private BaseUserMapper userMapper;

    @Autowired
    private BaseResourceAuthorityMapper resourceAuthorityMapper;

    @Autowired
    private BaseMenuMapper menuMapper;

    @Override
    public void insertSelective(BaseGroup entity) {
        if (CommonConstant.ROOT == entity.getParentId()) {
            entity.setPath("/" + entity.getCode());
        } else {
            BaseGroup parent = this.selectById(entity.getParentId());
            entity.setPath(parent.getPath() + "/" + entity.getCode());
        }
        super.insertSelective(entity);
    }

    @Override
    public void updateById(BaseGroup entity) {
        if (CommonConstant.ROOT == entity.getParentId()) {
            entity.setPath("/" + entity.getCode());
        } else {
            BaseGroup parent = this.selectById(entity.getParentId());
            entity.setPath(parent.getPath() + "/" + entity.getCode());
        }
        super.updateById(entity);
    }

    /**
     * 获取群组关联用户
     * @param groupId
     * @return
     */
    public GroupUsers getGroupUsers(int groupId) {
        return new GroupUsers(userMapper.selectMemberByGroupId(groupId), userMapper.selectLeaderByGroupId(groupId));
    }

    /**
     * 变更群主所分配用户
     * @param groupId
     * @param members
     * @param leaders
     */
    public void modifyGroupUsers(int groupId, String members, String leaders) {
        mapper.deleteGroupLeadersById(groupId);
        mapper.deleteGroupMembersById(groupId);
        if (!StringUtils.isEmpty(members)) {
            String[] mem = members.split(",");
            for (String m : mem) {
                mapper.insertGroupMembersById(groupId, Integer.parseInt(m));
            }
        }
        if (!StringUtils.isEmpty(leaders)) {
            String[] mem = leaders.split(",");
            for (String m : mem) {
                mapper.insertGroupLeadersById(groupId, Integer.parseInt(m));
            }
        }
    }

    /**
     * 变更群组关联的菜单
     * @param groupId
     * @param menuTrees
     */
    public void modifyAuthorityMenu(int groupId, List<AuthorityMenuTree> menuTrees) {
        resourceAuthorityMapper.deleteByAuthorityIdAndResourceType(groupId + "", CommonConstant.RESOURCE_TYPE_MENU);
        BaseResourceAuthority authority = null;
        for (AuthorityMenuTree menuTree : menuTrees) {
            authority = new BaseResourceAuthority(CommonConstant.AUTHORITY_TYPE_GROUP, CommonConstant.RESOURCE_TYPE_MENU);
            authority.setAuthorityId(groupId + "");
            authority.setResourceId(menuTree.getId() + "");
            authority.setParentId("-1");
            resourceAuthorityMapper.insertSelective(authority);
        }
    }

    /**
     * 分配资源权限
     * @param groupId
     * @param menuId
     * @param elementId
     */
    public void modifyAuthorityElement(int groupId,int menuId,int elementId){
        BaseResourceAuthority authority = new BaseResourceAuthority(CommonConstant.AUTHORITY_TYPE_GROUP,CommonConstant.RESOURCE_TYPE_BTN);
        authority.setAuthorityId(groupId + "");
        authority.setResourceId(elementId + "");
        authority.setParentId("-1");
        resourceAuthorityMapper.insertSelective(authority);
    }

    /**
     * 移除资源权限
     * @param groupId
     * @param menuId
     * @param elementId
     */
    public void removeAuthorityElement(int groupId, int menuId, int elementId) {
        BaseResourceAuthority authority = new BaseResourceAuthority();
        authority.setAuthorityId(groupId+"");
        authority.setResourceId(elementId + "");
        authority.setParentId("-1");
        resourceAuthorityMapper.delete(authority);
    }

    /**
     * 获得资源权限
     * @param groupId
     */
    public List<Integer> getAuthorityElement(int groupId) {
        BaseResourceAuthority authority = new BaseResourceAuthority(CommonConstant.AUTHORITY_TYPE_GROUP,CommonConstant.RESOURCE_TYPE_BTN);
        authority.setAuthorityId(groupId+"");
        List<BaseResourceAuthority> authorities = resourceAuthorityMapper.select(authority);
        List<Integer> ids = new ArrayList<Integer>();
        for(BaseResourceAuthority auth:authorities){
            ids.add(Integer.parseInt(auth.getResourceId()));
        }
        return ids;
    }


    /**
     * 获取群主关联的菜单
     * @param groupId
     * @return
     */
    public List<AuthorityMenuTree> getAuthorityMenu(int groupId){
        List<BaseMenu> menus = menuMapper.selectMenuByAuthorityId(String.valueOf(groupId), CommonConstant.AUTHORITY_TYPE_GROUP);
        List<AuthorityMenuTree> trees = new ArrayList<AuthorityMenuTree>();
        AuthorityMenuTree node = null;
        for (BaseMenu menu : menus) {
            node = new AuthorityMenuTree();
            node.setText(menu.getTitle());
            BeanUtils.copyProperties(menu, node);
            trees.add(node);
        }
        return trees;
    }

}
