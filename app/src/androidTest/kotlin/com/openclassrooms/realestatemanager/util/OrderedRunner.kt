package com.openclassrooms.realestatemanager.util

import org.junit.runners.BlockJUnit4ClassRunner
import org.junit.runners.model.FrameworkMethod
import java.util.*

class OrderedRunner(clazz: Class<*>?) : BlockJUnit4ClassRunner(clazz) {
    override fun computeTestMethods(): List<FrameworkMethod> {
        val list = super.computeTestMethods()
        val copy: List<FrameworkMethod> = ArrayList(list)
        Collections.sort(copy) { f1, f2 ->
            val o1 = f1.getAnnotation(
                Order::class.java)
            val o2 = f2.getAnnotation(
                Order::class.java)
            if (o1 == null || o2 == null) {
                -1
            } else o1.order - o2.order
        }
        return copy
    }
}