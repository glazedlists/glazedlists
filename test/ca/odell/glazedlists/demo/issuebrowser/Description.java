package ca.odell.glazedlists.demo.issuebrowser;

// GlazedLists

import ca.odell.glazedlists.*;
// For dates and lists
import java.util.*;


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
	public void getFilterStrings(List baseList) {
		baseList.add(text);
		baseList.add(who);
	}
}