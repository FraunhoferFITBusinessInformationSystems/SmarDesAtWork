/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.viewelements.components.base;

import androidx.lifecycle.LifecycleOwner;
import android.content.Context;
import androidx.databinding.ObservableArrayList;
import androidx.databinding.ObservableList;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Collection;

import de.vogler_engineering.smartdevicesapp.model.entities.ui.ComponentType;
import de.vogler_engineering.smartdevicesapp.model.entities.ui.UiComponent;

public class ConfigurableRecyclerAdapter<T extends UiComponent> extends RecyclerView.Adapter<ConfigurableRecyclerAdapter.ComponentViewHolder> {

    private final Context context;
    private final ComponentFactory componentFactory;
    private LifecycleOwner lifecycleOwner;
    private ObservableList<T> elements;
    private final ComponentDataProvider componentDataProvider;
    private final WeakReferenceOnListChangedCallback onListChangedCallback;

    public ConfigurableRecyclerAdapter(Context context, ComponentDataProvider componentDataProvider, ComponentFactory componentFactory, LifecycleOwner lifecycleOwner) {
        this.context = context;
        this.componentDataProvider = componentDataProvider;
        this.componentFactory = componentFactory;
        this.lifecycleOwner = lifecycleOwner;
        this.onListChangedCallback = new WeakReferenceOnListChangedCallback(this);
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public ComponentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ComponentType type = ComponentType.getTypeFromId(viewType);
        BaseComponent component = componentFactory.getComponent(type);

        View view = component.createView(parent.getContext(), LayoutInflater.from(parent.getContext()));
        return new ComponentViewHolder(component);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull ComponentViewHolder holder, int position) {
        T uiElements = elements.get(position);
        holder.component.setElement(uiElements);
        holder.component.setPosition(position);

        //Clear the component (-listeners), if it is rebound to new data
        holder.component.unbindData(lifecycleOwner);

        ComponentData data = componentDataProvider.getComponentData(position);

        //Set the data for observer-management
        holder.component.bindData(data);

        //Bind the data to the view
        //noinspection unchecked
        holder.component.bindView(context, data, lifecycleOwner);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return (elements == null) ? 0 : elements.size();
    }

    @Override
    public int getItemViewType(int position) {
        return elements.get(position).getType().getId();
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView)
    {
        if (elements != null)
        {
            elements.removeOnListChangedCallback(onListChangedCallback);
        }
    }

    public void setItems(@Nullable Collection<T> items)
    {
        if (this.elements == items)
        {
            return;
        }

        if (this.elements != null)
        {
            this.elements.removeOnListChangedCallback(onListChangedCallback);
            notifyItemRangeRemoved(0, this.elements.size());
        }

        if (items instanceof ObservableList)
        {
            this.elements = (ObservableList<T>) items;
            notifyItemRangeInserted(0, this.elements.size());
            this.elements.addOnListChangedCallback(onListChangedCallback);
        }
        else if (items != null)
        {
            this.elements = new ObservableArrayList<>();
            this.elements.addOnListChangedCallback(onListChangedCallback);
            this.elements.addAll(items);
        }
        else
        {
            this.elements = null;
        }
    }

    public void setItems(@Nullable ObservableList<T> items)
    {
        if (this.elements == items)
        {
            return;
        }

        if (this.elements != null)
        {
            this.elements.removeOnListChangedCallback(onListChangedCallback);
            notifyItemRangeRemoved(0, this.elements.size());
        }

        this.elements = items;
        notifyItemRangeInserted(0, this.elements.size());
        this.elements.addOnListChangedCallback(onListChangedCallback);
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ComponentViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public final BaseComponent component;

        ComponentViewHolder(BaseComponent c) {
            super(c.getView());
            component = c;
        }
    }

    private static class WeakReferenceOnListChangedCallback extends ObservableList.OnListChangedCallback
    {
        private final ConfigurableRecyclerAdapter adapterReference;

        public WeakReferenceOnListChangedCallback(ConfigurableRecyclerAdapter bindingRecyclerViewAdapter)
        {
            this.adapterReference = bindingRecyclerViewAdapter;
        }

        @Override
        public void onChanged(ObservableList sender)
        {
            RecyclerView.Adapter adapter = adapterReference;
            if (adapter != null)
            {
                adapter.notifyDataSetChanged();
            }
        }

        @Override
        public void onItemRangeChanged(ObservableList sender, int positionStart, int itemCount)
        {
            RecyclerView.Adapter adapter = adapterReference;
            if (adapter != null)
            {
                adapter.notifyItemRangeChanged(positionStart, itemCount);
            }
        }

        @Override
        public void onItemRangeInserted(ObservableList sender, int positionStart, int itemCount)
        {
            RecyclerView.Adapter adapter = adapterReference;
            if (adapter != null)
            {
                adapter.notifyItemRangeInserted(positionStart, itemCount);
            }
        }

        @Override
        public void onItemRangeMoved(ObservableList sender, int fromPosition, int toPosition, int itemCount)
        {
            RecyclerView.Adapter adapter = adapterReference;
            if (adapter != null)
            {
                adapter.notifyItemMoved(fromPosition, toPosition);
            }
        }

        @Override
        public void onItemRangeRemoved(ObservableList sender, int positionStart, int itemCount)
        {
            RecyclerView.Adapter adapter = adapterReference;
            if (adapter != null)
            {
                adapter.notifyItemRangeRemoved(positionStart, itemCount);
            }
        }
    }
}