package cn.edu.xmu.oomall.ooad201.order.microService;

import cn.edu.xmu.oomall.core.util.ReturnObject;
import cn.edu.xmu.oomall.ooad201.order.microService.vo.AdvanceVo;
import cn.edu.xmu.oomall.ooad201.order.microService.vo.GrouponActivityVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author xiuchen lang 22920192204222
 * @date 2021/12/07 16:19
 */
@FeignClient(name = "activity-service")
public interface ActivityService {
    /**
     * advancesales/{id}
     * 查询上线预售活动的详细信息（2021-1-10）
     *
     * @param id 预售活动id
     * @return 返回体cn.edu.xmu.oomall.ooad201.order.microService.vo.AdvanceVo
     */
    @GetMapping("/advancesales/{id}")
    ReturnObject<AdvanceVo> getAdvanceSaleById(@PathVariable Long id);

    /**
     * /groupons/{id}
     * 查上线态团购活动详情(2021-1-12)
     *
     * @param id 团购活动id
     * @return cn.edu.xmu.oomall.ooad201.order.microService.vo.GrouponActivityVo
     */
    @GetMapping("/groupons/{id}")
    ReturnObject<GrouponActivityVo> getGrouponsById(@PathVariable Long id);
}
