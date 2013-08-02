package org.ancode.secmail.activity;

import org.ancode.secmail.R;
import org.ancode.secmail.mail.Flag;

/**
 * This enum represents filtering parameters used by
 * {@link org.ancode.secmail.SearchAccount}.
 */
enum SearchModifier {
	FLAGGED(R.string.flagged_modifier, new Flag[] { Flag.FLAGGED }, null), UNREAD(R.string.unread_modifier, null,
			new Flag[] { Flag.SEEN });

	final int resId;
	final Flag[] requiredFlags;
	final Flag[] forbiddenFlags;

	SearchModifier(int nResId, Flag[] nRequiredFlags, Flag[] nForbiddenFlags) {
		resId = nResId;
		requiredFlags = nRequiredFlags;
		forbiddenFlags = nForbiddenFlags;
	}

}