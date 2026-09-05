package com.tracker.finance_app.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Velocity

/**
 * Enhanced PullToRefreshBox that prevents accidental refresh triggers when scrolling back up
 * to the top of the page.
 *
 * In standard Material 3 PullToRefresh, when a user is scrolled down and swipes down to return to
 * the top of the list, any leftover drag/fling delta once index 0 is reached is consumed as a pull-down
 * gesture, unintentionally triggering a refresh.
 *
 * FinPullToRefreshBox ensures that pull-to-refresh can ONLY be initiated if the touch gesture
 * STARTED while the scrollable content was ALREADY at the very top.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinPullToRefreshBox(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    lazyListState: LazyListState? = null,
    canScrollBackwardProvider: (() -> Boolean)? = null,
    threshold: Dp = PullToRefreshDefaults.PositionalThreshold,
    state: PullToRefreshState = rememberPullToRefreshState(),
    contentAlignment: Alignment = Alignment.TopStart,
    indicator: @Composable BoxScope.() -> Unit = {
        PullToRefreshDefaults.Indicator(
            state = state,
            isRefreshing = isRefreshing,
            modifier = Modifier.align(Alignment.TopCenter),
            threshold = threshold
        )
    },
    content: @Composable BoxScope.() -> Unit
) {
    val checkCanScrollBackward: () -> Boolean = remember(lazyListState, canScrollBackwardProvider) {
        when {
            canScrollBackwardProvider != null -> canScrollBackwardProvider
            lazyListState != null -> { { lazyListState.canScrollBackward } }
            else -> { { false } }
        }
    }

    // Records whether the current touch gesture started when content was already at the top.
    var isGestureStartedFromTop by remember { mutableStateOf(!checkCanScrollBackward()) }

    // If currently refreshing, keep enabled so indicator displays properly.
    // Otherwise, allow pull-to-refresh only if touch gesture started from the top and content is at top.
    val isPullRefreshEnabled = isRefreshing || (isGestureStartedFromTop && !checkCanScrollBackward())

    // NestedScrollConnection to intercept scroll gestures synchronously
    val nestedScrollConnection = remember(checkCanScrollBackward) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (source == NestedScrollSource.UserInput) {
                    if (checkCanScrollBackward()) {
                        // User started dragging or is dragging while scrolled down
                        isGestureStartedFromTop = false
                    }
                }
                return Offset.Zero
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                if (source == NestedScrollSource.UserInput) {
                    if (consumed.y > 0f || checkCanScrollBackward()) {
                        // Child consumed downward scroll (scrolling back up towards top)
                        // Keep pull-to-refresh disabled for this gesture
                        isGestureStartedFromTop = false
                    }
                }
                return Offset.Zero
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                // Gesture finished (finger lifted)
                isGestureStartedFromTop = !checkCanScrollBackward()
                return Velocity.Zero
            }
        }
    }

    Box(
        modifier = modifier
            .pointerInput(checkCanScrollBackward) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Initial)
                        val isDown = event.type == PointerEventType.Press ||
                                event.changes.any { it.pressed && !it.previousPressed }
                        val isUp = event.type == PointerEventType.Release ||
                                event.changes.all { !it.pressed }

                        if (isDown) {
                            // Touch down: record whether content was at top at the moment of touch
                            isGestureStartedFromTop = !checkCanScrollBackward()
                        } else if (isUp) {
                            // Touch lifted: reset according to current scroll position
                            isGestureStartedFromTop = !checkCanScrollBackward()
                        }
                    }
                }
            }
            .nestedScroll(nestedScrollConnection)
            .pullToRefresh(
                state = state,
                isRefreshing = isRefreshing,
                enabled = isPullRefreshEnabled,
                threshold = threshold,
                onRefresh = onRefresh
            ),
        contentAlignment = contentAlignment
    ) {
        content()
        indicator()
    }
}
