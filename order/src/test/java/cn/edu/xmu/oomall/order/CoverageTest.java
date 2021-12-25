package cn.edu.xmu.oomall.order;

import cn.edu.xmu.oomall.core.util.JacksonUtil;
import cn.edu.xmu.oomall.core.util.ReturnNo;
import cn.edu.xmu.oomall.core.util.ReturnObject;
import cn.edu.xmu.oomall.order.dao.OrderDao;
import cn.edu.xmu.oomall.order.microservice.*;
import cn.edu.xmu.oomall.order.microservice.bo.PaymentState;
import cn.edu.xmu.oomall.order.microservice.bo.RefundState;
import cn.edu.xmu.oomall.order.microservice.vo.*;
import cn.edu.xmu.oomall.order.model.bo.Order;
import cn.edu.xmu.oomall.order.model.bo.OrderItem;
import cn.edu.xmu.oomall.order.model.vo.*;
import cn.edu.xmu.privilegegateway.annotation.util.InternalReturnObject;
import cn.edu.xmu.privilegegateway.annotation.util.JwtHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = OrderApplication.class)
@AutoConfigureMockMvc
@Transactional
public class CoverageTest {

    static String adminToken;
    static final JwtHelper jwtHelper = new JwtHelper();
    String token_1;
    String token_2;
    String token_3;


    @Autowired private MockMvc mvc;
    @Autowired private OrderDao orderDao;

    @MockBean private ShopService shopService;
    @MockBean private TransactionService transactionService;
    @MockBean private GoodsService goodsService;
    @MockBean private CustomService customService;
    @MockBean private ActivityService activityService;
    @MockBean private CouponService couponService;
    @MockBean private FreightService freightService;

