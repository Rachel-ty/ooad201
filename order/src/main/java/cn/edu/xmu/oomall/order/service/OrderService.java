package cn.edu.xmu.oomall.order.service;

import cn.edu.xmu.oomall.core.util.ReturnNo;
import cn.edu.xmu.oomall.core.util.ReturnObject;
import cn.edu.xmu.oomall.order.dao.OrderDao;
import cn.edu.xmu.oomall.order.microservice.*;
import cn.edu.xmu.oomall.order.microservice.vo.AdvanceVo;
import cn.edu.xmu.oomall.order.microservice.vo.GrouponActivityVo;
import cn.edu.xmu.oomall.order.microservice.vo.OnSaleVo;
import cn.edu.xmu.oomall.order.microservice.vo.ProductVo;
import cn.edu.xmu.oomall.order.model.bo.Order;
import cn.edu.xmu.oomall.order.model.bo.OrderItem;
import cn.edu.xmu.oomall.order.model.bo.OrderState;
import cn.edu.xmu.oomall.order.model.vo.*;
import cn.edu.xmu.privilegegateway.annotation.util.Common;
import cn.edu.xmu.privilegegateway.annotation.util.InternalReturnObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class OrderService {
    @Autowired
    OrderDao orderDao;

    @Autowired
    ActivityService activityService;

    @Autowired
    CouponService couponService;

    @Autowired
    FreightService freightService;

    @Autowired
    GoodsService goodsService;

    @Autowired
    CustomService customService;

    @Autowired
    ShopService shopService;

    /**
     * 新建订单
     * @param simpleOrderVo
     * @param userId
     * @param userName
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public ReturnObject insertOrder(SimpleOrderVo simpleOrderVo, Long userId, String userName) {
        if (simpleOrderVo.getGrouponId() != null) {
            InternalReturnObject<GrouponActivityVo> grouponsById = activityService.getGrouponsById(simpleOrderVo.getGrouponId());
            if (grouponsById.getErrno() != 0) {
                return new ReturnObject(ReturnNo.getByCode(grouponsById.getErrno()));
            }
            SimpleOrderItemVo simpleOrderItemVo = simpleOrderVo.getOrderItems().get(0);
            InternalReturnObject<ProductVo> productById = goodsService.getProductById(simpleOrderItemVo.getProductId());
            if (productById.getErrno() != 0) {
                return new ReturnObject(ReturnNo.getByCode(productById.getErrno()));
            }
            InternalReturnObject<OnSaleVo> onsaleById = goodsService.getOnsaleById(simpleOrderItemVo.getOnsaleId());
            if (onsaleById.getData() == null) {
                return new ReturnObject(ReturnNo.RESOURCE_ID_NOTEXIST);
            }
            //团购通过onsale去算 每一件就的价钱是onsale的价钱
            Long price = onsaleById.getData().getPrice();

        } else if (simpleOrderVo.getAdvancesaleId() != null) {
            InternalReturnObject<AdvanceVo> advanceSaleById = activityService.getAdvanceSaleById(simpleOrderVo.getAdvancesaleId());
            if (advanceSaleById.getErrno() != 0) {
                return new ReturnObject(ReturnNo.getByCode(advanceSaleById.getErrno()));
            }
            SimpleOrderItemVo simpleOrderItemVo = simpleOrderVo.getOrderItems().get(0);
            InternalReturnObject<ProductVo> productById = goodsService.getProductById(simpleOrderItemVo.getProductId());
            if (productById.getErrno() != 0) {
                return new ReturnObject(ReturnNo.getByCode(productById.getErrno()));
            }
            InternalReturnObject<OnSaleVo> onsaleById = goodsService.getOnsaleById(simpleOrderItemVo.getOnsaleId());
            if (onsaleById.getErrno() != 0) {
                return new ReturnObject(ReturnNo.getByCode(onsaleById.getErrno()));
            }
            //根据预售去算钱

        } else {
            //都没有 传到3-1计算钱
        }
        return new ReturnObject();
    }

    /**
     * 买家逻辑删除订单
     * created by  xiuchen lang
     * @param id
     * @param userId
     * @param userName
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public ReturnObject deleteOrderByCustomer(Long id, Long userId, String userName) {
        ReturnObject returnObject = orderDao.getOrderById(id);
        if (returnObject.getCode() != ReturnNo.OK) {
            return returnObject;
        }
        Order data = (Order) returnObject.getData();
        if (!Objects.equals(data.getCustomerId(), userId)) {
            return new ReturnObject(ReturnNo.RESOURCE_ID_OUTSCOPE);
        }
        if (!(data.getState() == OrderState.COMPLETE_ORDER.getCode() || data.getState() == OrderState.CANCEL_ORDER.getCode())) {
            return new ReturnObject(ReturnNo.STATENOTALLOW);
        }
        Order order = new Order();
        order.setId(id);
        order.setBeDeleted((byte) 1);
        Common.setPoModifiedFields(order, userId, userName);
        return orderDao.updateOrder(order);
    }

    /**
     * 买家取消订单
     * create by xiuchen Lang
     * @param id
     * @param userId
     * @param userName
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public ReturnObject cancelOrderByCustomer(Long id, Long userId, String userName) {
        ReturnObject returnObject = orderDao.getOrderById(id);
        if (returnObject.getCode() != ReturnNo.OK) {
            return returnObject;
        }
        Order data = (Order) returnObject.getData();
        if (!Objects.equals(data.getCustomerId(), userId)) {
            return new ReturnObject(ReturnNo.RESOURCE_ID_OUTSCOPE);
        }
        if (data.getState() == OrderState.COMPLETE_ORDER.getCode() || data.getState() == OrderState.CANCEL_ORDER.getCode()) {
            return new ReturnObject(ReturnNo.STATENOTALLOW);
        }
        Order order = new Order();
        order.setId(id);
        order.setState(OrderState.CANCEL_ORDER.getCode());
        Common.setPoModifiedFields(order, userId, userName);
        return orderDao.updateOrder(order);
    }


    /**
     * 买家取消订单
     * create by hty
     */
    @Transactional(rollbackFor = Exception.class)
    public ReturnObject confirmOrder(Long orderId,Long loginUserId,String loginUserName) {
        ReturnObject ret=orderDao.getOrderById(orderId);
        if(ret.getData()==null) {
            return ret;
        }
        Order order=(Order) ret.getData();
        if(!order.getState().equals(OrderState.SEND_GOODS.getCode()))
        {
            return new ReturnObject(ReturnNo.STATENOTALLOW);
        }
        order.setState(OrderState.COMPLETE_ORDER.getCode());
        Common.setPoModifiedFields(order,loginUserId,loginUserName);
        return orderDao.updateOrder(order);
    }

    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public ReturnObject listBriefOrdersByShopId(Long shopId,Long customerId,String orderSn,LocalDateTime beginTime,LocalDateTime endTime, Integer pageNumber, Integer pageSize) {
        return orderDao.listBriefOrdersByShopId(shopId,customerId,orderSn,beginTime,endTime, pageNumber, pageSize);
    }

    @Transactional(rollbackFor = Exception.class)
    public ReturnObject updateOrderComment(Long shopId, Long orderId, OrderVo orderVo, Long loginUserId, String loginUserName) {
        ReturnObject ret=orderDao.getOrderById(orderId);
        if(!ret.getCode().equals(ReturnNo.OK))
        {
            return ret;
        }
        Order order = (Order) ret.getData();
        if(!order.getShopId().equals(shopId))
        {
            return new ReturnObject(ReturnNo.RESOURCE_ID_OUTSCOPE);
        }
        order.setMessage(orderVo.getMessage());
        Common.setPoModifiedFields(order, loginUserId, loginUserName);
        return orderDao.updateOrderComment(order);
    }

    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public ReturnObject getOrderDetail(Long shopId, Long orderId) {
        ReturnObject ret = orderDao.getOrderById(orderId);
        if (!ret.getCode().equals(ReturnNo.OK)) {
            return ret;
        }
        Order order = (Order) ret.getData();
        if (!order.getShopId().equals(shopId)) {
            return new ReturnObject(ReturnNo.RESOURCE_ID_OUTSCOPE);
        }
        SimpleVo customerVo = customService.getCustomerById(order.getCustomerId()).getData();
        SimpleVo shopVo = shopService.getShopById(order.getShopId()).getData();
        DetailOrderVo orderVo = Common.cloneVo(order, DetailOrderVo.class);
        orderVo.setCustomerVo(customerVo);
        orderVo.setShopVo(shopVo);
        List<OrderItem> orderItemList = (List<OrderItem>) orderDao.listOrderItemsByOrderId(orderId).getData();
        List<SimpleOrderItemVo> simpleOrderItemVos = new ArrayList<>();
        for (OrderItem orderItem : orderItemList) {
            SimpleOrderItemVo simpleOrderItemVo = Common.cloneVo(orderItem, SimpleOrderItemVo.class);
            simpleOrderItemVos.add(simpleOrderItemVo);
        }
        orderVo.setOrderItems(simpleOrderItemVos);
        return new ReturnObject(orderVo);
    }

    @Transactional(rollbackFor = Exception.class)
    public ReturnObject confirmGrouponOrder(Long shopId, Long grouponActivityId, Long loginUserId, String loginUserName) {
        ReturnObject<Order> retOrder = orderDao.getOrderById(grouponActivityId);
        // 判断订单存在与否
        if (retOrder.getCode().equals(ReturnNo.OK)) {
            return retOrder;
        }
        // 判断订单是否为团购订单
        Order newOrder = retOrder.getData();
        if (newOrder.getGrouponId() == null) {
            return new ReturnObject<>(ReturnNo.RESOURCE_ID_OUTSCOPE);
        }

        // 设置订单状态为付款成功
        newOrder.setState(OrderState.FINISH_PAY.getCode());
        Common.setPoModifiedFields(newOrder, loginUserId, loginUserName);
        ReturnObject returnObject = orderDao.updateOrder(newOrder);

//        TODO: 需根据团购规则退款
//        if (!returnObject.getCode().equals(ReturnNo.OK)) {
//            return returnObject;
//        }

        return returnObject;
    }
}
