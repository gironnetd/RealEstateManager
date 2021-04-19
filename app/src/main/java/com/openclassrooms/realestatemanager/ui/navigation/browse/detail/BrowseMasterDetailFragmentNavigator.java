package com.openclassrooms.realestatemanager.ui.navigation.browse.detail;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.CallSuper;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentFactory;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigator;
import androidx.navigation.NavigatorProvider;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

@Navigator.Name("browse_master_detail_fragment")
public class BrowseMasterDetailFragmentNavigator extends Navigator<androidx.navigation.fragment.FragmentNavigator.Destination> {
    private static final String TAG = "FragmentNavigator";
    private static final String KEY_BACK_STACK_IDS = "androidx-nav-fragment:navigator:backStackIds";

    private final Context mContext;
    private final FragmentManager mFragmentManager;
    private final int mContainerId;
    private ArrayDeque<Integer> mBackStack = new ArrayDeque<>();

    public BrowseMasterDetailFragmentNavigator(@NonNull Context context, @NonNull FragmentManager manager,
                                               int containerId) {
        mContext = context;
        mFragmentManager = manager;
        mContainerId = containerId;
    }

    /**
     * {@inheritDoc}
     * <p>
     * This method must call
     * {@link FragmentTransaction#setPrimaryNavigationFragment(Fragment)}
     * if the pop succeeded so that the newly visible Fragment can be retrieved with
     * {@link FragmentManager#getPrimaryNavigationFragment()}.
     * <p>
     * Note that the default implementation pops the Fragment
     * asynchronously, so the newly visible Fragment from the back stack
     * is not instantly available after this call completes.
     */
    @Override
    public boolean popBackStack() {
        if (mBackStack.isEmpty()) {
            return false;
        }
        if (mFragmentManager.isStateSaved()) {
            Log.i(TAG, "Ignoring popBackStack() call: FragmentManager has already"
                    + " saved its state");
            return false;
        }
        mFragmentManager.popBackStack(
                generateBackStackName(mBackStack.size(), mBackStack.peekLast()),
                FragmentManager.POP_BACK_STACK_INCLUSIVE);
        mBackStack.removeLast();
        return true;
    }

    @NonNull
    @Override
    public androidx.navigation.fragment.FragmentNavigator.Destination createDestination() {
        return new androidx.navigation.fragment.FragmentNavigator.Destination(this);
    }

    /**
     * Instantiates the Fragment via the FragmentManager's
     * {@link FragmentFactory}.
     *
     * Note that this method is <strong>not</strong> responsible for calling
     * {@link Fragment#setArguments(Bundle)} on the returned Fragment instance.
     *
     * @param context Context providing the correct {@link ClassLoader}
     * @param fragmentManager FragmentManager the Fragment will be added to
     * @param className The Fragment to instantiate
     * @param args The Fragment's arguments, if any
     * @return A new fragment instance.
     * @deprecated Set a custom {@link FragmentFactory} via
     * {@link FragmentManager#setFragmentFactory(FragmentFactory)} to control
     * instantiation of Fragments.
     */
    @SuppressWarnings("DeprecatedIsStillUsed") // needed to maintain forward compatibility
    @Deprecated
    @NonNull
    public Fragment instantiateFragment(@NonNull Context context,
                                        @NonNull FragmentManager fragmentManager,
                                        @NonNull String className, @SuppressWarnings("unused") @Nullable Bundle args) {
        return fragmentManager.getFragmentFactory().instantiate(
                context.getClassLoader(), className);
    }