    @BeforeEach
    void init() {
        /////////////////////////////
        adminToken = jwtHelper.createToken(1L, "admin", 0L, 2, 0);
        token_1 = jwtHelper.createToken(1L, "lxc", 0L, 1, 3600);
        token_2 = jwtHelper.createToken(2L, "lxc", 0L, 1, 3600);
        token_3 = jwtHelper.createToken(3L, "lxc", 0L, 1, 3600);
        ///////////////////////////////
        ProductVo productVo = new ProductVo();
        productVo.setId(1L);
        productVo.setOnsaleId(1L);
        productVo.setName("123");
        AdvanceVo advanceVo=new AdvanceVo();
        advanceVo.setAdvancePayPrice(500L);
        advanceVo.setId(2L);
        advanceVo.setName("123000");
        advanceVo.setAdvancePayPrice(100L);
        advanceVo.setBeginTime(ZonedDateTime.parse("2021-11-10T11:00:00.000+08:00"));
        advanceVo.setEndTime(ZonedDateTime.parse("2022-11-20T11:00:00.000+08:00"));
        Mockito.when(activityService.queryOnlineAdvanceSaleInfo(Mockito.anyLong())).thenReturn(new InternalReturnObject<>(advanceVo));
        List<GroupOnStrategyVo> list=new ArrayList<>();
        list.add(new GroupOnStrategyVo(200,10));
        list.add(new GroupOnStrategyVo(500,50));
        GrouponActivityVo grouponActivityVo= new GrouponActivityVo(1L,"aaa",1L,list);
        Mockito.when(activityService.getOnlineGroupOnActivity(Mockito.anyLong())).thenReturn(new InternalReturnObject<>(grouponActivityVo));
        List<ProductRetVo> plist=new ArrayList<>();
        plist.add(new ProductRetVo(4226L,2677L,0L,null));
        plist.add(new ProductRetVo(4264L,2715L,0L,null));
        List<ProductPostVo> pPosts=new ArrayList<>();
        pPosts.add(new ProductPostVo(4226L,2677L,2L,50L,null));
        pPosts.add(new ProductPostVo(4264L,2715L,1L,50L,null));
        Mockito.when(couponService.calculateDiscount(pPosts)).thenReturn(new InternalReturnObject<>(plist));
        CouponActivityVo couponActivityVo=new CouponActivityVo("aaa",50,(byte)1,(byte)1,ZonedDateTime.parse("2021-11-10T11:00:00.000+08:00"),ZonedDateTime.parse("2021-11-10T11:00:00.000+08:00"),ZonedDateTime.parse("2021-11-20T11:00:00.000+08:00"),"123",1);
        Mockito.when(couponService.showOwnCouponActivityInfo(Mockito.anyLong(),Mockito.anyLong())).thenReturn(new InternalReturnObject<>(couponActivityVo));
        SimpleVo customerVo=new SimpleVo(1L,"h");
        Mockito.when(customService.getCustomerById(Mockito.anyLong())).thenReturn(new InternalReturnObject<>(customerVo));
        Mockito.when(customService.changeCustomerPoint(1L,new CustomerModifyPointsVo(-100L))).thenReturn(new InternalReturnObject<>(new CustomerModifyPointsVo(400L)));
        Mockito.when(customService.refundCoupon(1L,"aaa",1L)).thenReturn(new InternalReturnObject(new ReturnObject<>(ReturnNo.OK)));
        Mockito.when(customService.useCoupon(1L,"aaa",1L)).thenReturn(new InternalReturnObject(new ReturnObject<>(ReturnNo.OK)));
        Mockito.when(customService.isCouponExists(Mockito.anyLong(),Mockito.anyLong())).thenReturn(new InternalReturnObject(new ReturnObject<>(ReturnNo.OK)));
        List<FreightCalculatingPostVo> list1=new ArrayList<>();
        list1.add(new FreightCalculatingPostVo(1L,50,1L,500L));
        list1.add(new FreightCalculatingPostVo(2L,100,1L,100L));
        Mockito.when(freightService.calculateFreight(1L,list1)).thenReturn(new InternalReturnObject<>(new FreightCalculatingRetVo(50L,1L)));
        Mockito.when(goodsService.selectFullOnsale(1L)).thenReturn(new InternalReturnObject<>(new OnSaleVo(1L,50L,10,null,null,(byte)1,1L,5L,1,100,null,null,(byte)1,new ProductSimpleVo(1L,"a",null),new SimpleVo(1L,"b"),new SimpleVo(1L,"a"),new SimpleVo(1L,"hhh"))));
        Mockito.when(goodsService.selectFullOnsale(2677L)).thenReturn(new InternalReturnObject<>(new OnSaleVo(2677L,50L,10,null,null,(byte)1,1L,5L,1,100,null,null,(byte)1,new ProductSimpleVo(1L,"a",null),new SimpleVo(1L,"b"),new SimpleVo(1L,"a"),new SimpleVo(1L,"hhh"))));
        Mockito.when(goodsService.selectFullOnsale(2715L)).thenReturn(new InternalReturnObject<>(new OnSaleVo(2715L,50L,10,null,null,(byte)2,1L,5L,1,100,null,null,(byte)1,new ProductSimpleVo(1L,"a",null),new SimpleVo(1L,"b"),new SimpleVo(1L,"a"),new SimpleVo(1L,"hhh"))));
        Mockito.when(goodsService.selectFullOnsale(3674L)).thenReturn(new InternalReturnObject<>(new OnSaleVo(3674L,50L,1,null,null,(byte)1,1L,5L,1,100,null,null,(byte)1,new ProductSimpleVo(1L,"a",null),new SimpleVo(1L,"b"),new SimpleVo(1L,"a"),new SimpleVo(1L,"hhh"))));
        Mockito.when(goodsService.getProductDetails(1L)).thenReturn(new InternalReturnObject<>(new ProductVo(1L,new SimpleVo(1L,"a"),1L,1L,"kkk","123",null,100L,500L,10L,50,(byte)1,null,null,null,new SimpleVo(1L,"s"),true,2L)));
        Mockito.when(goodsService.getProductDetails(4226L)).thenReturn(new InternalReturnObject<>(new ProductVo(4226L,new SimpleVo(1L,"a"),1L,2677L,"kkk","123",null,100L,500L,10L,50,(byte)1,null,null,null,new SimpleVo(1L,"s"),true,2L)));
        Mockito.when(goodsService.getProductDetails(4264L)).thenReturn(new InternalReturnObject<>(new ProductVo(4264L,new SimpleVo(1L,"a"),1L,2715L,"kkk","123",null,100L,500L,10L,50,(byte)1,null,null,null,new SimpleVo(1L,"s"),true,2L)));
        Mockito.when(goodsService.getProductDetails(5223L)).thenReturn(new InternalReturnObject<>(new ProductVo(5223L,new SimpleVo(1L,"a"),1L,3674L,"kkk","123",null,100L,500L,10L,50,(byte)1,null,null,null,new SimpleVo(1L,"s"),true,2L)));
        Mockito.when(goodsService.decreaseOnSale(1L,2677L,new QuantityVo(2L))).thenReturn(new InternalReturnObject(new ReturnObject<>(ReturnNo.OK)));
        Mockito.when(goodsService.decreaseOnSale(1L,2715L,new QuantityVo(1L))).thenReturn(new InternalReturnObject(new ReturnObject<>(ReturnNo.OK)));
        Mockito.when(goodsService.increaseOnSale(1L,1L,new QuantityVo(50L))).thenReturn(new InternalReturnObject(new ReturnObject<>(ReturnNo.OK)));
        Mockito.when(shopService.getSimpleShopById(Mockito.anyLong())).thenReturn(new InternalReturnObject<>(new SimpleVo(1L,"aaa")));
        List<PaymentRetVo> listPayments=new ArrayList<>();
        listPayments.add(new PaymentRetVo(1L,null,1L,"2016102361242",(byte)0,null,500L,498L,(byte)1,null,null,null));
        PageVo<PaymentRetVo> p1=new PageVo<>(1,100,1,1,listPayments);
        Mockito.when(transactionService.listPayment(1L,"2016102361242",(byte)1,null,null,null,null)).thenReturn(new InternalReturnObject<>(p1));
        Mockito.when(transactionService.listPayment(0L,"2016102378405",null,null,null,1,10)).thenReturn(new InternalReturnObject<>(p1));
        List<RefundRetVo> listRefund=new ArrayList<>();
        listRefund.add(new RefundRetVo(1L,1L,null,1L,500L,(byte)1,"2016102361242",(byte)0));
        PageVo<RefundRetVo> p2=new PageVo<>(1,100,1,1,listRefund);
        Mockito.when(transactionService.listRefund(0L, "2016102361242", RefundState.FINISH_REFUND.getCode(), null, null, 1, 10)).thenReturn(new InternalReturnObject<>(p2));
        Mockito.when(transactionService.listRefund(1L,"2016102361242",(byte)1,null,null,null,null)).thenReturn(new InternalReturnObject<>(p2));
        Mockito.when(transactionService.listRefund(0L,"2016102378405",null,null,null,1,10)).thenReturn(new InternalReturnObject<>(p2));
        Mockito.when(transactionService.listPaymentInternal("2016102361242",null,null,null,1,10)).thenReturn(new InternalReturnObject<>(p1));
        Mockito.when(transactionService.listPaymentInternal("2016102361242", PaymentState.ALREADY_PAY.getCode(),null,null,1,10)).thenReturn(new InternalReturnObject<>(p1));
        Mockito.when(transactionService.requestRefund(new RefundVo("2016102361242",(byte)0,1L,null,100L,null))).thenReturn(new InternalReturnObject<>(new RefundRetVo(1L,1L,null,1L,100L,(byte)1,"2016102361242",(byte)0)));
        Mockito.when(transactionService.requestRefund(new RefundVo("2016102378405",(byte)0,1L,null,-228291L,null))).thenReturn(new InternalReturnObject<>(new RefundRetVo(1L,1L,null,1L,100L,(byte)1,"2016102361242",(byte)0)));
        //////////////////////////////
//        Mockito.when().thenReturn();
//        Mockito.when(goodsService.getProductDetails(Mockito.anyLong())).thenReturn(new InternalReturnObject<>(productVo));
//        Mockito.when(goodsService.selectFullOnsale(Mockito.anyLong())).thenReturn();

    }

