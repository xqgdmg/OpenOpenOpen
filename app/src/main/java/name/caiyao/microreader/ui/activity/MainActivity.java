package name.caiyao.microreader.ui.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.transition.Slide;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import butterknife.BindView;
import butterknife.ButterKnife;
import name.caiyao.microreader.BuildConfig;
import name.caiyao.microreader.R;
import name.caiyao.microreader.bean.UpdateItem;
import name.caiyao.microreader.config.Config;
import name.caiyao.microreader.event.StatusBarEvent;
import name.caiyao.microreader.presenter.IMainPresenter;
import name.caiyao.microreader.presenter.impl.MainPresenterImpl;
import name.caiyao.microreader.ui.fragment.GuokrFragment;
import name.caiyao.microreader.ui.fragment.ItHomeFragment;
import name.caiyao.microreader.ui.fragment.VideoFragment;
import name.caiyao.microreader.ui.fragment.WeixinFragment;
import name.caiyao.microreader.ui.fragment.ZhihuFragment;
import name.caiyao.microreader.ui.iView.IMain;
import name.caiyao.microreader.utils.RxBus;
import name.caiyao.microreader.utils.SharePreferenceUtil;
import rx.Subscription;
import rx.functions.Action1;

public class MainActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener, IMain {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.nav_view)
    NavigationView navView;
    @BindView(R.id.drawer_layout)
    DrawerLayout drawerLayout;
    @BindView(R.id.ctl_main)
    CoordinatorLayout ctlMain;

    private Fragment currentFragment;
    private ArrayList<Fragment> mFragments;
    private ArrayList<Integer> mTitles;
    private IMainPresenter IMainPresenter;
    private Subscription rxSubscription;
    private SharedPreferences mSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            if (Config.isNight) {
                getDelegate().setLocalNightMode(
                        AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                getDelegate().setLocalNightMode(
                        AppCompatDelegate.MODE_NIGHT_NO);
            }
            // 调用 recreate() 使设置生效
            recreate();
        }

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

        mSharedPreferences = getSharedPreferences(SharePreferenceUtil.SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);

        setSupportActionBar(toolbar);
        IMainPresenter = new MainPresenterImpl(this);

        boolean isKitKat = Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT;
        if (isKitKat)
            ctlMain.setFitsSystemWindows(false);
        setToolBar(null, toolbar, true, false, drawerLayout);
        //改变statusBar颜色而DrawerLayout依然可以显示在StatusBar
        //ctlMain.setStatusBarBackgroundColor(setToolBar(toolbar,true,false,null));
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        View headerLayout = navView.getHeaderView(0);
        LinearLayout llImage = (LinearLayout) headerLayout.findViewById(R.id.side_image);
        TextView imageDescription = (TextView) headerLayout.findViewById(R.id.image_description);

        assert navView != null;
        navView.setNavigationItemSelectedListener(this);
        int[][] state = new int[][]{
                new int[]{-android.R.attr.state_checked}, // unchecked
                new int[]{android.R.attr.state_checked}  // pressed
        };

        getSwipeBackLayout().setEnableGesture(false);

        int[] color = new int[]{
                Color.BLACK,
                getSharedPreferences(SharePreferenceUtil.SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE).getInt(SharePreferenceUtil.MUTED, ContextCompat.getColor(this, R.color.colorAccent))
        };
        if (Config.isNight) {
            llImage.setAlpha(0.2f);
            color = new int[]{
                    Color.DKGRAY,
                    getSharedPreferences(SharePreferenceUtil.SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE).getInt(SharePreferenceUtil.MUTED, ContextCompat.getColor(this, R.color.colorAccent))
            };
        }
        navView.setItemTextColor(new ColorStateList(state, color));
        navView.setItemIconTintList(new ColorStateList(state, color));

        if (new File(getFilesDir().getPath() + "/bg.jpg").exists()) {
            BitmapDrawable bitmapDrawable = new BitmapDrawable(getResources(), getFilesDir().getPath() + "/bg.jpg");
            llImage.setBackground(bitmapDrawable);
            imageDescription.setText(getSharedPreferences(SharePreferenceUtil.SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE).getString(SharePreferenceUtil.IMAGE_DESCRIPTION, "我的愿望，就是希望你的愿望里，也有我"));
        }
        initMenu();
        IMainPresenter.checkUpdate();

        rxSubscription = RxBus.getDefault().toObservable(StatusBarEvent.class)
                .subscribe(new Action1<StatusBarEvent>() {
                    @Override
                    public void call(StatusBarEvent statusBarEvent) {
                        finish();
                        startActivity(new Intent(MainActivity.this, MainActivity.class));
                    }
                });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        assert drawer != null;
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void switchFragment(Fragment fragment, String title) {
        Slide slideTransition;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //Gravity.START部分机型崩溃java.lang.IllegalArgumentException: Invalid slide direction
            slideTransition = new Slide(Gravity.LEFT);
            slideTransition.setDuration(700);
            fragment.setEnterTransition(slideTransition);
            fragment.setExitTransition(slideTransition);
        }
        if (currentFragment == null || !currentFragment.getClass().getName().equals(fragment.getClass().getName())) {
            getSupportFragmentManager().beginTransaction().replace(R.id.replace, fragment).commit();
            currentFragment = fragment;
            ActionBar actionBar = getSupportActionBar();
            assert actionBar != null;
            actionBar.setTitle(title);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id < mFragments.size()) {
            switchFragment(mFragments.get(id), getString(mTitles.get(id)));
        }
        switch (id) {
            case R.id.nav_night:
                Config.isNight = !Config.isNight;
                finish();
                startActivity(new Intent(this, MainActivity.class));
                break;
            case R.id.nav_setting:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            case R.id.nav_change:
                startActivity(new Intent(this, ChangeChannelActivity.class));
                break;
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        assert drawer != null;
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!rxSubscription.isUnsubscribed()) {
            rxSubscription.unsubscribe();
        }
        IMainPresenter.unsubcrible();
    }

    private void initMenu() {
        ArrayList<Config.Channel> savedChannelList = new ArrayList<>();
        mTitles = new ArrayList<>();
        mFragments = new ArrayList<>();
        Menu menu = navView.getMenu();
        menu.clear();
        String savedChannel = mSharedPreferences.getString(SharePreferenceUtil.SAVED_CHANNEL, null);
        if (TextUtils.isEmpty(savedChannel)) {
            Collections.addAll(savedChannelList, Config.Channel.values());
        } else {
            for (String s : savedChannel.split(",")) {
                savedChannelList.add(Config.Channel.valueOf(s));
            }
        }
        for (int i = 0; i < savedChannelList.size(); i++) {
            MenuItem menuItem = menu.add(0, i, 0, savedChannelList.get(i).getTitle());
            mTitles.add(savedChannelList.get(i).getTitle());
            menuItem.setIcon(savedChannelList.get(i).getIcon());
            menuItem.setCheckable(true);
            addFragment(savedChannelList.get(i).name());
            if (i == 0) {
                menuItem.setChecked(true);
            }
        }
        navView.inflateMenu(R.menu.activity_main_drawer);
        switchFragment(mFragments.get(0), getString(mTitles.get(0)));
    }

    private void addFragment(String name) {
        switch (name) {
            case "GUOKR":
                mFragments.add(new GuokrFragment());
                break;
            case "WEIXIN":
                mFragments.add(new WeixinFragment());
                break;
            case "ZHIHU":
                mFragments.add(new ZhihuFragment());
                break;
            case "VIDEO":
                mFragments.add(new VideoFragment());
                break;
            case "IT":
                mFragments.add(new ItHomeFragment());
                break;
        }
    }

    @Override
    public void showUpdate(final UpdateItem updateItem) {
        if (updateItem.getVersionCode() > BuildConfig.VERSION_CODE)
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle(getString(R.string.update_title))
                    .setMessage(String.format(getString(R.string.update_description), updateItem.getVersionName(), updateItem.getReleaseNote()))
                    .setPositiveButton(getString(R.string.update_button), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(updateItem.getDownloadUrl())));
                        }
                    })
                    .setNegativeButton(R.string.common_cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .show();
    }
}
