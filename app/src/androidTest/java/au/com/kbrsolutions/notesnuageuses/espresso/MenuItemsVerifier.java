package au.com.kbrsolutions.notesnuageuses.espresso;

import android.content.res.Resources;
import android.util.Log;
import android.view.View;

import org.hamcrest.Matcher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import androidx.test.InstrumentationRegistry;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.FailureHandler;
import au.com.kbrsolutions.notesnuageuses.R;
import au.com.kbrsolutions.notesnuageuses.features.main.HomeActivity;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.anyOf;

//import android.support.test.InstrumentationRegistry;

/**
 * Verify menu items that should be or should be not available at the time when the
 * testFragmentMenus(...) id called.
 */

public class MenuItemsVerifier {

    private HomeActivity mActivity;
    Resources mResources;
    private static Map<Integer, String> allMenuIdsMap;
    private List<Integer> mHomeActivityMenuViewsIdsList;
    private List<Integer> mFragmentUnderTestMenuViewsIdsList = new ArrayList<>();

    private static final String TAG = MenuItemsVerifier.class.getSimpleName();

    void testFragmentMenus (HomeActivity activity, List<Integer> fragmentUnderTestMenuViewsIdsList) {
        mActivity = activity;
        mResources = activity.getResources();
        buildAllAvailableMenusIdsMap(mResources);

        mFragmentUnderTestMenuViewsIdsList.clear();
        mFragmentUnderTestMenuViewsIdsList.addAll(fragmentUnderTestMenuViewsIdsList);
        Log.v(TAG, "testFragmentMenus - mFragmentUnderTestMenuViewsIdsList: " + mFragmentUnderTestMenuViewsIdsList);
//        Log.v(TAG, "testFragmentMenus - mHomeActivityMenuViewsIdsList: " + mHomeActivityMenuViewsIdsList);

//        mFragmentUnderTestMenuViewsIdsList.addAll(buildActivityMenuIdsList(activity.isTwoPaneLayout()));
        for (Integer id: mFragmentUnderTestMenuViewsIdsList) {
            Log.v(TAG, "testFragmentMenus - id/text:" + id + "/" + allMenuIdsMap.get(id));
        }
        testMenus(mFragmentUnderTestMenuViewsIdsList);
    }

    private String menuText;
    private List<Integer> mValidMenuIdsList;
    private List<Integer> mExistingMenuIdsList = new ArrayList<>();
    private List<Integer> mNonExistingMenuIdsList = new ArrayList<>();

    /**
     * Verify that all menus in the validMenuIdsList exist or are displayed.
     *
     * According to the Espresso documentation the 'openActionBarOverflowOrOptionsMenu' will
     * contain all menus (displayed and existing in the overflow view). In my tests, the menus
     * that are displayed are not in the overflow view.
     *
     * At first I tried to see if the menus are displayed in the current view hierarchy. But,
     * all the menus showed as displayed. That is why I first check the overflow view. Any menu
     * that supposed to exists but was not found in the overflow menu is is checked if it is
     * displayed in the view hierarchy that was before we switch to overflow view.
     *
     * @param validMenuIdsList    list of menu ids that should be visible or exist when a fragment
     *                            is at the top of the stack
     */
    private void testMenus(List<Integer> validMenuIdsList) {
        Log.v(TAG, "testMenus - validMenuIdsList: " + validMenuIdsList);
        mValidMenuIdsList = validMenuIdsList;
        if (mValidMenuIdsList.size() > 0) {
            try {
                openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getTargetContext());
            } catch (Exception e) {
                //This is normal. Maybe we don't have overflow menu.
            }
//        onView(Core.anyOf(Core.withText(R.string.<your label for the menu item>), withId(R.id.<id of the menu item>))).perform(click());
            Set<Integer> menuIdsSet = allMenuIdsMap.keySet();

            for (final int menuId : menuIdsSet) {
                menuText = allMenuIdsMap.get(menuId);
                if (mValidMenuIdsList.contains(menuId)) {
                    onView(anyOf(withText(menuText), withId(menuId)))
                            .withFailureHandler(new FailureHandler() {
                                /* if we are here the menu exists */
                                @Override
                                public void handle(Throwable error, Matcher<View> viewMatcher) {
                                    Log.v(TAG, "testMenus (should        exist): " + menuText + " exists");
                                    mExistingMenuIdsList.add(menuId);
                                }
                            })
                            .check(doesNotExist());
                } else {
                    Log.v(TAG, "testMenus (should NOT exist): " + menuText);
                    onView(anyOf(withText(menuText), withId(menuId)))
                            .check(doesNotExist());
                    Log.v(TAG, "testMenus (should NOT exist): " + menuText + " does not exist");
                    mNonExistingMenuIdsList.add(menuId);
                }
            }
        }

        /* Close the overflow menu view */
        Espresso.pressBack();

