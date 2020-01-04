package com.msz.pay.service.impl;

import com.lly835.bestpay.config.WxPayConfig;
import com.lly835.bestpay.enums.BestPayPlatformEnum;
import com.lly835.bestpay.enums.BestPayTypeEnum;
import com.lly835.bestpay.enums.OrderStatusEnum;
import com.lly835.bestpay.model.PayRequest;
import com.lly835.bestpay.model.PayResponse;
import com.lly835.bestpay.service.BestPayService;
import com.lly835.bestpay.service.impl.BestPayServiceImpl;
import com.msz.pay.dao.PayInfoMapper;
import com.msz.pay.enums.PayPlatform;
import com.msz.pay.pojo.PayInfo;
import com.msz.pay.service.IPayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
@Slf4j
@Service
public class PayService implements IPayService {

    @Autowired
    private BestPayService bestPayService;

    @Autowired
    private PayInfoMapper payInfoMapper;

    /***
     * 创建/发起支付
     *
     * **/
    @Override
    public PayResponse create(String orderId, BigDecimal amount,BestPayTypeEnum bestPayTypeEnum) {
        /**
         * 支付的时候保存到数据库
         *
         * */
        PayInfo payInfo = new PayInfo(Long.parseLong(orderId), PayPlatform.getByBestPayTypeEnum(bestPayTypeEnum).getCode(), OrderStatusEnum.NOTPAY.name(),amount);

        payInfoMapper.insertSelective(payInfo);
        PayRequest request = new PayRequest();
        request.setOrderName("4748924-最好的支付sdk");
        request.setOrderId(orderId);
        request.setOrderAmount(amount.doubleValue());
        request.setPayTypeEnum(bestPayTypeEnum);
        PayResponse payResponse = bestPayService.pay(request);
        return payResponse;

    }

    /**
     * 异步通知处理
     * 微信返回的回调处理
     * **/
    @Override
    public String asyncNotify(String notifyData) {
        /***
         * 1.签名校验
         * **/
        PayResponse payResponse = bestPayService.asyncNotify(notifyData);

        /**
         * 2.金额校验
         * 从数据库根据订单号查金额和异步通知的金额做校验
         * ***/
        PayInfo payInfo = payInfoMapper.selectByOrderNo(Long.parseLong(payResponse.getOrderId()));
        if(payInfo == null){

            /***
             * 发出告警
             * */
            throw new RuntimeException("通过orderNo查询到的结果是null");
        }
        /**
         * 判断支付的状态不是已支付
         * **/
        if(!payInfo.getPlatformStatus().equals(OrderStatusEnum.SUCCESS.name())){
             if(payInfo.getPayAmount().compareTo(BigDecimal.valueOf(payResponse.getOrderAmount())) != 0){
                 /***
                  * 发出告警
                  * */
                 throw new RuntimeException("异步通知中的金额和数据库金额不一致，orderNo"+payResponse.getOrderId());
             }

             /**
              * 更新字段 修改支付状态
              * */
            /***
             * 3.修改订单的支付状态
             * */
            payInfo.setPlatformStatus(OrderStatusEnum.SUCCESS.name());
            payInfo.setPlatformNumber(payResponse.getOutTradeNo());
            payInfo.setUpdateTime(null);
            payInfoMapper.updateByPrimaryKeySelective(payInfo);
        }
        /***
         * 4.告诉微信不要再通知了
         *
         * **/
        if(payResponse.getPayPlatformEnum() == BestPayPlatformEnum.WX){
            return "<xml>" +
                    "  <return_code><![CDATA[SUCCESS]]></return_code>" +
                    "  <return_msg><![CDATA[OK]]></return_msg>" +
                    "</xml>";
        }else if(payResponse.getPayPlatformEnum() == BestPayPlatformEnum.ALIPAY){
            return "success";
        }
        throw new RuntimeException("异步通知中错误的支付平台");
    }
}
