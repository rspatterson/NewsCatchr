/*
 * NewsCatchr
 * Copyright © 2017 Jan-Lukas Else
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

@file:Suppress("EXPERIMENTAL_FEATURE_WARNING")

package jlelse.newscatchr.backend

import android.app.Activity
import co.metalab.asyncawait.async
import com.afollestad.ason.AsonName
import com.afollestad.bridge.annotations.ContentType
import jlelse.newscatchr.backend.apis.share
import jlelse.newscatchr.backend.apis.shortUrl
import jlelse.newscatchr.backend.helpers.Preferences
import jlelse.newscatchr.database
import jlelse.newscatchr.extensions.*

@ContentType("application/json")
data class Article(
		var id: String? = null,
		var published: Long = 0,
		var author: String? = null,
		var title: String? = null,
		@AsonName(name = "cannonical.$0.href")
		var canonicalHref: String? = null,
		@AsonName(name = "alternate.$0.href")
		var alternateHref: String? = null,
		@AsonName(name = "enclosure.$0.href")
		var enclosureHref: String? = null,
		var keywords: Array<String>? = null,
		@AsonName(name = "visual.url")
		var visualUrl: String? = null,
		@AsonName(name = "origin.title")
		var originTitle: String? = null,
		@AsonName(name = "summary.content")
		var summaryContent: String? = null,
		@AsonName(name = "content.content")
		var content: String? = null,
		var excerpt: String? = null,
		var cdnAmpUrl: String? = null,
		var ampUrl: String? = null,
		var url: String? = null
)

fun Article.process(): Article {
	content = content()
	excerpt = excerpt()
	url = url()
	visualUrl = image()
	return this
}

fun Article.content() = (summaryContent.blankNull() ?: content)?.cleanHtml()
fun Article.excerpt() = content?.toHtml().toString().buildExcerpt(30)
fun Article.url() = canonicalHref.blankNull() ?: alternateHref.blankNull() ?: url
fun Article.image() = enclosureHref.blankNull() ?: visualUrl

fun Article.share(context: Activity) {
	async {
		val newUrl = await {
			if (Preferences.urlShortener) tryOrNull { url?.shortUrl() } ?: url else url
		}
		context.share("\"$title\"", "$title - $newUrl")
	}
}

fun Article?.isBookmark(): Boolean = database.isBookmark(this?.url)