/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.viewelements.components.base;

import javax.inject.Inject;

import dagger.MembersInjector;
import de.vogler_engineering.smartdevicesapp.model.entities.ui.ComponentType;
import de.vogler_engineering.smartdevicesapp.model.entities.ui.UiComponent;
import de.vogler_engineering.smartdevicesapp.model.entities.ui.UiLayout;
import de.vogler_engineering.smartdevicesapp.viewelements.components.BarcodeInputComponent;
import de.vogler_engineering.smartdevicesapp.viewelements.components.ButtonComponent;
import de.vogler_engineering.smartdevicesapp.viewelements.components.ButtonComponentData;
import de.vogler_engineering.smartdevicesapp.viewelements.components.DateViewComponent;
import de.vogler_engineering.smartdevicesapp.viewelements.components.DateViewComponentData;
import de.vogler_engineering.smartdevicesapp.viewelements.components.DoubleButtonComponent;
import de.vogler_engineering.smartdevicesapp.viewelements.components.DoubleButtonComponentData;
import de.vogler_engineering.smartdevicesapp.viewelements.components.ExternalUrlComponent;
import de.vogler_engineering.smartdevicesapp.viewelements.components.ExternalUrlComponentData;
import de.vogler_engineering.smartdevicesapp.viewelements.components.GenericActionComponent;
import de.vogler_engineering.smartdevicesapp.viewelements.components.GenericActionComponentData;
import de.vogler_engineering.smartdevicesapp.viewelements.components.GraphDisplayComponent;
import de.vogler_engineering.smartdevicesapp.viewelements.components.GraphDisplayComponentData;
import de.vogler_engineering.smartdevicesapp.viewelements.components.JobListEntryComponent;
import de.vogler_engineering.smartdevicesapp.viewelements.components.JobListEntryComponentData;
import de.vogler_engineering.smartdevicesapp.viewelements.components.NumberInputComponent;
import de.vogler_engineering.smartdevicesapp.viewelements.components.NumberInputComponentData;
import de.vogler_engineering.smartdevicesapp.viewelements.components.OptionDialogComponent;
import de.vogler_engineering.smartdevicesapp.viewelements.components.OptionDialogComponentData;
import de.vogler_engineering.smartdevicesapp.viewelements.components.SpacerComponent;
import de.vogler_engineering.smartdevicesapp.viewelements.components.SpinnerComponent;
import de.vogler_engineering.smartdevicesapp.viewelements.components.SpinnerComponentData;
import de.vogler_engineering.smartdevicesapp.viewelements.components.SwitchComponent;
import de.vogler_engineering.smartdevicesapp.viewelements.components.SwitchComponentData;
import de.vogler_engineering.smartdevicesapp.viewelements.components.TextInputComponent;
import de.vogler_engineering.smartdevicesapp.viewelements.components.TextInputComponentData;
import de.vogler_engineering.smartdevicesapp.viewelements.components.TextViewComponent;
import de.vogler_engineering.smartdevicesapp.viewelements.components.TextViewComponentData;
import de.vogler_engineering.smartdevicesapp.viewelements.components.ToggleableEntryComponent;
import de.vogler_engineering.smartdevicesapp.viewelements.components.ToggleableEntryComponentData;
import de.vogler_engineering.smartdevicesapp.viewelements.components.ValueDisplayPlainComponent;
import de.vogler_engineering.smartdevicesapp.viewelements.components.ValueDisplayPlainComponentData;
import de.vogler_engineering.smartdevicesapp.viewelements.components.ValueMonitorComponent;
import de.vogler_engineering.smartdevicesapp.viewelements.components.ValueMonitorComponentData;
import de.vogler_engineering.smartdevicesapp.viewelements.components.ValueMonitorComponentSingle;
import de.vogler_engineering.smartdevicesapp.viewelements.components.VoidComponentData;
import de.vogler_engineering.smartdevicesapp.viewelements.components.picturecomponent.PictureComponentData;
import de.vogler_engineering.smartdevicesapp.viewelements.components.picturecomponent.PictureInputComponent;
import de.vogler_engineering.smartdevicesapp.viewelements.di.Injectable;
import de.vogler_engineering.smartdevicesapp.viewelements.ui.todolist.TodoListView;

public final class ComponentFactory {

    private final MembersInjector<ComponentData<Object>> componentDataInjector;
    private final MembersInjector<BaseComponent<Object>> componentInjector;
    private final MembersInjector<PictureInputComponent> pictureInputComponentInjector;
    private final MembersInjector<PictureComponentData> pictureComponentDataInjector;
    private final MembersInjector<ValueMonitorComponent> valueMonitorComponentInjector;
    private final MembersInjector<ValueMonitorComponentData> valueMonitorComponentDataInjector;
    private final MembersInjector<GraphDisplayComponentData> graphDisplayComponentDataInjector;
    private final MembersInjector<TodoListView> todoListViewInjector;

    @Inject
    public ComponentFactory(MembersInjector<ComponentData<Object>> componentDataInjector,
                            MembersInjector<BaseComponent<Object>> componentInjector,
                            MembersInjector<PictureInputComponent> pictureInputComponentInjector,
                            MembersInjector<PictureComponentData> pictureComponentDataInjector,
                            MembersInjector<ValueMonitorComponent> valueMonitorComponentInjector,
                            MembersInjector<ValueMonitorComponentData> valueMonitorComponentDataInjector,
                            MembersInjector<GraphDisplayComponentData> graphDisplayComponentDataInjector,
                            MembersInjector<TodoListView> todoListViewInjector){
        this.componentDataInjector = componentDataInjector;
        this.componentInjector = componentInjector;
        this.pictureInputComponentInjector = pictureInputComponentInjector;
        this.pictureComponentDataInjector = pictureComponentDataInjector;
        this.valueMonitorComponentInjector = valueMonitorComponentInjector;
        this.valueMonitorComponentDataInjector = valueMonitorComponentDataInjector;
        this.graphDisplayComponentDataInjector = graphDisplayComponentDataInjector;
        this.todoListViewInjector = todoListViewInjector;
    }

