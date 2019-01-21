/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.viewelements.components.base;

import androidx.lifecycle.LifecycleOwner;
import android.content.Context;
import android.content.Intent;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.lang.ref.WeakReference;

import de.vogler_engineering.smartdevicesapp.common.util.StringUtil;
import de.vogler_engineering.smartdevicesapp.model.entities.ui.ComponentType;
import de.vogler_engineering.smartdevicesapp.model.entities.ui.UiComponent;

/**
 * Created by vh on 22.03.2018.
 */

public abstract class BaseComponent<T> {

    public final ComponentType type;
    protected UiComponent element;
    protected WeakReference<LifecycleOwner> lifecycleOwner;

    private ComponentData componentData;

    private int position;

    public BaseComponent(ComponentType type) {
        this.type = type;
    }

    public void setElement(UiComponent element) {
        this.element = element;
    }

    protected BaseComponent(ComponentType type, UiComponent element) {
        this.type = type;
        this.element = element;
        this.lifecycleOwner = null;
    }

    protected BaseComponent(ComponentType type, UiComponent element, LifecycleOwner owner){
        this(type, element);
        setLifecycleOwner(owner);
    }

    public void setLifecycleOwner(LifecycleOwner lifecycleOwner) {
        this.lifecycleOwner = new WeakReference<>(lifecycleOwner);
    }

    public abstract View createView(Context context, LayoutInflater inflater);

    public abstract void bindView(Context context, ComponentData<T> componentData, LifecycleOwner lifecycleOwner);

    public abstract void dispose();

    public abstract View getView();

    protected void updateData(T s) {
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public LinearLayout.LayoutParams getDefaultLayoutParams(){
        return new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    public int getFromDp(DisplayMetrics metrics, float dp){

        return (int) (dp * metrics.density + 0.5f);
    }

    public int getFromDp(DisplayMetrics metrics, int dp){
        return (int) (dp * metrics.density + 0.5f);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data){
    }



    public void bindData(ComponentData data){
        componentData = data;
    }

    public void unbindData(LifecycleOwner lifecycleOwner){
        if(componentData != null) {
            componentData.valueLiveData().removeObservers(lifecycleOwner);
        }
    }

    public Object getFromAdditionalProperties(String key) {
        //Ignore fist key chase
        if(element == null ||
                element.getAdditionalProperties() == null) {
            return null;
        }
        String s = StringUtil.firstToLowerCase(key);
        if(element.getAdditionalProperties().containsKey(s)){
            return element.getAdditionalProperties().get(s);
        }
        s = StringUtil.firstToUpperCase(key);
        if(element.getAdditionalProperties().containsKey(s)){
            return element.getAdditionalProperties().get(s);
        }
        return null;
    }

    public <O> O getFromAdditionalProperties(String key, Class<O> clazz){
        Object o = getFromAdditionalProperties(key);
        if(o == null) return null;
        if(clazz.isInstance(o))
            //noinspection unchecked
            return (O)o;
        return null;
    }
}
