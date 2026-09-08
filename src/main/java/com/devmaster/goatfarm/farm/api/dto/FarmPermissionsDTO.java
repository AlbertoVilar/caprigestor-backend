package com.devmaster.goatfarm.farm.api.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FarmPermissionsDTO {
    private boolean canOperateFarm;
    private boolean canAdministerFarm;
}
