package com.huawei.radiolinedemo;

import com.huawei.radiolinedemo.slice.MainAbilitySlice;
import ohos.aafwk.ability.Ability;
import ohos.aafwk.content.Intent;

public class MainAbility extends Ability {
    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        setSwipeToDismiss(true);
        super.setMainRoute(MainAbilitySlice.class.getName());
    }
}