    /**
     * {@inheritDoc}
     * <p>
     * This method should always call
     * {@link FragmentTransaction#setPrimaryNavigationFragment(Fragment)}
     * so that the Fragment associated with the new destination can be retrieved with
     * {@link FragmentManager#getPrimaryNavigationFragment()}.
     * <p>
     * Note that the default implementation commits the new Fragment
     * asynchronously, so the new Fragment is not instantly available
     * after this call completes.
     */
    @SuppressWarnings("deprecation") /* Using instantiateFragment for forward compatibility */
    @Nullable
    @Override
    public NavDestination navigate(@NonNull androidx.navigation.fragment.FragmentNavigator.Destination destination, @Nullable Bundle args,
                                   @Nullable NavOptions navOptions, @Nullable Navigator.Extras navigatorExtras) {
        if (mFragmentManager.isStateSaved()) {
            Log.i(TAG, "Ignoring navigate() call: FragmentManager has already"
                    + " saved its state");
            return null;
        }
        String className = destination.getClassName();
        if (className.charAt(0) == '.') {
            className = mContext.getPackageName() + className;
        }

        final FragmentTransaction ft = mFragmentManager.beginTransaction();
        String tag = Integer.toString(destination.getId());

        Boolean initialNavigate = false;
        Fragment currentFragment = mFragmentManager.getPrimaryNavigationFragment();

        if (currentFragment != null) {
            ft.detach(currentFragment);
            //transaction.hide(currentFragment)
        } else {
            initialNavigate = true;
        }

        Fragment fragment = mFragmentManager.findFragmentByTag(tag);

        if(currentFragment != null
                && currentFragment.getClass().getName().equals(className)
                && fragment != null) {
            fragment = instantiateFragment(mContext, mFragmentManager, className, args);
            fragment.setArguments(args);
            ft.replace(mContainerId, fragment, tag);
        } else if (fragment == null) {
            //String className = destination.getClassName();
            fragment = instantiateFragment(mContext, mFragmentManager, className, args);
            fragment.setArguments(args);
            ft.add(mContainerId, fragment, tag);
        } else {
            fragment.setArguments(args);
            ft.attach(fragment);
            //transaction.show(fragment)
        }

        ft.setPrimaryNavigationFragment(fragment);

        final @IdRes int destId = destination.getId();
        final boolean initialNavigation = mBackStack.isEmpty();
        // TODO Build first class singleTop behavior for fragments
        final boolean isSingleTopReplacement = navOptions != null && !initialNavigation
                && navOptions.shouldLaunchSingleTop()
                && mBackStack.peekLast() == destId;

        boolean isAdded;
        if (initialNavigation) {
            isAdded = true;
        } else if (isSingleTopReplacement) {
            // Single Top means we only want one instance on the back stack
            if (mBackStack.size() > 1) {
                // If the Fragment to be replaced is on the FragmentManager's
                // back stack, a simple replace() isn't enough so we
                // remove it from the back stack and put our replacement
                // on the back stack in its place
                mFragmentManager.popBackStack(
                        generateBackStackName(mBackStack.size(), mBackStack.peekLast()),
                        FragmentManager.POP_BACK_STACK_INCLUSIVE);
                ft.addToBackStack(generateBackStackName(mBackStack.size(), destId));
            }
            isAdded = false;
        } else {
            ft.addToBackStack(generateBackStackName(mBackStack.size() + 1, destId));
            isAdded = true;
        }
        if (navigatorExtras instanceof Extras) {
            Extras extras = (Extras) navigatorExtras;
            for (Map.Entry<View, String> sharedElement : extras.getSharedElements().entrySet()) {
                ft.addSharedElement(sharedElement.getKey(), sharedElement.getValue());
            }
        }
        ft.setReorderingAllowed(true);
        ft.commit();
        // The commit succeeded, update our view of the world
        if (isAdded) {
            mBackStack.add(destId);
            return destination;
        } else {
            return null;
        }
    }

//    @Nullable
//    @Override
//    public NavDestination navigate(@NonNull androidx.navigation.fragment.FragmentNavigator.Destination destination, @Nullable Bundle args,
//                                   @Nullable NavOptions navOptions, @Nullable Navigator.Extras navigatorExtras) {
//        if (mFragmentManager.isStateSaved()) {
//
//            Log.i(TAG, "Ignoring navigate() call: FragmentManager has already"
//                    + " saved its state");
//            return null;
//        }
//        String className = destination.getClassName();
//        if (className.charAt(0) == '.') {
//            className = mContext.getPackageName() + className;
//        }
//
//        final FragmentTransaction ft = mFragmentManager.beginTransaction();
//
//        int enterAnim = navOptions != null ? navOptions.getEnterAnim() : -1;
//        int exitAnim = navOptions != null ? navOptions.getExitAnim() : -1;
//        int popEnterAnim = navOptions != null ? navOptions.getPopEnterAnim() : -1;
//        int popExitAnim = navOptions != null ? navOptions.getPopExitAnim() : -1;
//        if (enterAnim != -1 || exitAnim != -1 || popEnterAnim != -1 || popExitAnim != -1) {
//            enterAnim = enterAnim != -1 ? enterAnim : 0;
//            exitAnim = exitAnim != -1 ? exitAnim : 0;
//            popEnterAnim = popEnterAnim != -1 ? popEnterAnim : 0;
//            popExitAnim = popExitAnim != -1 ? popExitAnim : 0;
//            ft.setCustomAnimations(enterAnim, exitAnim, popEnterAnim, popExitAnim);
//        }
//
//        String tag = Integer.toString(destination.getId());
//        Fragment primaryNavigationFragment = mFragmentManager.getPrimaryNavigationFragment();
//        if(primaryNavigationFragment != null)
//            ft.hide(primaryNavigationFragment);
//        Fragment destinationFragment = mFragmentManager.findFragmentByTag(tag);
//        if(destinationFragment == null) {
//            destinationFragment = instantiateFragment(mContext, mFragmentManager, className, args);
//            destinationFragment.setArguments(args);
//            ft.add(mContainerId, destinationFragment , tag);
//        }
//        else
//            ft.show(destinationFragment);
//
//        ft.setPrimaryNavigationFragment(destinationFragment);
//
//        final @IdRes int destId = destination.getId();
//        final boolean initialNavigation = mBackStack.isEmpty();
//        // TODO Build first class singleTop behavior for fragments
//        final boolean isSingleTopReplacement = navOptions != null && !initialNavigation
//                && navOptions.shouldLaunchSingleTop()
//                && mBackStack.peekLast() == destId;
//
//        boolean isAdded;
//        if (initialNavigation) {
//            isAdded = true;
//        } else if (isSingleTopReplacement) {
//            // Single Top means we only want one instance on the back stack
//            if (mBackStack.size() > 1) {
//                // If the Fragment to be replaced is on the FragmentManager's
//                // back stack, a simple replace() isn't enough so we
//                // remove it from the back stack and put our replacement
//                // on the back stack in its place
//                mFragmentManager.popBackStackImmediate(
//                        generateBackStackName(mBackStack.size(), mBackStack.peekLast()), 0);
//                mIsPendingBackStackOperation = false;
//            }
//            isAdded = false;
//        } else {
//            ft.addToBackStack(generateBackStackName(mBackStack.size() + 1, destId));
//            mIsPendingBackStackOperation = true;
//            isAdded = true;
//        }
//        if (navigatorExtras instanceof Extras) {
//            Extras extras = (Extras) navigatorExtras;
//            for (Map.Entry<View, String> sharedElement : extras.getSharedElements().entrySet()) {
//                ft.addSharedElement(sharedElement.getKey(), sharedElement.getValue());
//            }
//        }
//        ft.setReorderingAllowed(true);
//        ft.commit();
//        // The commit succeeded, update our view of the world
//        if (isAdded) {
//            mBackStack.add(destId);
//            return destination;
//        } else {
//            return null;
//        }
//    }

