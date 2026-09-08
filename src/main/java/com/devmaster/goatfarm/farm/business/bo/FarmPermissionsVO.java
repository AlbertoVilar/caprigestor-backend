package com.devmaster.goatfarm.farm.business.bo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FarmPermissionsVO {
    /** Whether the authenticated principal may execute operational farm work. */
    private boolean canOperateFarm;

    /** Whether the authenticated principal may administer the farm itself. */
    private boolean canAdministerFarm;
}
