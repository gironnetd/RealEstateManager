package com.openclassrooms.realestatemanager.util

import io.reactivex.Scheduler
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

class RxImmediateSchedulerRule: TestRule {

    override fun apply(base: Statement?, description: Description?): Statement {
        return object : Statement() {
            @Throws(Throwable::class)
            override fun evaluate() {
                RxJavaPlugins.setIoSchedulerHandler { scheduler: Scheduler? -> Schedulers.trampoline() }
                RxJavaPlugins.setComputationSchedulerHandler { scheduler: Scheduler? -> Schedulers.trampoline() }
                RxJavaPlugins.setNewThreadSchedulerHandler { scheduler: Scheduler? -> Schedulers.trampoline() }
                RxAndroidPlugins.setMainThreadSchedulerHandler { scheduler: Scheduler? -> Schedulers.trampoline() }
                RxJavaPlugins.setErrorHandler { throwable: Throwable? -> }
                try {
                    base!!.evaluate()
                } finally {
                    RxJavaPlugins.reset()
                    RxAndroidPlugins.reset()
                }
            }
        }
    }
}