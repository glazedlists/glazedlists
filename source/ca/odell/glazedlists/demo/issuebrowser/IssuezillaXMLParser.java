/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.demo.issuebrowser;

import java.util.*;
import java.io.*;
import java.net.URL;
// parse XML using SAX
import javax.xml.parsers.*;

import org.xml.sax.*;
import org.xml.sax.helpers.*;
// glazed lists
import ca.odell.glazedlists.*;
import ca.odell.glazedlists.demo.issuebrowser.Description;
import ca.odell.glazedlists.demo.issuebrowser.Issue;
import ca.odell.glazedlists.swing.*;


/**
 * Parses IssueZilla issue as described by their XML.
 * <p/>
 * <p>Parsing supports DTD revision 1.2 only and may not work with prior or
 * later versions of the issuezilla XML format.
 * <p/>
 * <p>Dates are currently unsupported and will always have a value of "null".
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 * @see <a href="https://glazedlists.dev.java.net/issues/issuezilla.dtd">Issuezilla DTD</a>
 */
public class IssuezillaXMLParser {

	private static SortedSet ISSUE_SIMPLE_FIELDS = new TreeSet();

	static {
		ISSUE_SIMPLE_FIELDS.add("issue_id");
		ISSUE_SIMPLE_FIELDS.add("issue_status");
		ISSUE_SIMPLE_FIELDS.add("priority");
		ISSUE_SIMPLE_FIELDS.add("resolution");
		ISSUE_SIMPLE_FIELDS.add("component");
		ISSUE_SIMPLE_FIELDS.add("version");
		ISSUE_SIMPLE_FIELDS.add("rep_platform");
		ISSUE_SIMPLE_FIELDS.add("assigned_to");
		ISSUE_SIMPLE_FIELDS.add("delta_ts");
		ISSUE_SIMPLE_FIELDS.add("subcomponent");
		ISSUE_SIMPLE_FIELDS.add("reporter");
		ISSUE_SIMPLE_FIELDS.add("target_milestone");
		ISSUE_SIMPLE_FIELDS.add("issue_type");
		ISSUE_SIMPLE_FIELDS.add("creation_ts");
		ISSUE_SIMPLE_FIELDS.add("qa_contact");
		ISSUE_SIMPLE_FIELDS.add("status_whiteboard");
		ISSUE_SIMPLE_FIELDS.add("votes");
		ISSUE_SIMPLE_FIELDS.add("op_sys");
		ISSUE_SIMPLE_FIELDS.add("short_desc");
		// optional
		ISSUE_SIMPLE_FIELDS.add("keywords");
		ISSUE_SIMPLE_FIELDS.add("blocks");
		ISSUE_SIMPLE_FIELDS.add("dependson");
		ISSUE_SIMPLE_FIELDS.add("cc");
	}

	private static SortedSet DESCRIPTION_SIMPLE_FIELDS = new TreeSet();

	static {
		DESCRIPTION_SIMPLE_FIELDS.add("who");
		DESCRIPTION_SIMPLE_FIELDS.add("issue_when");
		DESCRIPTION_SIMPLE_FIELDS.add("thetext");
	}

	private static SortedSet ATTACHMENT_SIMPLE_FIELDS = new TreeSet();

	static {
		ATTACHMENT_SIMPLE_FIELDS.add("mimetype");
		ATTACHMENT_SIMPLE_FIELDS.add("attachid");
		ATTACHMENT_SIMPLE_FIELDS.add("date");
		ATTACHMENT_SIMPLE_FIELDS.add("desc");
		ATTACHMENT_SIMPLE_FIELDS.add("ispatch");
		ATTACHMENT_SIMPLE_FIELDS.add("filename");
		ATTACHMENT_SIMPLE_FIELDS.add("submitter_id");
		ATTACHMENT_SIMPLE_FIELDS.add("submitting_username");
		ATTACHMENT_SIMPLE_FIELDS.add("data");
		ATTACHMENT_SIMPLE_FIELDS.add("attachment_iz_url");
	}

	private static SortedSet ACTIVITY_SIMPLE_FIELDS = new TreeSet();

