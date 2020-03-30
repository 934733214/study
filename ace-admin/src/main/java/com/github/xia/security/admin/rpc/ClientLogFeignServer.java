package com.github.xia.security.admin.rpc;

import com.github.xia.security.admin.biz.GateClientBiz;
import com.github.xia.security.admin.biz.GateLogBiz;
import com.github.xia.security.admin.entity.GateClient;
import com.github.xia.security.admin.entity.GateLog;
import com.github.xia.security.common.constant.CommonConstant;
import com.github.xia.security.common.vo.ClientInfo;
import com.github.xia.security.common.vo.LogInfo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>网关客户操作日志 feign 服务</p>
 *
 * @author: XIA
 * @date: 2020-03-13
 * @since: JDK 1.8
 * @version: 1.0
 */
@RequestMapping("/clientLogFeignServer")
@Controller
public class ClientLogFeignServer {

    @Autowired
    private GateLogBiz gateLogBiz;

    @Autowired
    private GateClientBiz gateClientBiz;

    @RequestMapping(value = "/log/save", method = RequestMethod.POST,produces="application/json;charset=utf-8")
    public void saveLog(@RequestBody LogInfo info) {
        GateLog log = new GateLog();
        BeanUtils.copyProperties(info, log);
        gateLogBiz.insertSelective(log);
    }

    @RequestMapping(value = "/gate/client/{clientId}", method = RequestMethod.GET,produces="application/json;charset=utf-8")
    @ResponseBody
    public ClientInfo getGateClientInfo(@PathVariable("clientId") String clientId) {
        Example example = new Example(GateClient.class);
        example.createCriteria().andEqualTo("code",clientId);
        ClientInfo info = new ClientInfo();
        GateClient gateClient = gateClientBiz.selectByExample(example).get(0);
        BeanUtils.copyProperties(gateClient, info);
        info.setLocked(CommonConstant.BOOLEAN_NUMBER_TRUE.equals(gateClient.getLocked()));
        return info;
    }
}
