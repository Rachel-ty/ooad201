package cn.edu.xmu.oomall.transaction.util.wechatpay.microservice.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author ziyi guo
 * @date 2021/11/30
 */
@Data
@NoArgsConstructor
public class WeChatRefundVo {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public class RefundAmountVo {
        private Integer refund;
        private Integer total;
        private String currency;
    }
    @NotBlank
    private String outTradeNo;

    @NotBlank
    private String outRefundNo;

    private String reason;

    private String notifyUrl;

    @NotNull
    private RefundAmountVo amount;


}