    /**
     * 获得订单的所有状态
     * */
    @Test
    public void listAllOrderStateController() throws Exception{
        String responseString = mvc.perform(get("/orders/states")
                        .header("authorization", token_1)
                        .contentType("application/json;charset=UTF-8"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString = "{\"errno\":0,\"errmsg\":\"成功\"}";
        JSONAssert.assertEquals(expectString, responseString, false);
    }

    /**
     * 买家查询名下订单概要
     * */
    @Test
    public void listCustomerBriefOrdersController() throws Exception{
        String responseString = mvc.perform(get("/orders")
                        .header("authorization", token_1)
                        .contentType("application/json;charset=UTF-8"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString = "{\"errno\":0,\"data\":{\"total\":18,\"pages\":1,\"pageSize\":18,\"page\":1,\"list\":[{\"id\":145,\"customerId\":1,\"shopId\":1,\"pid\":0,\"state\":null,\"gmtCreate\":\"2016-10-26T00:23:03\",\"originPrice\":264513,\"discountPrice\":0,\"expressFee\":691,\"point\":0,\"grouponId\":null,\"advancesaleId\":null,\"shipmentSn\":\"邮政小包\"},{\"id\":143,\"customerId\":1,\"shopId\":1,\"pid\":0,\"state\":null,\"gmtCreate\":\"2016-10-26T00:21:50\",\"originPrice\":104268,\"discountPrice\":0,\"expressFee\":505,\"point\":0,\"grouponId\":null,\"advancesaleId\":null,\"shipmentSn\":\"邮政小包\"},{\"id\":140,\"customerId\":1,\"shopId\":1,\"pid\":0,\"state\":null,\"gmtCreate\":\"2016-10-26T00:18:25\",\"originPrice\":228389,\"discountPrice\":0,\"expressFee\":406,\"point\":0,\"grouponId\":null,\"advancesaleId\":null,\"shipmentSn\":\"邮政小包\"},{\"id\":134,\"customerId\":1,\"shopId\":1,\"pid\":0,\"state\":null,\"gmtCreate\":\"2016-10-26T00:14:47\",\"originPrice\":79618,\"discountPrice\":0,\"expressFee\":662,\"point\":0,\"grouponId\":null,\"advancesaleId\":null,\"shipmentSn\":\"邮政小包\"},{\"id\":132,\"customerId\":1,\"shopId\":1,\"pid\":0,\"state\":null,\"gmtCreate\":\"2016-10-26T00:12:51\",\"originPrice\":25841,\"discountPrice\":0,\"expressFee\":542,\"point\":0,\"grouponId\":null,\"advancesaleId\":null,\"shipmentSn\":\"邮政小包\"},{\"id\":62,\"customerId\":1,\"shopId\":1,\"pid\":0,\"state\":null,\"gmtCreate\":\"2016-10-23T15:33:09\",\"originPrice\":91173,\"discountPrice\":0,\"expressFee\":378,\"point\":0,\"grouponId\":null,\"advancesaleId\":null,\"shipmentSn\":\"邮政小包\"},{\"id\":41,\"customerId\":1,\"shopId\":1,\"pid\":0,\"state\":null,\"gmtCreate\":\"2016-10-23T14:39:45\",\"originPrice\":65250,\"discountPrice\":0,\"expressFee\":573,\"point\":0,\"grouponId\":null,\"advancesaleId\":null,\"shipmentSn\":\"邮政小包\"},{\"id\":37,\"customerId\":1,\"shopId\":1,\"pid\":0,\"state\":null,\"gmtCreate\":\"2016-10-23T14:33:32\",\"originPrice\":78414,\"discountPrice\":0,\"expressFee\":121,\"point\":0,\"grouponId\":null,\"advancesaleId\":null,\"shipmentSn\":\"邮政小包\"},{\"id\":36,\"customerId\":1,\"shopId\":1,\"pid\":0,\"state\":null,\"gmtCreate\":\"2016-10-23T14:32:01\",\"originPrice\":50208,\"discountPrice\":0,\"expressFee\":968,\"point\":0,\"grouponId\":null,\"advancesaleId\":null,\"shipmentSn\":\"邮政小包\"},{\"id\":29,\"customerId\":1,\"shopId\":1,\"pid\":0,\"state\":null,\"gmtCreate\":\"2016-10-23T12:29:50\",\"originPrice\":73719,\"discountPrice\":0,\"expressFee\":388,\"point\":0,\"grouponId\":null,\"advancesaleId\":null,\"shipmentSn\":\"邮政小包\"},{\"id\":31,\"customerId\":1,\"shopId\":1,\"pid\":0,\"state\":null,\"gmtCreate\":\"2016-10-23T12:29:50\",\"originPrice\":758877,\"discountPrice\":0,\"expressFee\":286,\"point\":0,\"grouponId\":null,\"advancesaleId\":null,\"shipmentSn\":\"邮政小包\"},{\"id\":33,\"customerId\":1,\"shopId\":1,\"pid\":0,\"state\":null,\"gmtCreate\":\"2016-10-23T12:29:50\",\"originPrice\":854007,\"discountPrice\":0,\"expressFee\":937,\"point\":0,\"grouponId\":null,\"advancesaleId\":null,\"shipmentSn\":\"邮政小包\"},{\"id\":10,\"customerId\":1,\"shopId\":1,\"pid\":0,\"state\":null,\"gmtCreate\":\"2016-10-23T12:26:43\",\"originPrice\":1277367,\"discountPrice\":0,\"expressFee\":233,\"point\":0,\"grouponId\":null,\"advancesaleId\":null,\"shipmentSn\":\"邮政小包\"},{\"id\":30,\"customerId\":1,\"shopId\":1,\"pid\":0,\"state\":null,\"gmtCreate\":\"2016-10-23T12:14:40\",\"originPrice\":66366,\"discountPrice\":0,\"expressFee\":890,\"point\":0,\"grouponId\":null,\"advancesaleId\":null,\"shipmentSn\":\"邮政小包\"},{\"id\":32,\"customerId\":1,\"shopId\":1,\"pid\":0,\"state\":null,\"gmtCreate\":\"2016-10-23T12:14:40\",\"originPrice\":95736,\"discountPrice\":0,\"expressFee\":759,\"point\":0,\"grouponId\":null,\"advancesaleId\":null,\"shipmentSn\":\"邮政小包\"},{\"id\":34,\"customerId\":1,\"shopId\":1,\"pid\":0,\"state\":null,\"gmtCreate\":\"2016-10-23T12:14:40\",\"originPrice\":1170671,\"discountPrice\":0,\"expressFee\":410,\"point\":0,\"grouponId\":null,\"advancesaleId\":null,\"shipmentSn\":\"邮政小包\"},{\"id\":4,\"customerId\":1,\"shopId\":1,\"pid\":0,\"state\":null,\"gmtCreate\":\"2016-10-23T12:13:18\",\"originPrice\":82349,\"discountPrice\":0,\"expressFee\":112,\"point\":0,\"grouponId\":null,\"advancesaleId\":null,\"shipmentSn\":\"邮政小包\"},{\"id\":1,\"customerId\":1,\"shopId\":1,\"pid\":0,\"state\":null,\"gmtCreate\":\"2016-10-23T12:07:00\",\"originPrice\":799121,\"discountPrice\":0,\"expressFee\":545,\"point\":0,\"grouponId\":null,\"advancesaleId\":null,\"shipmentSn\":\"邮政小包\"}]},\"errmsg\":\"成功\"}";
        JSONAssert.assertEquals(expectString, responseString, true);
    }

    /**
     * 新建订单
     * */
    @Test
    public void insertOrderByCustom() throws Exception{
        //======预售========
//        SimpleOrderVo vo1 = new SimpleOrderVo(null, "lxc", 1604L, "厦门大学", "15165666666", "没有留言",
//                2L, null, 666L, 100L);
//        List<SimpleOrderItemVo> list1 = new ArrayList<>();
//        SimpleOrderItemVo item1 = new SimpleOrderItemVo(1555L, 6L, 10L, null, null, null, null);
//        list1.add(item1);
//        vo1.setOrderItems(list1);
//        String requestJSON = JacksonUtil.toJson(vo1);
        String json = "{\"orderItems\": [\n" +
                "{\"productId\": 4226, \"onsaleId\": 2677,  \"quantity\": 2}," +
                "{\"productId\": 4264, \"onsaleId\": 2715,  \"quantity\": 1}" +
                "  ], \"consignee\": \"李存伟\",\"regionId\": 1604, \"address\": \"翔安南路1号\", \"mobile\": \"13959207496\", \"point\": 100}";
        String responseString = mvc.perform(post("/orders")
                        .header("authorization", adminToken)
                        .contentType("application/json;charset=UTF-8")
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        //=====团购========

        //=====优惠券======

    }
//    @Test
//    public void insertOrderByCustom2() throws Exception {
//        //======预售========
//        SimpleOrderVo vo1 = new SimpleOrderVo(null, "lxc", 1604L, "厦门大学", "15165666666", "没有留言",
//                2L, null, 666L, 100L);
//        List<SimpleOrderItemVo> list1 = new ArrayList<>();
//        SimpleOrderItemVo item1 = new SimpleOrderItemVo(1555L, 6L, 10L, null, null, null, null);
//        list1.add(item1);
//        vo1.setOrderItems(list1);
//        String requestJSON = JacksonUtil.toJson(vo1);
//        String responseString = mvc.perform(post("/orders")
//                .header("authorization", token_1)
//                .contentType("application/json;charset=UTF-8")
//                .content(requestJSON))
//                .andExpect(status().isForbidden())
//                .andExpect(content().contentType("application/json;charset=UTF-8"))
//                .andReturn().getResponse().getContentAsString();
//        String expectString = "{\"errno\":505,\"errmsg\":\"操作的资源id不是自己的对象\"}";
//        JSONAssert.assertEquals(expectString, responseString, false);
//    }

    /**
     * 买家查询订单完整信息
     * */
    @Test
    public void getCustomerWholeOrderController() throws Exception{
        String responseString = mvc.perform(get("/orders/1")
                        .header("authorization", token_1)
                        .contentType("application/json;charset=UTF-8"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
    }

    /**
     * 买家修改本人名下订单
     * */
    @Test
    public void updateCustomerOrderController() throws Exception{
        UpdateOrderVo updateOrderVo = new UpdateOrderVo(null,2417L,null,"13900000000");
        Order order=new Order();
        order.setId(1L);
        order.setState(201);
        orderDao.updateOrder(order);
        String responseString = mvc.perform(put("/orders/1")
                        .header("authorization", token_1)
                        .contentType("application/json;charset=UTF-8")
                        .content(JacksonUtil.toJson(updateOrderVo)))
                .andExpect(status().isOk())//TODO: qm的订单全是400状态...
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
    }

    /**
     * 买家逻辑删除本人名下订单
     * */
    @Test
    public void deleteOrderByCustom() throws Exception{
        String responseString = mvc.perform(delete("/orders/1")
                        .header("authorization", token_1)
                        .contentType("application/json;charset=UTF-8"))
                .andExpect(status().isOk())//STATENOTALLOW
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString = "{\"errno\":0,\"errmsg\":\"成功\"}";
        JSONAssert.assertEquals(expectString, responseString, false);
    }

    /**
     * 买家取消本人名下订单
     * */
    @Test
    public void cancelOrderByCustomer() throws Exception{
        String json = "{\"orderItems\": [\n" +
                "{\"productId\": 4226, \"onsaleId\": 2677,  \"quantity\": 2}," +
                "{\"productId\": 4264, \"onsaleId\": 2715,  \"quantity\": 1}" +
                "  ], \"consignee\": \"李存伟\",\"regionId\": 1604, \"address\": \"翔安南路1号\", \"mobile\": \"13959207496\", \"point\": 100}";
        String responseString1 = mvc.perform(post("/orders")
                .header("authorization", adminToken)
                .contentType("application/json;charset=UTF-8")
                .content(json))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        System.out.println(responseString1);
        String responseString = mvc.perform(get("/orders/31501/cancel")
                .header("authorization", adminToken)
                .contentType("application/json;charset=UTF-8"))
                .andExpect(status().isForbidden())//STATENOTALLOW
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
    }

    /**
     * 买家标记确认收货
     * */
    @Test
    public void confirmOrder() throws Exception{
        String responseString = mvc.perform(put("/orders/1/confirm")
                        .header("authorization", token_1)
                        .contentType("application/json;charset=UTF-8"))
                .andExpect(status().isOk())//STATENOTALLOW
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString = "{\"errno\":507,\"errmsg\":\"当前状态禁止此操作\"}";
        JSONAssert.assertEquals(expectString, responseString, false);
    }

    /**
     * 店家查询商户所有订单概要
     * */
    @Test
    public void listBriefOrdersByShopId() throws Exception{
        String responseString = mvc.perform(get("/shops/1/orders")
                        .header("authorization", adminToken)
                        .contentType("application/json;charset=UTF-8"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString = "{\"errno\":0,\"data\":{\"total\":2500,\"pages\":250,\"pageSize\":10,\"page\":1,\"list\":[{\"id\":1,\"customerId\":1,\"shopId\":1,\"pid\":0,\"state\":null,\"gmtCreate\":\"2016-10-23T12:07:00\",\"originPrice\":799121,\"discountPrice\":0,\"expressFee\":545,\"point\":0,\"grouponId\":null,\"advancesaleId\":null,\"shipmentSn\":\"邮政小包\"},{\"id\":2,\"customerId\":2,\"shopId\":1,\"pid\":0,\"state\":null,\"gmtCreate\":\"2016-10-23T12:08:43\",\"originPrice\":4659,\"discountPrice\":0,\"expressFee\":749,\"point\":0,\"grouponId\":null,\"advancesaleId\":null,\"shipmentSn\":\"邮政小包\"},{\"id\":4,\"customerId\":1,\"shopId\":1,\"pid\":0,\"state\":null,\"gmtCreate\":\"2016-10-23T12:13:18\",\"originPrice\":82349,\"discountPrice\":0,\"expressFee\":112,\"point\":0,\"grouponId\":null,\"advancesaleId\":null,\"shipmentSn\":\"邮政小包\"},{\"id\":7,\"customerId\":4,\"shopId\":1,\"pid\":0,\"state\":null,\"gmtCreate\":\"2016-10-23T12:23:32\",\"originPrice\":25730,\"discountPrice\":0,\"expressFee\":314,\"point\":0,\"grouponId\":null,\"advancesaleId\":null,\"shipmentSn\":\"邮政小包\"},{\"id\":10,\"customerId\":1,\"shopId\":1,\"pid\":0,\"state\":null,\"gmtCreate\":\"2016-10-23T12:26:43\",\"originPrice\":1277367,\"discountPrice\":0,\"expressFee\":233,\"point\":0,\"grouponId\":null,\"advancesaleId\":null,\"shipmentSn\":\"邮政小包\"},{\"id\":13,\"customerId\":7,\"shopId\":1,\"pid\":0,\"state\":null,\"gmtCreate\":\"2016-10-23T12:46:24\",\"originPrice\":93930,\"discountPrice\":0,\"expressFee\":222,\"point\":0,\"grouponId\":null,\"advancesaleId\":null,\"shipmentSn\":\"邮政小包\"},{\"id\":14,\"customerId\":7,\"shopId\":1,\"pid\":0,\"state\":null,\"gmtCreate\":\"2016-10-23T12:47:33\",\"originPrice\":14516,\"discountPrice\":0,\"expressFee\":413,\"point\":0,\"grouponId\":null,\"advancesaleId\":null,\"shipmentSn\":\"邮政小包\"},{\"id\":15,\"customerId\":2,\"shopId\":1,\"pid\":0,\"state\":null,\"gmtCreate\":\"2016-10-23T12:48:19\",\"originPrice\":503387,\"discountPrice\":0,\"expressFee\":400,\"point\":0,\"grouponId\":null,\"advancesaleId\":null,\"shipmentSn\":\"邮政小包\"},{\"id\":16,\"customerId\":7,\"shopId\":1,\"pid\":0,\"state\":null,\"gmtCreate\":\"2016-10-23T12:49:54\",\"originPrice\":684667,\"discountPrice\":0,\"expressFee\":759,\"point\":0,\"grouponId\":null,\"advancesaleId\":null,\"shipmentSn\":\"邮政小包\"},{\"id\":17,\"customerId\":2,\"shopId\":1,\"pid\":0,\"state\":null,\"gmtCreate\":\"2016-10-23T12:50:08\",\"originPrice\":244101,\"discountPrice\":0,\"expressFee\":596,\"point\":0,\"grouponId\":null,\"advancesaleId\":null,\"shipmentSn\":\"邮政小包\"}]},\"errmsg\":\"成功\"}";
        JSONAssert.assertEquals(expectString, responseString, true);
    }

    /**
     * 店家修改订单
     * */
    @Test
    public void updateOrderComment() throws Exception{
        OrderVo orderVo = new OrderVo("lalalalademaxiya");

        String responseString = mvc.perform(put("/shops/1/orders/1")
                        .header("authorization", adminToken)
                        .contentType("application/json;charset=UTF-8")
                        .content(JacksonUtil.toJson(orderVo)))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString = "{\"errno\":0,\"errmsg\":\"成功\"}";
        JSONAssert.assertEquals(expectString, responseString, false);
    }

    /**
     * 店家查询店内订单完整信息
     * */
    @Test
    public void getOrderDetail() throws Exception{
        String responseString = mvc.perform(get("/shops/1/orders/1")
                .header("authorization", adminToken)
                .contentType("application/json;charset=UTF-8"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
    }

    /**
     * 管理员取消本店铺订单
     * */
    @Test
    public void cancelOrderByShop() throws Exception{
        String responseString = mvc.perform(delete("/shops/1/orders/1")
                        .header("authorization", adminToken)
                        .contentType("application/json;charset=UTF-8"))
                .andExpect(status().isOk())//STATENOTALLOW
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString = "{\"errno\":507,\"errmsg\":\"当前状态禁止此操作\"}";
        JSONAssert.assertEquals(expectString, responseString, false);
    }

    /**
     * 店家对订单标记发货
     * */
    @Test
    public void deliverByShop() throws Exception{
        MarkShipmentVo markShipmentVo = new MarkShipmentVo("lalalalalualua");

        String responseString = mvc.perform(put("/shops/1/orders/1/deliver")
                        .header("authorization", token_1)
                        .contentType("application/json;charset=UTF-8")
                        .content(JacksonUtil.toJson(markShipmentVo)))
                .andExpect(status().isOk())//STATENOTALLOW
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
    }

    /**
     * 查询自己订单的支付信息
     * */
    @Test
    public void getPaymentByOrderId() throws Exception{
        String responseString = mvc.perform(get("/orders/1/payment")
                        .header("authorization", token_1)
                        .contentType("application/json;charset=UTF-8"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
    }

    /**
     * 确认团购订单
     * */
    @Test
    public void confirmGrouponOrder() throws Exception{
//        String json = "{\"orderItems\": [\n" +
//                "{\"productId\": 4264, \"onsaleId\": 2715,  \"quantity\": 1}" +
//                "  ], \"consignee\": \"李存伟\",\"regionId\": 1604, \"address\": \"翔安南路1号\", \"mobile\": \"13959207496\", \"point\": 100, \"grouponId\": 1}";
//        String responseString1 = mvc.perform(post("/orders")
//                .header("authorization", adminToken)
//                .contentType("application/json;charset=UTF-8")
//                .content(json))
//                .andExpect(status().isOk())
//                .andExpect(content().contentType("application/json;charset=UTF-8"))
//                .andReturn().getResponse().getContentAsString();
//        System.out.println(responseString1);
        Order order=new Order();
        order.setId(2L);
        order.setGrouponId(1L);
        order.setState(300);
        orderDao.updateOrder(order);
        String responseString = mvc.perform(put("/internal/shops/1/grouponorders/2/confirm")
                        .header("authorization", adminToken)
                        .contentType("application/json;charset=UTF-8"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
    }

    /**
     * 内部API取消订单
     * */
    @Test
    public void internalCancleOrderByShop() throws Exception{
        String responseString = mvc.perform(put("/internal/shops/1/orders/2/cancel")
                .header("authorization", adminToken)
                .contentType("application/json;charset=UTF-8"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString = "{\"errno\":507,\"errmsg\":\"当前状态禁止此操作\"}";
        JSONAssert.assertEquals(expectString, responseString, false);
    }

    /**
     * 内部API管理员建立售后订单
     * */
    @Test
    public void createAftersaleOrder() throws Exception{
        AftersaleRecVo aftersaleRecVo = new AftersaleRecVo(new AftersaleOrderitemRecVo(5223L,3674L,1L),
                "fz",2417L,null,"13900000000",null,null);

        String responseString = mvc.perform(post("/internal/shops/1/orders")
                        .header("authorization", adminToken)
                        .contentType("application/json;charset=UTF-8")
                        .content(JacksonUtil.toJson(aftersaleRecVo)))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
    }

    /**
     * 查询自己订单的退款信息
     * */
    @Test
    public void listOrderRefunds() throws Exception{
        String responseString = mvc.perform(get("/orders/1/refund")
                        .header("authorization", token_1)
                        .contentType("application/json;charset=UTF-8"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
    }

    /**
     * 根据Itemid找item
     * */
    @Test
    public void getOrderItemById() throws Exception{
        String responseString = mvc.perform(get("/internal/orderitems/1")
                        .header("authorization", token_1)
                        .contentType("application/json;charset=UTF-8"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
    }

    /**
     * 根据itemid找Payment
     * */
    @Test
    public void getPaymentByOrderItemId() throws Exception{
        String responseString = mvc.perform(get("/internal/orderitems/11864/payment")
                        .header("authorization", token_1)
                        .contentType("application/json;charset=UTF-8"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
    }

    /**
     * orderId查item
     * */
    @Test
    public void listOrderItemsByOrderId() throws Exception{
        String responseString = mvc.perform(get("/internal/orders/1/orderitems")
                        .header("authorization", token_1)
                        .contentType("application/json;charset=UTF-8"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString = "{\"errno\":0,\"data\":[{\"id\":11864,\"orderId\":1,\"shopId\":1,\"productId\":5223,\"onsaleId\":3674,\"name\":\"长通型牛奶杯\",\"quantity\":8,\"price\":916,\"discountPrice\":0,\"point\":0,\"couponId\":null,\"couponActivityId\":null,\"customerId\":null},{\"id\":13747,\"orderId\":1,\"shopId\":1,\"productId\":3011,\"onsaleId\":1462,\"name\":\"味好美五香粉10\",\"quantity\":7,\"price\":27560,\"discountPrice\":0,\"point\":0,\"couponId\":null,\"couponActivityId\":null,\"customerId\":null},{\"id\":36420,\"orderId\":1,\"shopId\":1,\"productId\":5209,\"onsaleId\":3660,\"name\":\"纸筒\",\"quantity\":8,\"price\":16908,\"discountPrice\":0,\"point\":0,\"couponId\":null,\"couponActivityId\":null,\"customerId\":null},{\"id\":36433,\"orderId\":1,\"shopId\":1,\"productId\":5222,\"onsaleId\":3673,\"name\":\"小倍尔爽\",\"quantity\":3,\"price\":52221,\"discountPrice\":0,\"point\":0,\"couponId\":null,\"couponActivityId\":null,\"customerId\":null},{\"id\":37062,\"orderId\":1,\"shopId\":1,\"productId\":1756,\"onsaleId\":207,\"name\":\"玉兰油清透型香皂\",\"quantity\":7,\"price\":25656,\"discountPrice\":0,\"point\":0,\"couponId\":null,\"couponActivityId\":null,\"customerId\":null},{\"id\":37382,\"orderId\":1,\"shopId\":1,\"productId\":2076,\"onsaleId\":527,\"name\":\"松花鸭皮蛋(六枚)\",\"quantity\":7,\"price\":2763,\"discountPrice\":0,\"point\":0,\"couponId\":null,\"couponActivityId\":null,\"customerId\":null},{\"id\":46590,\"orderId\":1,\"shopId\":1,\"productId\":3094,\"onsaleId\":1545,\"name\":\"海天柱候酱\",\"quantity\":5,\"price\":16805,\"discountPrice\":0,\"point\":0,\"couponId\":null,\"couponActivityId\":null,\"customerId\":null},{\"id\":48600,\"orderId\":1,\"shopId\":1,\"productId\":5104,\"onsaleId\":3555,\"name\":\"金峻日太空杯\",\"quantity\":6,\"price\":3998,\"discountPrice\":0,\"point\":0,\"couponId\":null,\"couponActivityId\":null,\"customerId\":null}],\"errmsg\":\"成功\"}";
        JSONAssert.assertEquals(expectString, responseString, true);
    }

    /**
     * orderSn查orderId
     * */
    @Test
    public void getOrderIdByOrderSn() throws Exception{
        String responseString = mvc.perform(get("/internal/orderid")
                        .header("authorization", token_1)
                        .queryParam("orderSn","2016102361242")
                        .contentType("application/json;charset=UTF-8"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString = "{\"errno\":0,\"data\":{\"id\":1},\"errmsg\":\"成功\"}";
        JSONAssert.assertEquals(expectString, responseString, true);
    }


}