	static {
		ACTIVITY_SIMPLE_FIELDS.add("user");
		ACTIVITY_SIMPLE_FIELDS.add("when");
		ACTIVITY_SIMPLE_FIELDS.add("field_name");
		ACTIVITY_SIMPLE_FIELDS.add("field_desc");
		ACTIVITY_SIMPLE_FIELDS.add("oldvalue");
		ACTIVITY_SIMPLE_FIELDS.add("newvalue");
	}

	/**
	 * When executed, this opens a file specified on the command line, parses
	 * it for Issuezilla XML and writes the issues to the command line.
	 */
	public static void main(String[] args) throws IOException {
		if (args.length != 1) {
			System.out.println("Usage: IssuezillaXMLParser <url>");
			return;
		}

		BasicEventList issuesList = new BasicEventList();
		loadIssues(issuesList, args[ 0 ]);
		System.out.println(issuesList);
	}

	/**
	 * Loads issues from the specified URL.
	 */
	public static void loadIssues(EventList target, String baseUrl) throws IOException {
		int issuesPerRequest = 100;

		// continuously load issues until there's no more
		while (true) {
			// figure out how many to load
			int currentTotal = target.size();
			int nextTotal = currentTotal + issuesPerRequest;

			// assemble the issue ID argument
			StringBuffer idArg = new StringBuffer();
			for (int i = currentTotal + 1; i <= nextTotal; i++) {
				idArg.append(i);
				if (i < nextTotal) idArg.append(":");
			}

			// prepare a stream
			URL issuesUrl = new URL(baseUrl + "?id=" + idArg);
			InputStream issuesInStream = issuesUrl.openStream();

			// parse
			loadIssues(target, issuesInStream);

			// if we couldn't load everything, we've consumed everything
			if (target.size() < nextTotal) return;
		}
	}

	/**
	 * Parses the Issuezilla XML document on the specified input stream into a List
	 * of issues. While the parsing is taking place this writes some simple Java
	 * commands to reproduce a lightweight version of this list. This is useful
	 * to load the issues as code rather than XML.
	 */
	public static void loadIssues(EventList target, InputStream source) throws IOException {
		try {
			IssueHandler issueReader = new IssueHandler(target);
			SAXParserFactory.newInstance().newSAXParser().parse(source, issueReader);
		} catch(SAXException e) {
			e.printStackTrace();
			throw new IOException("Parsing failed " + e.getMessage());
		} catch(ParserConfigurationException e) {
			e.printStackTrace();
			throw new IOException("Parsing failed " + e.getMessage());
		}
	}

	/**
	 * The IssueHandler does the real parsing.
	 */
	static class IssueHandler extends AbstractSimpleElementHandler {
		private EventList issues = null;
		private Issue currentIssue;
		private AbstractSimpleElementHandler simpleElementHandler = null;

		public IssueHandler(EventList issues) {
			super(null, "issue", ISSUE_SIMPLE_FIELDS);
			this.issues = issues;
			parent = this;
		}

		/**
		 * Gets the list of issues parsed by this handler.
		 */
		public List getIssues() {
			return issues;
		}

		public void startElement(String uri, String localName, String qName,
			Attributes attributes) {
			// ignore issuezilla tags
			if (qName.equals("issuezilla")) {
				// write the necessary Java code
				/*
				System.out.println("List issues = new ArrayList();");
				System.out.println("Issue currentIssue = null;");
				System.out.println("Description description = null;");
				*/
				// ignore
				// create a new issue
			} else if (currentIssue == null) {
				if (qName.equals("issue")) {
					currentIssue = new Issue();

					// save the status code
					String statusCode = attributes.getValue("status_code");
					currentIssue.setStatusCode(statusCode != null ? Integer.valueOf(statusCode) : new Integer(404));

					// write the necessary Java code
					/*System.out.println("currentIssue = new Issue();");*/
				} else {
					addException(this + " encountered unexpected element \"" + qName + "\"");
				}
				// attempt to delegate to a specialized handler
			} else if (simpleElementHandler != null) {
				simpleElementHandler.startElement(uri, localName, qName, attributes);
				// handle a sophisticated description field
			} else if (qName.equals("long_desc")) {
				simpleElementHandler = new DescriptionHandler(this);
				// handle a sophisticated attachment field
			} else if (qName.equals("attachment")) {
				simpleElementHandler = new AttachmentHandler(this);
				// handle a sophisticated activity field
			} else if (qName.equals("activity")) {
				simpleElementHandler = new ActivityHandler(this);
				// handle all other cases in the base class
			} else {
				super.startElement(uri, localName, qName, attributes);
			}
		}

