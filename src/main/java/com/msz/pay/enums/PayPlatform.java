package com.msz.pay.enums;

import com.lly835.bestpay.enums.BestPayTypeEnum;
import lombok.Getter;

@Getter
public enum PayPlatform {
    /****
     * 1  支付宝
     * 2  微信
     *
     * **/

    ALIPAY(1),
    WX(2),
    ;
    Integer code;

    PayPlatform(Integer code) {
        this.code = code;
    }

    /***
     * 获取的枚举和这里的比较
     * **/

    public static PayPlatform getByBestPayTypeEnum(BestPayTypeEnum bestPayTypeEnum){
        for (PayPlatform payPlatform : PayPlatform.values()) {
            if(bestPayTypeEnum.getPlatform().name().equals(payPlatform.name())){
                return payPlatform;
            }
        }
        throw new RuntimeException(bestPayTypeEnum.name());
    }
}