    @Override
    @Nullable
    public Bundle onSaveState() {
        Bundle b = new Bundle();
        int[] backStack = new int[mBackStack.size()];
        int index = 0;
        for (Integer id : mBackStack) {
            backStack[index++] = id;
        }
        b.putIntArray(KEY_BACK_STACK_IDS, backStack);
        return b;
    }

    @Override
    public void onRestoreState(@Nullable Bundle savedState) {
        if (savedState != null) {
            int[] backStack = savedState.getIntArray(KEY_BACK_STACK_IDS);
            if (backStack != null) {
                mBackStack.clear();
                for (int destId : backStack) {
                    mBackStack.add(destId);
                }
            }
        }
    }

    @NonNull
    private String generateBackStackName(int backStackIndex, int destId) {
        return backStackIndex + "-" + destId;
    }

    /**
     * NavDestination specific to {@link androidx.navigation.fragment.FragmentNavigator}
     */
    @NavDestination.ClassType(Fragment.class)
    public static class Destination extends NavDestination {

        private String mClassName;

        /**
         * Construct a new fragment destination. This destination is not valid until you set the
         * Fragment via {@link #setClassName(String)}.
         *
         * @param navigatorProvider The {@link NavController} which this destination
         *                          will be associated with.
         */
        public Destination(@NonNull NavigatorProvider navigatorProvider) {
            this(navigatorProvider.getNavigator(androidx.navigation.fragment.FragmentNavigator.class));
        }