		public void endElement(String uri, String localName, String qName) {
			// if we are within a simple field
			if (currentField != null) {
				super.endElement(uri, localName, qName);
				// if we can delegate to a specialized handler
			} else if (simpleElementHandler != null) {
				simpleElementHandler.endElement(uri, localName, qName);
				// if this is the end of an issue
			} else if (qName.equals("issue")) {
				// break out if this request is cancelled
				if (Thread.interrupted()) throw new RuntimeException(new InterruptedException());

				// add the issue to the list if it was found okay
				if (currentIssue.getStatusCode() != null && currentIssue.getStatusCode().intValue() == 200) {
					issues.add(currentIssue);
				}

				// prepare for the next issue
				currentIssue = null;
				// write the necessary Java code
				/*System.out.println("issues.add(currentIssue);");
				System.out.println("currentIssue = null;");*/
				// ignore issuezilla tags
			} else if (qName.equals("issuezilla")) {
				// write the necessary Java code
				/*System.out.println("return issues;");*/
				// handle all other cases in an error
			} else {
				addException(this + " encountered unexpected end of element \"" + qName + "\"");
			}
		}

		public void characters(char[] ch, int start, int length) {
			// if we can delegate to a specialized handler
			if (simpleElementHandler != null) {
				simpleElementHandler.characters(ch, start, length);
			} else {
				super.characters(ch, start, length);
			}
		}

		public void addFieldAndValue(String currentField, String value) {
			if (currentField.equals("issue_id"))
				currentIssue.setId(Integer.valueOf(value));
			else if (currentField.equals("issue_status"))
				currentIssue.setStatus(value);
			else if (currentField.equals("priority"))
				currentIssue.setPriority(Priority.lookup(value));
			else if (currentField.equals("resolution"))
				currentIssue.setResolution(value);
			else if (currentField.equals("component"))
				currentIssue.setComponent(value);
			else if (currentField.equals("version"))
				currentIssue.setVersion(value);
			else if (currentField.equals("rep_platform"))
				currentIssue.setRepPlatform(value);
			else if (currentField.equals("assigned_to"))
				currentIssue.setAssignedTo(value);
			else if (currentField.equals("delta_ts"))
				currentIssue.setDeltaTimestamp(null);
			else if (currentField.equals("subcomponent"))
				currentIssue.setSubcomponent(value);
			else if (currentField.equals("reporter"))
				currentIssue.setReporter(value);
			else if (currentField.equals("target_milestone"))
				currentIssue.setTargetMilestone(value);
			else if (currentField.equals("issue_type"))
				currentIssue.setIssueType(value);
			else if (currentField.equals("creation_ts"))
				currentIssue.setCreationTimestamp(null);
			else if (currentField.equals("qa_contact"))
				currentIssue.setQAContact(value);
			else if (currentField.equals("status_whiteboard"))
				currentIssue.setStatusWhiteboard(value);
			else if (currentField.equals("votes"))
				currentIssue.setVotes(value);
			else if (currentField.equals("op_sys"))
				currentIssue.setOperatingSystem(value);
			else if (currentField.equals("short_desc"))
				currentIssue.setShortDescription(value);
			else if (currentField.equals("keywords"))
				currentIssue.getKeywords().add(value);
			else if (currentField.equals("blocks"))
				currentIssue.getBlocks().add(value);
			else if (currentField.equals("dependson"))
				currentIssue.getDependsOn().add(value);
			else if (currentField.equals("cc"))
				currentIssue.getCC().add(value);
			else
				parent.addException(this + " encountered unexpected element " + currentField);

			// print the executed Java command
			/*if(currentField.equals("issue_id")) System.out.println("currentIssue.setId(new Integer(" + Integer.valueOf(value) + "));");
			else if(currentField.equals("issue_status")) System.out.println("currentIssue.setStatus(\"" + escapeToJava(value) + "\");");
			else if(currentField.equals("priority")) System.out.println("currentIssue.setPriority(Priority." + value + ");");
			else if(currentField.equals("resolution")) System.out.println("currentIssue.setResolution(\"" + escapeToJava(value) + "\");");
			else if(currentField.equals("component")) System.out.println("currentIssue.setComponent(\"" + escapeToJava(value) + "\");");
			else if(currentField.equals("version")) System.out.println("currentIssue.setVersion(\"" + escapeToJava(value) + "\");");
			else if(currentField.equals("assigned_to")) System.out.println("currentIssue.setAssignedTo(\"" + escapeToJava(value) + "\");");
			else if(currentField.equals("subcomponent")) System.out.println("currentIssue.setSubcomponent(\"" + escapeToJava(value) + "\");");
			else if(currentField.equals("issue_type")) System.out.println("currentIssue.setIssueType(\"" + escapeToJava(value) + "\");");
			else if(currentField.equals("qa_contact")) System.out.println("currentIssue.setQAContact(\"" + escapeToJava(value) + "\");");
			else if(currentField.equals("short_desc")) System.out.println("currentIssue.setShortDescription(\"" + escapeToJava(value) + "\");");*/
		}

