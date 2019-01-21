/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.app.phone.ui.main;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.databinding.ObservableArrayList;
import androidx.databinding.ObservableInt;
import androidx.annotation.Nullable;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import android.text.TextUtils;

import com.rahimlis.badgedtablayout.BadgedTabLayout;

import java.util.ArrayList;
import java.util.List;

import de.vogler_engineering.smartdevicesapp.app.phone.ui.NavigationController;
import de.vogler_engineering.smartdevicesapp.app.phone.ui.tabs.GenericTabFragment;
import de.vogler_engineering.smartdevicesapp.common.misc.BiConsumer;
import de.vogler_engineering.smartdevicesapp.common.misc.list.ArrayListChangedListener;
import de.vogler_engineering.smartdevicesapp.common.rx.SchedulersFacade;
import de.vogler_engineering.smartdevicesapp.model.entities.tabs.TabConfig;
import de.vogler_engineering.smartdevicesapp.model.management.AppManagerImpl;
import de.vogler_engineering.smartdevicesapp.model.repository.JobRepository;
import de.vogler_engineering.smartdevicesapp.model.repository.TabRepository;
import io.reactivex.Single;
import timber.log.Timber;

/**
 * Class to manage the TabPager on the MainActivity. Dynamically generates the Tabs from the
 * TabConfiguration, redirects Navigation and updates other Components when the selectedTab changed.
 */
public class TabPagerManager implements TabLayout.OnTabSelectedListener, ViewPager.OnPageChangeListener, NavigationController.MainTabChangeCallback {

    private static final String TAG = "TabPagerManager";

    private final MainActivity activity;
    private final AppManagerImpl appManager;
    private final TabRepository tabConfigRepository;
    private final JobRepository jobRepository;
    private final NavigationController navigationController;
    private final ObservableArrayList<TabConfig> mTabConfig;
    private final ViewPager mViewPager;
    private final BadgedTabLayout mTabLayout;
    private final SchedulersFacade schedulersFacade;
    private final List<GenericTabFragment> mFragments = new ArrayList<>();
    private final MutableLiveData<Boolean> containsData = new MutableLiveData<>();

    private BiConsumer<String, Integer> tabCountListener;

    private SectionsPagerAdapter mSectionsPagerAdapter;

    private ObservableInt currentTabIndex = new ObservableInt(0);

    public TabPagerManager(MainActivity activity,
                           AppManagerImpl appManager,
                           TabRepository tabConfigRepository,
                           JobRepository jobRepository,
                           NavigationController navigationController,
                           ViewPager mViewPager,
                           BadgedTabLayout mTabLayout, SchedulersFacade schedulersFacade) {
        this.activity = activity;
        this.appManager = appManager;
        this.tabConfigRepository = tabConfigRepository;
        this.mTabConfig = tabConfigRepository.getTabConfig();
        this.jobRepository = jobRepository;
        this.navigationController = navigationController;

        this.mViewPager = mViewPager;
        this.mTabLayout = mTabLayout;
        this.schedulersFacade = schedulersFacade;
        this.containsData.setValue(false);

        updatePagerAdapter();
    }

    private void updatePagerAdapter() {
        this.mViewPager.setAdapter(null);
        this.mSectionsPagerAdapter = new SectionsPagerAdapter(activity.getSupportFragmentManager());
        this.mSectionsPagerAdapter.notifyDataSetChanged();
        this.mViewPager.setAdapter(mSectionsPagerAdapter);
    }

