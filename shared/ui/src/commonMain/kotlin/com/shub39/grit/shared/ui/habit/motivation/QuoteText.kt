/*
 * Copyright (C) 2026  Shubham Gorai
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.shub39.grit.shared.ui.habit.motivation

import com.shub39.grit.core.motivation.MotivationQuote
import com.shub39.grit.core.motivation.QuoteCatalog
import grit.shared.ui.generated.resources.Res
import grit.shared.ui.generated.resources.allStringResources
import org.jetbrains.compose.resources.StringResource

/**
 * Text of this quote. The quote id is the string resource name; unknown ids resolve to the fallback
 * quote. Format with the active days as first argument.
 */
fun MotivationQuote.textResource(): StringResource =
    Res.allStringResources[id] ?: Res.allStringResources.getValue(QuoteCatalog.fallback.id)
