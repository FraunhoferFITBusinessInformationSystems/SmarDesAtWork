/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.app.phone.ui.main;

import android.graphics.drawable.Drawable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.navigation.NavigationView;

import java.util.Collection;
import java.util.Iterator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.ObservableList;
import androidx.drawerlayout.widget.DrawerLayout;
import de.vogler_engineering.smartdevicesapp.app.phone.R;
import de.vogler_engineering.smartdevicesapp.app.phone.ui.NavigationController;
import de.vogler_engineering.smartdevicesapp.model.entities.dto.AppInfo;
import de.vogler_engineering.smartdevicesapp.model.entities.dto.DeviceConfig;
import de.vogler_engineering.smartdevicesapp.model.entities.tabs.TabConfig;
import de.vogler_engineering.smartdevicesapp.model.management.AppManagerImpl;
import de.vogler_engineering.smartdevicesapp.model.management.OnlineState;
import de.vogler_engineering.smartdevicesapp.model.repository.ConfigRepository;
import de.vogler_engineering.smartdevicesapp.model.repository.ResourceRepository;
import de.vogler_engineering.smartdevicesapp.model.repository.TabRepository;
import de.vogler_engineering.smartdevicesapp.viewelements.ui.AbstractActivity;
import de.vogler_engineering.smartdevicesapp.viewelements.util.DeviceInfoUtils;
import de.vogler_engineering.smartdevicesapp.viewelements.util.ResourceUtils;
import timber.log.Timber;

/**
 * Class to manage the Drawer-View on an Activity. Dynamically generates Drawer-Entries,
 * configures the header and adds additional Drawer entries.
 */