    public ComponentData getComponentData(UiComponent element, ConfigurableViewModelFeatures features) {
        ComponentData cd = createComponentData(element, features);

        if(cd instanceof Injectable) {
            if(cd instanceof PictureComponentData){
                pictureComponentDataInjector.injectMembers((PictureComponentData)cd);
            }else if(cd instanceof ValueMonitorComponentData) {
                valueMonitorComponentDataInjector.injectMembers((ValueMonitorComponentData) cd);
            }else if(cd instanceof GraphDisplayComponentData) {
                graphDisplayComponentDataInjector.injectMembers((GraphDisplayComponentData) cd);
            }else{
                //noinspection unchecked
                componentDataInjector.injectMembers((ComponentData<Object>) cd);
            }
        }
        return cd;
    }

    public BaseComponent getComponent(ComponentType type) {
        BaseComponent c = createComponent(type);

        if(c instanceof Injectable) {
            if(c instanceof PictureInputComponent){
                pictureInputComponentInjector.injectMembers((PictureInputComponent)c);
            }else if(c instanceof ValueMonitorComponent) {
                valueMonitorComponentInjector.injectMembers((ValueMonitorComponent) c);
            }else{
                //noinspection unchecked
                componentInjector.injectMembers((BaseComponent<Object>)c);
            }
        }
        return c;
    }

    private ComponentData createComponentData(UiComponent element, ConfigurableViewModelFeatures features) {
        switch (element.getType()) {
            case TextInput:
            case BarcodeInput:
                return new TextInputComponentData(element, features);
            case Button:
                return new ButtonComponentData(element, features);
            case DoubleButton:
                return new DoubleButtonComponentData(element, features);
            case Spinner:
                return new SpinnerComponentData(element, features);
            case Switch:
                return new SwitchComponentData(element, features);
            case NumberInput:
                return new NumberInputComponentData(element, features);
            case OptionDialog:
                return new OptionDialogComponentData(element, features);
            case TextView:
            case Unknown:
                return new TextViewComponentData(element, features);
            case ValueDisplayPlain:
                return new ValueDisplayPlainComponentData(element, features);
            case ValueDisplayGauge:
                return new ValueDisplayPlainComponentData(element, features);
            case ValueDisplayAdvanced:
                return new ValueDisplayPlainComponentData(element, features);
            case ValueMonitor:
            case ValueMonitorSingle:
                return new ValueMonitorComponentData(element, features);
            case GraphDisplay:
                return new GraphDisplayComponentData(element, features);
            case GenericAction:
                return new GenericActionComponentData(element, features);
            case JobListEntry:
                return new JobListEntryComponentData(element, features);
            case Spacer:
                return new VoidComponentData(element, features);
            case DateInput:
            case DateView:
                return new DateViewComponentData(element, features);
            case PictureInput:
            case PictureView:
                return new PictureComponentData(element, features);
            case ExternalUrl:
                return new ExternalUrlComponentData(element, features);
            case ToggleableEntry:
                return new ToggleableEntryComponentData(element, features);
            default:
                throw new IllegalArgumentException("Can't create ComponentData: Unknown component type: " + element.getType());
        }
    }

    private BaseComponent createComponent(ComponentType type) {
        switch (type) {
            case TextInput:
                return new TextInputComponent();
            case Button:
                return new ButtonComponent();
            case DoubleButton:
                return new DoubleButtonComponent();
            case Spinner:
                return new SpinnerComponent();
            case Switch:
                return new SwitchComponent();
            case NumberInput:
                return new NumberInputComponent();
            case OptionDialog:
                return new OptionDialogComponent();
            case TextView:
            case Unknown:
                return new TextViewComponent();
            case ValueDisplayPlain:
                return new ValueDisplayPlainComponent();
            case ValueDisplayGauge:
                return new ValueDisplayPlainComponent();
            case ValueDisplayAdvanced:
                return new ValueDisplayPlainComponent();
            case ValueMonitor:
                return new ValueMonitorComponent();
            case ValueMonitorSingle:
                return new ValueMonitorComponentSingle();
            case GraphDisplay:
                return new GraphDisplayComponent();
            case GenericAction:
                return new GenericActionComponent();
            case JobListEntry:
                return new JobListEntryComponent();
            case Spacer:
                return new SpacerComponent();
            case DateInput:
            case DateView:
                return new DateViewComponent();
            case PictureInput:
            case PictureView:
                return new PictureInputComponent();
            case ExternalUrl:
                return new ExternalUrlComponent();
            case ToggleableEntry:
                return new ToggleableEntryComponent();
            case BarcodeInput:
                return new BarcodeInputComponent();
            default:
                throw new IllegalArgumentException("Can't create Component: Unknown component type: " + type);
        }
    }

    public TodoListView createTodoListView(UiLayout layout){
        TodoListView view = new TodoListView(layout);
        todoListViewInjector.injectMembers(view);
        return view;
    }
}
