package cn.edu.xmu.oomall.order.dao;

import cn.edu.xmu.oomall.core.util.Common;
import cn.edu.xmu.oomall.core.util.ReturnNo;
import cn.edu.xmu.oomall.core.util.ReturnObject;
import cn.edu.xmu.oomall.order.mapper.OrderItemPoMapper;
import cn.edu.xmu.oomall.order.mapper.OrderPoMapper;
import cn.edu.xmu.oomall.order.model.bo.Order;
import cn.edu.xmu.oomall.order.model.bo.OrderItem;
import cn.edu.xmu.oomall.order.model.bo.OrderState;
import cn.edu.xmu.oomall.order.model.po.OrderItemPo;
import cn.edu.xmu.oomall.order.model.po.OrderItemPoExample;
import cn.edu.xmu.oomall.order.model.po.OrderPo;
import cn.edu.xmu.oomall.order.model.po.OrderPoExample;
import cn.edu.xmu.oomall.order.model.vo.BriefOrderVo;
import cn.edu.xmu.oomall.order.model.vo.OrderItemRetVo;
import cn.edu.xmu.privilegegateway.annotation.util.InternalReturnObject;
import cn.edu.xmu.privilegegateway.annotation.util.RedisUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static cn.edu.xmu.privilegegateway.annotation.util.Common.cloneVo;


@Repository
public class OrderDao {
    private static final Logger logger = LoggerFactory.getLogger(OrderDao.class);

    @Autowired
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    OrderPoMapper orderPoMapper;

    @Autowired
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    OrderItemPoMapper orderItemPoMapper;

    @Value("${oomall.order.expiretime}")
    private long orderExpireTime;

    @Autowired
    RedisUtil redisUtil;

    final static private String ORDER_KEY = "order_%d";

    public ReturnObject getNotDeleteOrderById(Long id) {
        try {
            String key = String.format(ORDER_KEY, id);
            Order order = (Order) redisUtil.get(key);
            if (order != null) {
                return new ReturnObject(order);
            }
            OrderPo po = orderPoMapper.selectByPrimaryKey(id);
            if (po == null || po.getBeDeleted() != null && po.getBeDeleted() == 1) {
                return new ReturnObject<>(ReturnNo.RESOURCE_ID_OUTSCOPE);
            }
            order = cloneVo(po, Order.class);
            redisUtil.set(key, order, orderExpireTime);
            return new ReturnObject<>(order);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return new ReturnObject<>(ReturnNo.INTERNAL_SERVER_ERR, e.getMessage());
        }
    }
    public ReturnObject getOrderById(Long id) {
        try {
            String key = String.format(ORDER_KEY, id);
            Order order = (Order) redisUtil.get(key);
            if (order != null) {
                return new ReturnObject(order);
            }
            OrderPo po = orderPoMapper.selectByPrimaryKey(id);
            order = cloneVo(po, Order.class);
            redisUtil.set(key, order, orderExpireTime);
            return new ReturnObject<>(order);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return new ReturnObject<>(ReturnNo.INTERNAL_SERVER_ERR, e.getMessage());
        }
    }
//传入子订单
    public ReturnObject updateRelatedSonOrder(Order order) {
        try {
            OrderPoExample orderPoExample = new OrderPoExample();
            OrderPoExample.Criteria cr = orderPoExample.createCriteria();
            cr.andPidEqualTo(order.getPid());
            OrderPo orderPo = cloneVo(order, OrderPo.class);
            orderPoMapper.updateByExampleSelective(orderPo, orderPoExample);
            //TODO:在考虑一下 怎么删除redis
            return new ReturnObject(ReturnNo.OK);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return new ReturnObject<>(ReturnNo.INTERNAL_SERVER_ERR, e.getMessage());
        }
    }

