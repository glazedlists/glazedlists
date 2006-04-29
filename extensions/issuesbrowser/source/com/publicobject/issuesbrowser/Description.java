/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package com.publicobject.issuesbrowser;

// GlazedLists
import ca.odell.glazedlists.TextFilterable;

import java.util.Date;
import java.util.List;

/**
 * Models the description of a particular Issue.
 *
 * @author <a href="jesse@odel.on.ca">Jesse Wilson</a>
 */
public class Description implements TextFilterable {
	private String who = null;
	private Date when = null;
	private String text = null;

	/**
	 * Email of person posting long_desc.
	 */
	public String getWho() {
		return who;
	}

	public void setWho(String who) {
		this.who = who;
	}

	/**
	 * Timestamp when long_desc added ('yyy-mm-dd hh:mm')
	 */
	public Date getWhen() {
		return when;
	}

	public void setWhen(Date when) {
		this.when = when;
	}

	/**
	 * Free text that comprises the long desc.
	 */
	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	/**
	 * Gets the strings to filter this issue by.
	 */
	public void getFilterStrings(List<String> baseList) {
		baseList.add(text);
		baseList.add(who);
	}
}