        /**
         * Construct a new fragment destination. This destination is not valid until you set the
         * Fragment via {@link #setClassName(String)}.
         *
         * @param fragmentNavigator The {@link androidx.navigation.fragment.FragmentNavigator} which this destination
         *                          will be associated with. Generally retrieved via a
         *                          {@link NavController}'s
         *                          {@link NavigatorProvider#getNavigator(Class)} method.
         */
        public Destination(@NonNull Navigator<? extends androidx.navigation.fragment.FragmentNavigator.Destination> fragmentNavigator) {
            super(fragmentNavigator);
        }

        @CallSuper
        @Override
        public void onInflate(@NonNull Context context, @NonNull AttributeSet attrs) {
            super.onInflate(context, attrs);
            TypedArray a = context.getResources().obtainAttributes(attrs,
                    androidx.navigation.fragment.R.styleable.FragmentNavigator);
            String className = a.getString(androidx.navigation.fragment.R.styleable.FragmentNavigator_android_name);
            if (className != null) {
                setClassName(className);
            }
            a.recycle();
        }

        /**
         * Set the Fragment class name associated with this destination
         * @param className The class name of the Fragment to show when you navigate to this
         *                  destination
         * @return this {@link androidx.navigation.fragment.FragmentNavigator.Destination}
         */
        @NonNull
        public final Destination setClassName(@NonNull String className) {
            mClassName = className;
            return this;
        }

        /**
         * Gets the Fragment's class name associated with this destination
         *
         * @throws IllegalStateException when no Fragment class was set.
         */
        @NonNull
        public final String getClassName() {
            if (mClassName == null) {
                throw new IllegalStateException("Fragment class was not set");
            }
            return mClassName;
        }

        @NonNull
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(super.toString());
            sb.append(" class=");
            if (mClassName == null) {
                sb.append("null");
            } else {
                sb.append(mClassName);
            }
            return sb.toString();
        }
    }

    /**
     * Extras that can be passed to FragmentNavigator to enable Fragment specific behavior
     */
    public static final class Extras implements Navigator.Extras {
        private final LinkedHashMap<View, String> mSharedElements = new LinkedHashMap<>();

        Extras(Map<View, String> sharedElements) {
            mSharedElements.putAll(sharedElements);
        }

        /**
         * Gets the map of shared elements associated with these Extras. The returned map
         * is an {@link Collections#unmodifiableMap(Map) unmodifiable} copy of the underlying
         * map and should be treated as immutable.
         */
        @NonNull
        public Map<View, String> getSharedElements() {
            return Collections.unmodifiableMap(mSharedElements);
        }

        /**
         * Builder for constructing new {@link androidx.navigation.fragment.FragmentNavigator.Extras} instances. The resulting instances are
         * immutable.
         */
        public static final class Builder {
            private final LinkedHashMap<View, String> mSharedElements = new LinkedHashMap<>();

            /**
             * Adds multiple shared elements for mapping Views in the current Fragment to
             * transitionNames in the Fragment being navigated to.
             *
             * @param sharedElements Shared element pairs to add
             * @return this {@link androidx.navigation.fragment.FragmentNavigator.Extras.Builder}
             */
            @NonNull
            public Builder addSharedElements(@NonNull Map<View, String> sharedElements) {
                for (Map.Entry<View, String> sharedElement : sharedElements.entrySet()) {
                    View view = sharedElement.getKey();
                    String name = sharedElement.getValue();
                    if (view != null && name != null) {
                        addSharedElement(view, name);
                    }
                }
                return this;
            }

            /**
             * Maps the given View in the current Fragment to the given transition name in the
             * Fragment being navigated to.
             *
             * @param sharedElement A View in the current Fragment to match with a View in the
             *                      Fragment being navigated to.
             * @param name The transitionName of the View in the Fragment being navigated to that
             *             should be matched to the shared element.
             * @return this {@link androidx.navigation.fragment.FragmentNavigator.Extras.Builder}
             * @see FragmentTransaction#addSharedElement(View, String)
             */
            @NonNull
            public Builder addSharedElement(@NonNull View sharedElement, @NonNull String name) {
                mSharedElements.put(sharedElement, name);
                return this;
            }

            /**
             * Constructs the final {@link androidx.navigation.fragment.FragmentNavigator.Extras} instance.
             *
             * @return An immutable {@link androidx.navigation.fragment.FragmentNavigator.Extras} instance.
             */
            @NonNull
            public Extras build() {
                return new Extras(mSharedElements);
            }
        }
    }
}