		public void endSimpleElement() {
			if (simpleElementHandler.hostElement.equals("long_desc")) {
				DescriptionHandler descriptionHandler = (DescriptionHandler) simpleElementHandler;
				currentIssue.getDescriptions().add(descriptionHandler.description);
				simpleElementHandler = null;
				// write the necessary Java code
				/*System.out.println("currentIssue.getDescriptions().add(description);");
				System.out.println("description = null;");*/
			} else if (simpleElementHandler.hostElement.equals("attachment")) {
				AttachmentHandler attachmentHandler = (AttachmentHandler) simpleElementHandler;
				currentIssue.getAttachments().add(attachmentHandler.attachment);
				simpleElementHandler = null;
			} else if (simpleElementHandler.hostElement.equals("activity")) {
				ActivityHandler activityHandler = (ActivityHandler) simpleElementHandler;
				currentIssue.getActivities().add(activityHandler.activity);
				simpleElementHandler = null;
			} else {
				addException(this + " encountered unexpected end of element " + simpleElementHandler);
			}
		}

		public void addException(String message) {
			new Exception(message).printStackTrace();
		}
	}


	/**
	 * Handles nested description tags.
	 */
	static class DescriptionHandler extends AbstractSimpleElementHandler {
		public Description description;

		public DescriptionHandler(IssueHandler parent) {
			super(parent, "long_desc", DESCRIPTION_SIMPLE_FIELDS);
			description = new Description();
			// print the executed Java command
			/*System.out.println("description = new Description();");*/
		}

		public void addFieldAndValue(String currentField, String value) {
			if (currentField.equals("who"))
				description.setWho(value);
			else if (currentField.equals("issue_when"))
				description.setWhen(null);
			else if (currentField.equals("thetext"))
				description.setText(value);
			else
				parent.addException(this + " encountered unexpected element " + currentField);

			// print the executed Java command
			/*if(currentField.equals("who")) System.out.println("description.setWho(\"" + escapeToJava(value) + "\");");
			else if(currentField.equals("issue_when")) System.out.println("description.setWhen(null);");
			else if(currentField.equals("thetext")) System.out.println("description.setText(\"" + escapeToJava(value) + "\");");*/
		}
	}


	/**
	 * Handles nested attachment tags.
	 */
	static class AttachmentHandler extends AbstractSimpleElementHandler {
		public Attachment attachment;

		public AttachmentHandler(IssueHandler parent) {
			super(parent, "attachment", ATTACHMENT_SIMPLE_FIELDS);
			attachment = new Attachment();
		}