    public void initTabPager() {
        mTabLayout.setTabTruncateAt(TextUtils.TruncateAt.END);
        mTabLayout.setBadgeTruncateAt(TextUtils.TruncateAt.MARQUEE);
        mTabLayout.setupWithViewPager(mViewPager);

        for (int i = 0; i < mTabConfig.size(); i++) {
            TabConfig cfg = mTabConfig.get(i);
            mFragments.add(createFragment(cfg));
        }

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabLayout));
        mTabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));

        mViewPager.addOnPageChangeListener(this);
        mTabLayout.addOnTabSelectedListener(this);

        mTabConfig.addOnListChangedCallback(new OnTabConfigChangeCallback());
        mSectionsPagerAdapter.notifyDataSetChanged();

        tabCountListener = this::updateBadgeNumber;
        updateContainsData();
    }

    private int getIndexFromKey(String tabKey) {
        for (int i = 0; i < mTabConfig.size(); i++) {
            TabConfig tc = mTabConfig.get(i);
            if (tc.getKey().equals(tabKey)) {
                return i;
            }
        }
        return -1;
    }

    private void updateBadgeNumber(String tabKey, Integer count) {
        final int tabIdx = getIndexFromKey(tabKey);
        updateBadgeNumber(tabIdx, count);
    }

    private void updateBadgeNumber(final int tabIdx, final int count) {
        final TabConfig tc = mTabConfig.get(tabIdx);
        activity.addDisposable(Single.fromCallable(() -> {
            if (tc.isShowBadgeNumber()) {
                if (count == 0) {
                    mTabLayout.setBadgeText(tabIdx, null);
                } else {
                    mTabLayout.setBadgeText(tabIdx, String.valueOf(count));
                }
            } else {
                mTabLayout.setBadgeText(tabIdx, null);
            }
            return true;
        })
                .subscribeOn(schedulersFacade.ui())
                .subscribe(
                        (r) -> {
                        },
                        (err) -> Timber.tag(TAG).e(err, "Could not set Tab-Badge!")));
    }

    private GenericTabFragment createFragment(TabConfig tabConfig) {
        return GenericTabFragment.create(tabConfig.getKey());
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {
    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        Timber.tag(TAG).d("Selected tab pos: %d", position);

        TabConfig tc = this.mTabConfig.get(position);
        activity.updateNavigationTabSelection(position, tc);
        currentTabIndex.set(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    public GenericTabFragment getTabFragment(int index) {
        return mFragments.get(index);
    }

    public int getCurrentTabIndex() {
        return currentTabIndex.get();
    }

    public ObservableInt getCurrentTabIndexObservable() {
        return currentTabIndex;
    }

    public boolean isCurrentTabMainTab() {
        return mTabConfig.get(currentTabIndex.get()).isMainTab();
    }

    public int getMainTabIndex() {
        for (int i = 0; i < mTabConfig.size(); i++) {
            if (mTabConfig.get(i).isMainTab()) return i;
        }
        return -1;
    }

    public int getTabIndex(String tabKey) {
        for (int i = 0; i < mTabConfig.size(); i++) {
            if (mTabConfig.get(i).getKey().equals(tabKey)) return i;
        }
        return -1;
    }

    @Override
    public void navigateToMainTab() {
        int idx = getMainTabIndex();
        if (idx >= 0) {
            mViewPager.setCurrentItem(getMainTabIndex());
        }
    }

    @Override
    public void navigateToTab(String tabKey) throws IllegalArgumentException {
        int i = getTabIndex(tabKey);
        if (i < 0) {
            throw new IllegalArgumentException("Tab not found!");
        }
        navigateToTabIndex(i);
    }

    public void navigateToTabIndex(int index) {
        mViewPager.setCurrentItem(index);
    }

    public TabConfig getTabConfig(int tabIdx) {
        return mTabConfig.get(tabIdx);
    }

    public void registerReceivers() {
        navigationController.setMainTabChangeCallback(this);
        jobRepository.setTabCountChangeListener(tabCountListener);

        for (int i = 0; i < mTabConfig.size(); i++) {
            TabConfig tabConfig = mTabConfig.get(i);
            updateBadgeNumber(i, jobRepository.getEntries(tabConfig.getKey()).size());
        }

    }

    public void unregisterReceivers() {
        navigationController.setMainTabChangeCallback(null);
        jobRepository.setTabCountChangeListener(null);
    }

    private void updateContainsData() {
        if (mTabConfig.size() > 0) {
            containsData.postValue(true);
        } else {
            containsData.postValue(false);
        }
    }

    public boolean getContainsData() {
        Boolean b = containsData.getValue();
        return b == null ? false : b;
    }

    public LiveData<Boolean> getContainsDataObservable() {
        return containsData;
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            TabConfig config = mTabConfig.get(position);
            return GenericTabFragment.create(config.getKey());
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            TabConfig config = mTabConfig.get(position);
            return config.getTitle();
        }

        @Override
        public long getItemId(int position) {
            TabConfig config = mTabConfig.get(position);
            return config.getKey().hashCode();
        }

        @Override
        public int getCount() {
            return mTabConfig.size();
        }
    }

    public class OnTabConfigChangeCallback extends ArrayListChangedListener<TabConfig> {

        @Override
        public void onItemRangeChanged(ObservableArrayList<TabConfig> sender, int positionStart, int itemCount) {
            activity.addDisposable(Single.fromCallable(() -> true)
                    .subscribeOn(schedulersFacade.ui())
                    .subscribe((b) -> {
                        try {
                            for (int i = 0; i < itemCount; i++) {
                                final int pos = i + positionStart;
                                TabConfig cfg = sender.get(pos);
                                mFragments.set(pos, createFragment(sender.get(pos)));

                                int cnt = jobRepository.getEntries(cfg.getKey()).size();
                                updateBadgeNumber(pos, cnt);
                            }
                            updateContainsData();
                            mSectionsPagerAdapter.notifyDataSetChanged();
                        } catch (Exception e) {
                            Timber.tag(TAG).e(e, "Exception in TabConfigChangeCallback$onItemRangeChanged");
                        }
                    }));
        }

        @Override
        public void onItemRangeInserted(ObservableArrayList<TabConfig> sender, int positionStart, int itemCount) {
            activity.addDisposable(Single.fromCallable(() -> true)
                    .subscribeOn(schedulersFacade.ui())
                    .subscribe((b) -> {
                        try {
                            boolean updatedActiveTab = false;
                            for (int i = 0; i < itemCount; i++) {
                                final int pos = i + positionStart;
                                TabConfig cfg = sender.get(pos);
                                int currentTabIndex = mViewPager.getCurrentItem();
                                if (currentTabIndex == pos) {
                                    updatedActiveTab = true;
                                }

                                mFragments.add(pos, createFragment(cfg));
                                int cnt = jobRepository.getEntries(cfg.getKey()).size();
                                updateBadgeNumber(pos, cnt);
                            }
                            updateContainsData();

                            mSectionsPagerAdapter.notifyDataSetChanged();
                            if (updatedActiveTab) {
                                //Rebind ViewPager in case that the active tab has been changed.
                                updatePagerAdapter();
                            }
                        } catch (Exception e) {
                            Timber.tag(TAG).e(e, "Exception in TabConfigChangeCallback$onItemRangeInserted");
                        }
                    }));
        }

        @Override
        public void onItemRangeMoved(ObservableArrayList<TabConfig> sender, int fromPosition, int toPosition, int itemCount) {
            Timber.tag(TAG).e("onItemRangeMoved is not implemented!");
        }

        @Override
        public void onItemRangeRemoved(ObservableArrayList<TabConfig> sender, int positionStart, int itemCount) {
            activity.addDisposable(Single.fromCallable(() -> true)
                    .subscribeOn(schedulersFacade.ui())
                    .subscribe((b) -> {
                        try {
                            boolean updatedActiveTab = false;
                            for (int i = 0; i < itemCount; i++) {
                                final int pos = i + positionStart;
                                int currentTabIndex = mViewPager.getCurrentItem();
                                if (currentTabIndex == pos) {
                                    updatedActiveTab = true;
                                }
                                mFragments.remove(pos);
                            }
                            updateContainsData();

                            mSectionsPagerAdapter.notifyDataSetChanged();
                            if (updatedActiveTab) {
                                //Rebind ViewPager in case that the active tab has been changed.
                                updatePagerAdapter();
                            }
                        } catch (Exception e) {
                            Timber.tag(TAG).e(e, "Exception in TabConfigChangeCallback$onItemRangeRemoved");
                        }
                    }));
        }
    }
}
