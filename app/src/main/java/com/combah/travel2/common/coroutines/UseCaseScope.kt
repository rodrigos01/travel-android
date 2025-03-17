package com.combah.travel2.common.coroutines

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

val UseCaseScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
