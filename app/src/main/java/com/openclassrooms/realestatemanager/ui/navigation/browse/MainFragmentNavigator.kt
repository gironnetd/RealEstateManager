package com.openclassrooms.realestatemanager.ui.navigation.browse

import android.content.Context
import android.os.Bundle
import androidx.annotation.IdRes
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavDestination
import androidx.navigation.NavOptions
import androidx.navigation.Navigator
import androidx.navigation.fragment.FragmentNavigator
import timber.log.Timber
import java.util.*

@Navigator.Name("main_fragment")
class MainFragmentNavigator(
        private val mContext: Context,
        private val mFragmentManager: FragmentManager, // Should pass childFragmentManager.
        private val mContainerId: Int,
) : FragmentNavigator(mContext, mFragmentManager, mContainerId) {

    private val mBackStack = ArrayDeque<Int>()

    override fun navigate(destination: Destination, args: Bundle?, navOptions: NavOptions?, navigatorExtras: Navigator.Extras?): NavDestination? {
        if (mFragmentManager.isStateSaved) {
            Timber.tag(TAG).i("Ignoring navigate() call: FragmentManager has already saved its state")
            return null
        }
        var className = destination.className
        if (className[0] == '.') {
            className = mContext.packageName + className
        }

        val ft = mFragmentManager.beginTransaction()
        val tag = destination.id.toString()

        val currentFragment = mFragmentManager.primaryNavigationFragment
        if (currentFragment != null) {
            ft.hide(currentFragment)
        }

        var fragment = mFragmentManager.findFragmentByTag(tag)

        if (fragment == null) {
            fragment = mFragmentManager.fragmentFactory.instantiate(mContext.classLoader, className)
            fragment.arguments = args
            ft.add(mContainerId, fragment, tag)
        } else {
            fragment.arguments = args
            ft.show(fragment)
        }

        ft.setPrimaryNavigationFragment(fragment)

        @IdRes val destId = destination.id
        val initialNavigation = mBackStack.isEmpty()
        // TODO Build first class singleTop behavior for fragments
        val isSingleTopReplacement = (navOptions != null && !initialNavigation
                && navOptions.shouldLaunchSingleTop()
                && mBackStack.peekLast() == destId)

        val isAdded: Boolean = if (initialNavigation) {
            true
        } else if (isSingleTopReplacement) {
            // Single Top means we only want one instance on the back stack
            if (mBackStack.size > 1) {
                // If the Fragment to be replaced is on the FragmentManager's
                // back stack, a simple replace() isn't enough so we
                // remove it from the back stack and put our replacement
                // on the back stack in its place
                mFragmentManager.popBackStack(
                        generateBackStackName(mBackStack.size, mBackStack.peekLast()),
                        FragmentManager.POP_BACK_STACK_INCLUSIVE)
                ft.addToBackStack(generateBackStackName(mBackStack.size, destId))
            }
            false
        } else {
            ft.addToBackStack(generateBackStackName(mBackStack.size + 1, destId))
            true
        }
        if (navigatorExtras is Extras) {
            for ((key, value) in navigatorExtras.sharedElements) {
                ft.addSharedElement(key!!, value!!)
            }
        }
        ft.setReorderingAllowed(true)
        ft.commit()
        // The commit succeeded, update our view of the world
        return if (isAdded) {
            mBackStack.add(destId)
            destination
        } else {
            null
        }
    }

    private fun generateBackStackName(backStackIndex: Int, destId: Int): String {
        return "$backStackIndex-$destId"
    }

    companion object {
        private const val TAG = "MainFragmentNav"
    }
}