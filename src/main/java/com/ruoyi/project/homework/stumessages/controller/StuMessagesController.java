package com.ruoyi.project.homework.stumessages.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.ruoyi.common.utils.security.ShiroUtils;
import com.ruoyi.project.system.user.domain.User;
import com.ruoyi.project.system.user.domain.UserRole;
import com.ruoyi.project.system.user.mapper.UserRoleMapper;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import com.ruoyi.framework.aspectj.lang.annotation.Log;
import com.ruoyi.framework.aspectj.lang.enums.BusinessType;
import com.ruoyi.project.homework.stumessages.domain.StuMessages;
import com.ruoyi.project.homework.stumessages.service.IStuMessagesService;
import com.ruoyi.framework.web.controller.BaseController;
import com.ruoyi.framework.web.domain.AjaxResult;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.framework.web.page.TableDataInfo;

/**
 * 学生留言Controller
 * 
 * @author AbuCoder QQ932696181
 * @date 2022-04-10
 */
@Controller
@RequestMapping("/homework/stumessages")
public class StuMessagesController extends BaseController
{
    private String prefix = "homework/stumessages";

    @Autowired
    private IStuMessagesService stuMessagesService;
    @Autowired
    private UserRoleMapper userRoleMapper;


    @RequiresPermissions("homework:stumessages:view")
    @GetMapping()
    public String stumessages()
    {
        return prefix + "/stumessages";
    }

    /**
     * 查询学生留言列表
     */
    @RequiresPermissions("homework:stumessages:list")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(StuMessages stuMessages)
    {
        startPage();
        User sysUser = ShiroUtils.getSysUser();
        List<UserRole> userRoles = userRoleMapper.selectUserRoleByUserId(sysUser.getUserId());
        boolean isAdmin = false;
        List<StuMessages> list =stuMessagesService.selectStuMessagesList(stuMessages);

        for (UserRole userRole : userRoles) {
            long id = userRole.getRoleId();
            if (id == 1 || id == 2) {
               isAdmin = true;
            }
        }
        if (!isAdmin){
             list = list.stream().filter(item -> {
                 if (item.getCreateBy().equals(sysUser.getLoginName())){
                     return true;
                 }
                 if (ObjectUtils.isNotEmpty(item.getSendto())) {
                     String[] split = item.getSendto().split(",");
                     for (String loginName : split) {
                         if (loginName.equals(sysUser.getLoginName())) {
                             return true;
                         }
                     }
                 }
                 return false;
             }).collect(Collectors.toList());
        }

        return getDataTable(list);
    }

    /**
     * 导出学生留言列表
     */
    @RequiresPermissions("homework:stumessages:export")
    @Log(title = "学生留言", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    @ResponseBody
    public AjaxResult export(StuMessages stuMessages)
    {
        List<StuMessages> list = stuMessagesService.selectStuMessagesList(stuMessages);
        ExcelUtil<StuMessages> util = new ExcelUtil<StuMessages>(StuMessages.class);
        return util.exportExcel(list, "学生留言数据");
    }

    /**
     * 新增学生留言
     */
    @GetMapping("/add")
    public String add()
    {
        return prefix + "/add";
    }

    /**
     * 新增保存学生留言
     */
    @RequiresPermissions("homework:stumessages:add")
    @Log(title = "学生留言", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    @ResponseBody
    public AjaxResult addSave(StuMessages stuMessages)
    {
        return toAjax(stuMessagesService.insertStuMessages(stuMessages));
    }

    /**
     * 修改学生留言
     */
    @RequiresPermissions("homework:stumessages:edit")
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") Long id, ModelMap mmap)
    {
        StuMessages stuMessages = stuMessagesService.selectStuMessagesById(id);
        mmap.put("stuMessages", stuMessages);
        return prefix + "/edit";
    }

    /**
     * 修改保存学生留言
     */
    @RequiresPermissions("homework:stumessages:edit")
    @Log(title = "学生留言", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    @ResponseBody
    public AjaxResult editSave(StuMessages stuMessages)
    {
        return toAjax(stuMessagesService.updateStuMessages(stuMessages));
    }

    /**
     * 删除学生留言
     */
    @RequiresPermissions("homework:stumessages:remove")
    @Log(title = "学生留言", businessType = BusinessType.DELETE)
    @PostMapping( "/remove")
    @ResponseBody
    public AjaxResult remove(String ids)
    {
        return toAjax(stuMessagesService.deleteStuMessagesByIds(ids));
    }

    /**
     * 留言信息详情
     */
    @RequiresPermissions("homework:stumessages:detail")
    @Log(title = "查看留言详情", businessType = BusinessType.OTHER)
    @GetMapping("/detail/{id}")
    public String detail(@PathVariable("id") Long id, ModelMap mmap)
    {
        StuMessages stuMessages = stuMessagesService.selectStuMessagesById(id);
        mmap.put("messages", stuMessages);
        return prefix + "/detail";
    }
}
