package com.hm.admanagerx.utility
/*
 * Copyright 2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.view.View
import android.view.ViewGroup
import java.util.ConcurrentModificationException
import java.util.NoSuchElementException
import androidx.core.view.isNotEmpty

/**
 * Execute [action] for each child of the received [ViewGroup].
 *
 * @param action the action to execute.
 */
@Deprecated(
    message = "Use the Android KTX version",
    replaceWith = ReplaceWith("forEach(action)", "androidx.core.view.forEach")
)
inline fun ViewGroup.forEachChild(action: (View) -> Unit) {
    for (i in 0..childCount - 1) {
        action(getChildAt(i))
    }
}

/**
 * Execute [action] for each child of the received [ViewGroup].
 *
 * @param action the action to execute. The first index is 0.
 */
@Deprecated(
    message = "Use the Android KTX version",
    replaceWith = ReplaceWith("forEachIndexed(action)", "androidx.core.view.forEachIndexed")
)
inline fun ViewGroup.forEachChildWithIndex(action: (Int, View) -> Unit) {
    for (i in 0..childCount - 1) {
        action(i, getChildAt(i))
    }
}

/**
 * Return the first child [View] matching the given [predicate].
 *
 * @param predicate the predicate to check against.
 * @return the child [View] that matches [predicate].
 *   [NoSuchElementException] will be thrown if no such child was found.
 */
inline fun ViewGroup.firstChild(predicate: (View) -> Boolean): View {
    return firstChildOrNull(predicate)
        ?: throw NoSuchElementException("No element matching predicate was found.")
}

/**
 * Return the first child [View] matching the given [predicate].
 *
 * @param predicate the predicate to check against.
 * @return the child [View] that matches [predicate], or null if no such child was found.
 */
inline fun ViewGroup.firstChildOrNull(predicate: (View) -> Boolean): View? {
    for (i in 0..childCount - 1) {
        val child = getChildAt(i)
        if (predicate(child)) {
            return child
        }
    }
    return null
}

/**
 * Return the sequence of children of the received [View].
 * Note that the sequence is not thread-safe.
 *
 * @return the [Sequence] of children.
 */
@Deprecated(
    message = "Use the Android KTX version",
    replaceWith = ReplaceWith("children", "androidx.core.view.children")
)
fun View.childrenSequence(): Sequence<View> = ViewChildrenSequence(this)

/**
 * Return the [Sequence] of all children of the received [View], recursively.
 * Note that the sequence is not thread-safe.
 *
 * @return the [Sequence] of children.
 */
fun View.childrenRecursiveSequence(): Sequence<View> = ViewChildrenRecursiveSequence(this)

private class ViewChildrenSequence(private val view: View) : Sequence<View> {
    override fun iterator(): Iterator<View> {
        if (view !is ViewGroup) return emptyList<View>().iterator()
        return ViewIterator(view)
    }

    private class ViewIterator(private val view: ViewGroup) : Iterator<View> {
        private var index = 0
        private val count = view.childCount

        override fun next(): View {
            if (!hasNext()) throw NoSuchElementException()
            return view.getChildAt(index++)
        }

        override fun hasNext(): Boolean {
            checkCount()
            return index < count
        }

        private fun checkCount() {
            if (count != view.childCount) throw ConcurrentModificationException()
        }
    }
}

private class ViewChildrenRecursiveSequence(private val view: View) : Sequence<View> {
    override fun iterator(): Iterator<View> {
        if (view !is ViewGroup) return emptyList<View>().iterator()
        return RecursiveViewIterator(view)
    }

    private class RecursiveViewIterator(view: View) : Iterator<View> {
        private val sequences = arrayListOf(view.childrenSequence())

        //sequences.removeAt(sequences.size - 1).iterator()
        private var current = if (sequences.isNotEmpty()) {
            sequences.removeAt(sequences.size - 1).iterator()
        } else {
            emptyList<View>().iterator()
        }


        override fun next(): View {
            if (!hasNext()) throw NoSuchElementException()
            val view = current.next()
            if (view is ViewGroup && view.isNotEmpty()) {
                sequences.add(view.childrenSequence())
            }
            return view
        }

        override fun hasNext(): Boolean {
            if (!current.hasNext() && sequences.isNotEmpty()) {
                current = sequences.removeAt(sequences.size - 1).iterator()
            }

            return current.hasNext()
        }

    }
}
