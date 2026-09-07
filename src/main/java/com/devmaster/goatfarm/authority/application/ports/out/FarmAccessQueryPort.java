package com.devmaster.goatfarm.authority.application.ports.out;

public interface FarmAccessQueryPort {

    boolean existsOperatorLink(Long farmId, Long userId);
}