		public void addFieldAndValue(String currentField, String value) {
			if (currentField.equals("mimetype"))
				attachment.setMimeType(value);
			else if (currentField.equals("attachid"))
				attachment.setAttachId(value);
			else if (currentField.equals("date"))
				attachment.setDate(null);
			else if (currentField.equals("desc"))
				attachment.setDescription(value);
			else if (currentField.equals("ispatch"))
				attachment.setIsPatch(value);
			else if (currentField.equals("filename"))
				attachment.setFilename(value);
			else if (currentField.equals("submitter_id"))
				attachment.setSubmitterId(value);
			else if (currentField.equals("submitting_username"))
				attachment.setSubmitterUsername(value);
			else if (currentField.equals("data"))
				attachment.setData(value);
			else if (currentField.equals("attachment_iz_url"))
				attachment.setAttachmentIzUrl(value);
			else
				parent.addException(this + " encountered unexpected element " + currentField);
		}
	}


	/**
	 * Handles nested activity tags.
	 */
	static class ActivityHandler extends AbstractSimpleElementHandler {
		public Activity activity;

		public ActivityHandler(IssueHandler parent) {
			super(parent, "activity", ACTIVITY_SIMPLE_FIELDS);
			activity = new Activity();
		}

		public void addFieldAndValue(String currentField, String value) {
			if (currentField.equals("user"))
				activity.setUser(value);
			else if (currentField.equals("when"))
				activity.setWhen(null);
			else if (currentField.equals("field_name"))
				activity.setField(value);
			else if (currentField.equals("field_desc"))
				activity.setFieldDescription(value);
			else if (currentField.equals("oldvalue"))
				activity.setOldValue(value);
			else if (currentField.equals("newvalue"))
				activity.setNewValue(value);
			else
				parent.addException(this + " encountered unexpected element " + currentField);
		}
	}


	/**
	 * Convert the specified literal String to a String that the Java compiler
	 * can parse.
	 */
	private static String escapeToJava(String text) {
		StringBuffer result = new StringBuffer();
		for (int c = 0; c < text.length(); c++) {
			char letter = text.charAt(c);
			if (letter == '\"')
				result.append("\\\"");
			else if (letter == '\\')
				result.append("\\\\");
			else if (letter == '\n')
				result.append("\\n");
			else
				result.append(letter);
		}
		return result.toString();
	}


	/**
	 * A simple class for parsing issuezilla element blocks that contain no
	 * smaller blocks within.
	 */
	static abstract class AbstractSimpleElementHandler extends DefaultHandler {
		protected String currentField = null;
		protected IssueHandler parent;
		protected String hostElement = null;
		private Set acceptableFields = null;
		private StringBuffer currentValue;

		protected AbstractSimpleElementHandler(IssueHandler parent, String hostElement,
			Set acceptableFields) {
			this.parent = parent;
			this.hostElement = hostElement;
			this.acceptableFields = acceptableFields;
		}

		public void startElement(String uri, String localName, String qName,
			Attributes attributes) {
			if (currentField != null) {
				parent.addException(this + " expected end of element " + currentField + " but found " + qName);
			} else if (acceptableFields.contains(qName)) {
				currentField = qName;
				currentValue = new StringBuffer();
			} else {
				parent.addException(this + " encountered unexpected element " + qName);
			}
		}

		public void endElement(String uri, String localName, String qName) {
			if (qName.equals(hostElement)) {
				parent.endSimpleElement();
			} else if (currentField == null) {
				parent.addException(this + " expected new element but found end of " + qName);
			} else if (currentField.equals(qName)) {
				addFieldAndValue(currentField, currentValue.toString());
				currentField = null;
				currentValue = null;
			} else {
				parent.addException(this + " expected end of element " + currentField + " but found end of " + qName);
			}
		}

		public void characters(char[] ch, int start, int length) {
			if (currentField == null) return;
			currentValue.append(ch, start, length);
		}

		protected abstract void addFieldAndValue(String currentField, String value);

		public String toString() {
			return "<" + hostElement + ">";
		}
	}
}