    public ReturnObject updateOrder(Order order) {
        try {
            OrderPo orderPo = cloneVo(order, OrderPo.class);
            int flag = orderPoMapper.updateByPrimaryKeySelective(orderPo);
            if (flag == 0) {
                return new ReturnObject<>(ReturnNo.RESOURCE_ID_NOTEXIST);
            } else {
                redisUtil.del(String.format(ORDER_KEY,order.getId()));
                return new ReturnObject<>(ReturnNo.OK);
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            return new ReturnObject<>(ReturnNo.INTERNAL_SERVER_ERR, e.getMessage());
        }
    }


    public ReturnObject listOrderItemsByOrderId(Long orderId) {
        try {
            OrderItemPoExample orderItemPoExample = new OrderItemPoExample();
            OrderItemPoExample.Criteria cr = orderItemPoExample.createCriteria();
            cr.andOrderIdEqualTo(orderId);
            List<OrderItemPo> orderItemPos = orderItemPoMapper.selectByExample(orderItemPoExample);
            if (orderItemPos.size() == 0) {
                return new ReturnObject(ReturnNo.RESOURCE_ID_NOTEXIST);
            }
            List<OrderItem> orderItemList = new ArrayList<>(orderItemPos.size());
            for (OrderItemPo orderItemPo : orderItemPos) {
                orderItemList.add(cloneVo(orderItemPo, OrderItem.class));
            }
            return new ReturnObject(orderItemList);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return new ReturnObject(ReturnNo.INTERNAL_SERVER_ERR, e.getMessage());
        }
    }

    public ReturnObject listBriefOrdersByShopId(Long shopId, Long customerId, String orderSn, LocalDateTime beginTime, LocalDateTime endTime, Integer pageNumber, Integer pageSize) {
        try {
            PageHelper.startPage(pageNumber, pageSize, true, true, true);
            OrderPoExample orderPoExample = new OrderPoExample();
            OrderPoExample.Criteria cr = orderPoExample.createCriteria();
            cr.andShopIdEqualTo(shopId);
            if (customerId != null) {
                cr.andCustomerIdEqualTo(customerId);
            }
            if (orderSn != null) {
                cr.andOrderSnEqualTo(orderSn);
            }
            if (beginTime != null) {
                cr.andGmtCreateGreaterThan(beginTime);
            }
            if (endTime != null) {
                cr.andGmtCreateLessThan(endTime);
            }
            List<OrderPo> orderPoList = orderPoMapper.selectByExample(orderPoExample);
            if (orderPoList.size() == 0) {
                return new ReturnObject(ReturnNo.RESOURCE_ID_NOTEXIST);
            }
            ReturnObject<PageInfo<Object>> ret = new ReturnObject(new PageInfo(orderPoList));
            return Common.getPageRetVo(ret, BriefOrderVo.class);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return new ReturnObject(ReturnNo.INTERNAL_SERVER_ERR, e.getMessage());
        }
    }

    public ReturnObject getOrderItemById(Long id) {
        try {
            OrderItemPo orderItemPo = orderItemPoMapper.selectByPrimaryKey(id);
            if (orderItemPo == null) {
                return new ReturnObject(ReturnNo.RESOURCE_ID_NOTEXIST);
            }
            OrderItem orderItem = cloneVo(orderItemPo, OrderItem.class);
            return new ReturnObject(orderItem);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return new ReturnObject(ReturnNo.INTERNAL_SERVER_ERR, e.getMessage());
        }

    }

    /**
     * a-1
     *
     * @author Fang Zheng
     */
    public ReturnObject listBriefOrderByUserId(Long userId,
                                               String orderSn,
                                               Integer state,
                                               LocalDateTime beginTime,
                                               LocalDateTime endTime,
                                               Integer pageNumber,
                                               Integer pageSize) {
        try {
            if (pageNumber != null && pageSize != null) {
                PageHelper.startPage(pageNumber, pageSize, true, true, true);
            }
            OrderPoExample orderPoExample = new OrderPoExample();
            OrderPoExample.Criteria cr = orderPoExample.createCriteria();
            cr.andCustomerIdEqualTo(userId);
            cr.andBeDeletedEqualTo((byte) 0);
            if (orderSn != null) {
                cr.andOrderSnEqualTo(orderSn);
            }
            if (state != null){
                cr.andStateEqualTo(state);
            }
            if (beginTime != null) {
                cr.andConfirmTimeGreaterThanOrEqualTo(beginTime);
            }
            if (endTime != null){
                cr.andConfirmTimeLessThanOrEqualTo(endTime);
            }
            orderPoExample.setOrderByClause("gmt_create DESC");
            List<OrderPo> orderPoList = orderPoMapper.selectByExample(orderPoExample);
            ReturnObject<PageInfo<Object>> ret = new ReturnObject(new PageInfo(orderPoList));
            return Common.getPageRetVo(ret, BriefOrderVo.class);
        }catch (Exception e){
            logger.error(e.getMessage());
            return new ReturnObject(ReturnNo.INTERNAL_SERVER_ERR, e.getMessage());
        }
    }
    public ReturnObject listOrderItemsByPOrderId(Long id){
        try{
            OrderPoExample example = new OrderPoExample();
            OrderPoExample.Criteria criteria = example.createCriteria();
            criteria.andPidEqualTo(id);
            List<OrderPo> list = orderPoMapper.selectByExample(example);
            List<OrderItemPo>orderItemPos=new ArrayList<>();
            //没有子订单
            if (list.size()==0) {
                OrderItemPoExample orderItemPoExample=new OrderItemPoExample();
                OrderItemPoExample.Criteria orderItemPoExampleCriteria=orderItemPoExample.createCriteria();
                orderItemPoExampleCriteria.andOrderIdEqualTo(id);
                orderItemPos=orderItemPoMapper.selectByExample(orderItemPoExample);
            }
            //有子订单
            else{
                for(OrderPo orderPo:list){
                    OrderItemPoExample orderItemPoExample=new OrderItemPoExample();
                    OrderItemPoExample.Criteria orderItemPoExampleCriteria=orderItemPoExample.createCriteria();
                    orderItemPoExampleCriteria.andOrderIdEqualTo(orderPo.getId());
                    orderItemPos.addAll(orderItemPoMapper.selectByExample(orderItemPoExample));
                }
            }
            List<OrderItemRetVo>list1=new ArrayList<>();
            for(OrderItemPo orderItemPo:orderItemPos){
                list1.add(cloneVo(orderItemPo,OrderItemRetVo.class));
            }
            return new ReturnObject(list1);}
        catch (Exception e){
            logger.error(e.getMessage());
            return new ReturnObject(ReturnNo.INTERNAL_SERVER_ERR, e.getMessage());
        }

    }

    public ReturnObject createOrder(Order order)
    {
        try
        {
            OrderPo orderPo=cloneVo(order,OrderPo.class);
            orderPoMapper.insert(orderPo);
            Order order1=cloneVo(orderPo,Order.class);
            return new ReturnObject(order1);
        }
        catch (Exception e){
            logger.error(e.getMessage());
            return new ReturnObject(ReturnNo.INTERNAL_SERVER_ERR,e.getMessage());
        }
    }

    public ReturnObject createOrderItem(OrderItem orderItem)
    {
        try{
            OrderItemPo orderItemPo=cloneVo(orderItem,OrderItemPo.class);
            orderItemPoMapper.insert(orderItemPo);
            return new ReturnObject(ReturnNo.OK);
        }catch (Exception e){
            logger.error(e.getMessage());
            return new ReturnObject(ReturnNo.INTERNAL_SERVER_ERR,e.getMessage());
        }
    }


    public ReturnObject getOrderByOrderSn(String orderSn){
        try {
            OrderPoExample orderPoExample = new OrderPoExample();
            OrderPoExample.Criteria criteria = orderPoExample.createCriteria();
            criteria.andOrderSnEqualTo(orderSn);
            List<OrderPo> orderPos = orderPoMapper.selectByExample(orderPoExample);
            if (orderPos.size()==0){
                return new ReturnObject(ReturnNo.RESOURCE_ID_NOTEXIST);
            }
            return new ReturnObject(orderPos.get(0));
        }catch (Exception e){
            logger.error(e.getMessage());
            return new ReturnObject(ReturnNo.INTERNAL_SERVER_ERR,e.getMessage());
        }
    }
    public ReturnObject getQuantityByGroupOnId(Long id){
        try {
            List<Integer>stateList=new ArrayList<>();
            stateList.add(OrderState.FINISH_PAY.getCode());
            stateList.add(OrderState.WAIT_GROUP.getCode());
            stateList.add(OrderState.SEND_GOODS.getCode());
            OrderPoExample orderPoExample = new OrderPoExample();
            OrderPoExample.Criteria criteria = orderPoExample.createCriteria();
            criteria.andGrouponIdEqualTo(id);
            criteria.andStateIn(stateList);
            List<OrderPo> orderPos = orderPoMapper.selectByExample(orderPoExample);
            if (orderPos.size()==0){
                return new ReturnObject(ReturnNo.RESOURCE_ID_NOTEXIST);
            }
            Long quantity=0L;
            for(OrderPo orderPo:orderPos){
                ReturnObject returnObject=listOrderItemsByPOrderId(orderPo.getId());
                List<OrderItemRetVo>list=(List<OrderItemRetVo>)returnObject.getData();
                for(OrderItemRetVo orderItemRetVo:list){
                    quantity+=orderItemRetVo.getQuantity();
                }
            }
            return new ReturnObject(quantity);

        }catch (Exception e){
            logger.error(e.getMessage());
            return new ReturnObject(ReturnNo.INTERNAL_SERVER_ERR,e.getMessage());
        }
    }
}