public class DrawerManager implements DrawerLayout.DrawerListener,
        NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "DrawerManager";

    private final AbstractActivity activity;
    private ResourceRepository resourceRepository;
    private TabRepository tabConfigRepository;
    private AppManagerImpl appManager;
    private NavigationController navigationController;
    private NavigationView mNavigationView;

    private int groupId = View.generateViewId();
    private int[] menuIds = null;

    public DrawerManager(AbstractActivity activity,
                         ConfigRepository configRepository,
                         ResourceRepository resourceRepository, AppManagerImpl appManager,
                         TabRepository tabConfigRepository,
                         NavigationController navigationController,
                         NavigationView navigationView) {
        this.activity = activity;
        ConfigRepository configRepository1 = configRepository;
        this.resourceRepository = resourceRepository;
        this.tabConfigRepository = tabConfigRepository;
        this.appManager = appManager;
        this.navigationController = navigationController;
        this.mNavigationView = navigationView;

        this.rebuildDrawerMenuEntries(tabConfigRepository.getTabConfig());
        tabConfigRepository.getTabConfig().addOnListChangedCallback(new OnTabConfigChangeCallback());
        configRepository.getDeviceConfigObservable().observe(activity, this::onDevicesConfigChanged);
    }

    private AppInfo appInfo = null;

    private void onDevicesConfigChanged(DeviceConfig deviceConfig) {
        if(appInfo == null || !appInfo.equals(deviceConfig.getInfo())){
            //App Info has changed!
            appInfo = deviceConfig.getInfo();
            updateDrawerTitle();
        }
    }

    private void rebuildDrawerMenuEntries(Collection<TabConfig> tabConfig) {
        Menu menu = mNavigationView.getMenu();

        menu.removeGroup(groupId);

        if(tabConfig.size() > 0) {
            menuIds = new int[tabConfig.size()];

            Iterator<TabConfig> iterator = tabConfig.iterator();
            for (int i = 0; iterator.hasNext(); i++) {
                TabConfig config = iterator.next();
                int id = View.generateViewId();
                MenuItem item = menu.add(groupId, id, Menu.NONE, config.getTitle());

                String icon = config.getIcon();
                if (icon != null) {
                    int iconRes = ResourceUtils.getIconResourceByKey(icon);
                    if (iconRes != ResourceUtils.INVALID_RESOURCE)
                        item.setIcon(iconRes);
                }

                menuIds[i] = id;
            }
        }
    }

    private void onDrawerMenuEntriesUpdated(){
        rebuildDrawerMenuEntries(tabConfigRepository.getTabConfig());
    }

    public void initDrawer(DrawerLayout drawerLayout, Toolbar toolbar) {
        //Drawer Toggle
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                activity, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        drawerLayout.addDrawerListener(this);

        //Drawer Layout change
        removeDrawerHeader();
        mNavigationView.inflateHeaderView(R.layout.drawer_header_main);
    }

    private void removeDrawerHeader() {
        while (mNavigationView.getHeaderCount() > 0) {
            View headerView = mNavigationView.getHeaderView(0);
            mNavigationView.removeHeaderView(headerView);
        }
    }

    public void updateDrawer() {
        //setOnlineState(appManager.getOnlineState());
        updateDrawerHeaderInfo();

        activity.invalidateOptionsMenu();
        activity.supportInvalidateOptionsMenu();
    }

    private String versionNameOld = null;
    private OnlineState onlineStateOld = null;
    private String deviceIdOld = null;
    private String imageOld = null;

    private void updateDrawerTitle(){
        View headerView = mNavigationView.getHeaderView(0);
        if (headerView != null && appInfo != null) {
            TextView title = headerView.findViewById(R.id.nav_header_title);
            title.setText(appInfo.getTitle());

            TextView subtitle = headerView.findViewById(R.id.nav_header_subtitle);
            subtitle.setText(appInfo.getSubtitle());

            if (imageOld == null || !imageOld.equals(appInfo.getTitleResource())) {
                ImageView image = headerView.findViewById(R.id.nav_header_image);
                try {
                    resourceRepository.loadImage(appInfo.getTitleResource())
                            .resize(0, 90)
                            .centerCrop()
                            .placeholder(R.drawable.ic_photo_placeholder)
                            .error(R.drawable.ic_launcher_smartdes)
                            .into(image);
                }catch (ResourceRepository.ImageLoadingException e){
                    Timber.tag(TAG).e(e,"Could not load DrawerImage");
                }
            }
        }
    }

    private void updateDrawerHeaderInfo() {
        View headerView = mNavigationView.getHeaderView(0);
        if (headerView != null) {

            String deviceId = appManager.getDeviceIdKey();
            if (deviceIdOld == null || !deviceIdOld.equals(deviceId)) {
                TextView deviceIdText = headerView.findViewById(R.id.nav_header_device_id);
                deviceIdText.setText(String.format("%s", deviceId));
            }

            String versionName = DeviceInfoUtils.getAppVersion(this.activity, activity.getPackageName());
            if (versionNameOld == null || !versionNameOld.equals(versionName)) {
                TextView versionText = headerView.findViewById(R.id.nav_header_version);
                versionText.setText(String.format("v: %s", versionName));
                versionNameOld = versionName;
            }

            OnlineState onlineState = appManager.getOnlineState();
            if (onlineStateOld == null || onlineState != onlineStateOld) {
                TextView onlineStateText = headerView.findViewById(R.id.nav_header_online_state);
                ImageView onlineStateImage = headerView.findViewById(R.id.nav_header_online_state_icon);

                OnlineStateResources oRes = OnlineStateResources.getResource(onlineState);
                Drawable drawable = activity.getResources().getDrawable(oRes.drawableRes, null);
                onlineStateImage.setImageDrawable(drawable);
                onlineStateText.setText(oRes.stringRes);
                onlineStateOld = onlineState;
            }

        }
    }

    @Override
    public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
    }

    @Override
    public void onDrawerOpened(@NonNull View drawerView) {
        //setOnlineState(appManager.getOnlineState());
        updateDrawer();
    }

    @Override
    public void onDrawerClosed(@NonNull View drawerView) {
    }

    @Override
    public void onDrawerStateChanged(int newState) {
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if(itemId == R.id.nav_item_settings){
            navigationController.navigateToSettings(activity);
            return true;
//        }else if(itemId == R.id.nav_item_about){
//            navigationController.navigateToAbout(activity);
//            return true;
        }else {
            for (int i = 0; i < menuIds.length; i++) {
                int entryId = menuIds[i];
                if (itemId == entryId) {
                    try {
                        TabConfig config = tabConfigRepository.getTabConfig().get(i);
                        Timber.tag(TAG).d("DrawerNavigation to tab: %s", config.getKey());
                        navigationController.navigateToMainTab(activity, config.getKey());
                        return true;
                    } catch (Exception e) {
                        Timber.tag(TAG).e(e, "Could not navigate to tab!");
                        return true;
                    }
                }
            }
            return false;
        }
    }

    public enum OnlineStateResources {
        ONLINE(OnlineState.ONLINE, R.string.enum_online_state_online, R.color.online_state_online, R.drawable.ic_point_online),
        CONNECTING(OnlineState.PENDING, R.string.enum_online_state_pending, R.color.online_state_pending, R.drawable.ic_point_pending),
        OFFLINE(OnlineState.OFFLINE, R.string.enum_online_state_offline, R.color.online_state_offline, R.drawable.ic_point_offline);

        private final OnlineState online;
        public final int stringRes;
        public final int colorRes;
        public final int drawableRes;

        OnlineStateResources(OnlineState online, int stringRes, int colorRes, int drawableRes) {
            this.online = online;
            this.stringRes = stringRes;
            this.colorRes = colorRes;
            this.drawableRes = drawableRes;
        }

        public static OnlineStateResources getResource(OnlineState state) {
            for (OnlineStateResources resources : values()) {
                if (resources.online == state)
                    return resources;
            }
            throw new IllegalArgumentException("Unknown enum entry!");
        }
    }

    public class OnTabConfigChangeCallback extends ObservableList.OnListChangedCallback<ObservableList<TabConfig>> {
        @Override
        public void onChanged(ObservableList<TabConfig> sender) { }

        @Override
        public void onItemRangeChanged(ObservableList<TabConfig> sender, int positionStart, int itemCount) {
            onDrawerMenuEntriesUpdated();
        }

        @Override
        public void onItemRangeInserted(ObservableList<TabConfig> sender, int positionStart, int itemCount) {
            onDrawerMenuEntriesUpdated();
        }

        @Override
        public void onItemRangeMoved(ObservableList<TabConfig> sender, int fromPosition, int toPosition, int itemCount) {
            onDrawerMenuEntriesUpdated();
        }

        @Override
        public void onItemRangeRemoved(ObservableList<TabConfig> sender, int positionStart, int itemCount) {
            onDrawerMenuEntriesUpdated();
        }
    }
}


