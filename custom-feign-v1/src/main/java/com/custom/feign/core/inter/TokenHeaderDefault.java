package com.custom.feign.core.inter;

import java.util.HashMap;
import java.util.Map;

/**
 * Description:
 *
 * @author damon.liu
 * Date 2023-03-14 7:56
 */
public class TokenHeaderDefault implements TokenHeaderInter{
    /**
     * 令牌
     * @return
     */
    @Override
    public Map<String,String> tokenHeader(){
        return new HashMap<>();
    }
}
