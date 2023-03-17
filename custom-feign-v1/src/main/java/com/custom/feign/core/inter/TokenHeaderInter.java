package com.custom.feign.core.inter;

import java.util.Map;

/**
 * Description:
 *
 * @author damon.liu
 * Date 2023-03-14 7:56
 */
public interface TokenHeaderInter {
    /**
     * 令牌
     * @return
     */
    Map<String,String> tokenHeader();

}
