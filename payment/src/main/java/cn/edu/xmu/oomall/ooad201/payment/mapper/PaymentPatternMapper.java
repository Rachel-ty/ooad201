package cn.edu.xmu.oomall.ooad201.payment.mapper;

import cn.edu.xmu.oomall.ooad201.payment.model.po.PaymentPattern;
import cn.edu.xmu.oomall.ooad201.payment.model.po.PaymentPatternExample;
import java.util.List;

public interface PaymentPatternMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table oomall_payment_pattern
     *
     * @mbg.generated
     */
    int deleteByPrimaryKey(Long id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table oomall_payment_pattern
     *
     * @mbg.generated
     */
    int insert(PaymentPattern record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table oomall_payment_pattern
     *
     * @mbg.generated
     */
    int insertSelective(PaymentPattern record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table oomall_payment_pattern
     *
     * @mbg.generated
     */
    List<PaymentPattern> selectByExample(PaymentPatternExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table oomall_payment_pattern
     *
     * @mbg.generated
     */
    PaymentPattern selectByPrimaryKey(Long id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table oomall_payment_pattern
     *
     * @mbg.generated
     */
    int updateByPrimaryKeySelective(PaymentPattern record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table oomall_payment_pattern
     *
     * @mbg.generated
     */
    int updateByPrimaryKey(PaymentPattern record);
}