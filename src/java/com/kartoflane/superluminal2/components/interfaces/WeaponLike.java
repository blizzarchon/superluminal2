package com.kartoflane.superluminal2.components.interfaces;

import com.kartoflane.superluminal2.ftl.AnimationObject;

public interface WeaponLike {
    String getBlueprintName();

    AnimationObject getAnimation();

    String buttonView();
}