        /* remove all menu ids that suppose to exist and exist in the overflow menu */
        mValidMenuIdsList.removeAll(mExistingMenuIdsList);
        Log.v(TAG, "testMenus - mValidMenuIdsList: " + mValidMenuIdsList);
        Log.v(TAG, "testMenus - mNonExistingMenuIdsList: " + mNonExistingMenuIdsList);

        if(mValidMenuIdsList.size() > 0) {
            testCurrentViewHierarchy();
        }

        testMenusNotExistInCurrentViewHierarchy();
//        delay(3000);
    }

    private boolean mIsMenuItemNotDisplayed;
//    private boolean mIsMenuItemDisplayed;

    /**
     *
     * Verify that menus that do not 'exist' in the overflow view, but should exist, are 'displayed'
     *  - if they are not displayed they should be in the overflow view.
     *
     */
    private void testCurrentViewHierarchy() {
        final Iterator<Integer> integerIterator = mValidMenuIdsList.iterator();
        int menuId;
        mIsMenuItemNotDisplayed = false;
        while (integerIterator.hasNext()) {
            menuId = integerIterator.next();
            menuText = allMenuIdsMap.get(menuId);
            if (mValidMenuIdsList.contains(menuId)) {
//                Log.v(TAG, "testCurrentViewHierarchy - should exists menuName: " + menuId + "/" + menuText);
                onView(withId(menuId))
                        .withFailureHandler(new FailureHandler() {
                            @Override
                            public void handle(Throwable error, Matcher<View> viewMatcher) {
                                Log.v(TAG, "testCurrentViewHierarchy - the + " + menuText + " + is not displayed");
                                mIsMenuItemNotDisplayed = true;
                            }
                        })
                        .check(matches(isDisplayed()));
                if (mIsMenuItemNotDisplayed) {
                    throw new RuntimeException("BR - " + TAG + ".testCurrentViewHierarchy - " + menuId + "/'" + menuText + "' does not exist and is not displayed");
                } else {
                    Log.v(TAG, "testCurrentViewHierarchy (should be displayed): " + menuText + " is displayed");
                }
            }
//            else {
//                Log.v(TAG, "testMenus - should be not exists menuName: " + menuText);
//                onView(anyOf(withText(menuText), withId(menuId)))
//                        .check(doesNotExist());
//            }
        }
    }

    /**
     *
     * Verify that menus that do not 'exist' in the overflow view and supposed to not display, are
     * not 'displayed'.
     *
     */
    private void testMenusNotExistInCurrentViewHierarchy() {
        final Iterator<Integer> integerIterator = mNonExistingMenuIdsList.iterator();
        int menuId;
//        mIsMenuItemDisplayed = false;
        while (integerIterator.hasNext()) {
            menuId = integerIterator.next();
            menuText = allMenuIdsMap.get(menuId);
            if (mNonExistingMenuIdsList.contains(menuId)) {
                Log.v(TAG, "testMenusNotExistInCurrentViewHierarchy - should NOT exist menuName: " + menuId + "/" + menuText);
                onView(withId(menuId))
//                        .withFailureHandler(new FailureHandler() {
//                            @Override
//                            public void handle(Throwable error, Matcher<View> viewMatcher) {
//                                Log.v(TAG, "testMenusNotExistInCurrentViewHierarchy - the + " + menuText + " + exists");
//                                mIsMenuItemDisplayed = true;
//                            }
//                        })
                        .check(doesNotExist());
//                        .check(matches(not(isDisplayed())));
//                if (mIsMenuItemDisplayed) {
//                    throw new RuntimeException("BR - " + TAG + ".testCurrentViewHierarchy - " + menuId + "/'" + menuText + "' does not exist but is displayed");
//                } else {
//                    Log.v(TAG, "testMenusNotExistInCurrentViewHierarchy (should NOT be displayed): " + menuText + " is not displayed");
//                }
            }
        }
    }

    private void buildAllAvailableMenusIdsMap(Resources resources) {
        allMenuIdsMap = new HashMap<>();
        // 'main activity'
//        allMenuIdsMap.put(R.id.action_about, resources.getString(R.string.action_about));
        allMenuIdsMap.put(R.id.menuShowTrashed, resources.getString(R.string.action_settings));
        allMenuIdsMap.put(R.id.menuHideTrashed, resources.getString(R.string.action_settings));
        // 'favorite stops fragment'
        allMenuIdsMap.put(R.id.menuCreateFile, resources.getString(R.string.action_create_file));
    }

    private List<Integer> buildActivityMenuIdsList(boolean twoPaneLayout) {
        List<Integer> mHomeActivityMenuViewsIdsList = new ArrayList<>();
//        mHomeActivityMenuViewsIdsList.add(R.id.action_about);
//        mHomeActivityMenuViewsIdsList.add(R.id.action_settings);
//        mHomeActivityMenuViewsIdsList.add(R.id.action_help);
        return mHomeActivityMenuViewsIdsList;
    }

}
