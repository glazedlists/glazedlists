/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003-2005 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.demo;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.swing.EventListModel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.Properties;


/**
 * Main application that will allow launching individual demos.
 *
 * @author <a href="mailto:rob@starlight-systems.com>Rob Eden</a>
 */
public class Launcher implements ActionListener, ListSelectionListener {
	/**
	 * Name of the logo image file.
	 */
	private static final String LOGO_FILE = "logoonbrown.gif";

	/**
	 * Background color
	 */
	private static final Color BACKGROUND_COLOR = new Color(102, 51, 0);

	/**
	 * Foreground color for highlights
	 */
	private static final Color FOREGROUND_COLOR = new Color(255, 204, 0);

	/**
	 * Background of section headers
	 */
	private static final Color SECTION_HEADER_BACKGROUND = new Color(255, 119, 0);
	/**
	 * Foreground of section headers
	 */
	private static final Color SECTION_HEADER_FOREGROUND = BACKGROUND_COLOR;

	/**
	 * Background of link bar labels
	 */
	private static final Color LINK_BAR_BACKGROUND = BACKGROUND_COLOR;
	/**
	 * Background of link bar labels when the mouse is over them
	 */
	private static final Color LINK_BAR_BACKGROUND_ROLLOVER = new Color(119, 72, 0);
	/**
	 * Foreground of link bar labels
	 */
	private static final Color LINK_BAR_FOREGROUND = new Color(255, 153, 0);

	/**
	 * Color of help text
	 */
//	private static final String HELP_TEXT_COLOR = "#ff7700";
	private static final String HELP_TEXT_COLOR = "#663300";

	/**
	 * Welcome text
	 */
	private static final String WELCOME_TEXT =
		"<html>Welcome to the Glazed Lists demo launcher!<br><br>" +
		"These demos will help show off some of Glazed Lists' features " +
		"and serve as a guide when creating your own applications.<br><br>" +
		"Choose a demo from the list below to get started:</html>";


	private static final Class STRING_ARRAY_CLASS = new String[ 0 ].getClass();

	private static boolean in_launcher = false;


	private JList demo_list;
	private JButton launch_button;
	private JEditorPane help_text_viewer;


	public static void main(String[] args) {
		// Set this system property so that anything we launch knows not to System.exit().
//		System.setProperty("in_launcher", "true");
		in_launcher = true;

		// Use the system look and feel
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch(Exception ex) {
		}

		new Launcher();
	}


	/**
	 * Shoudl be used by other demo applications to tell when they're being run from the
	 * Launcher. This is useful to know when those demos should exit the VM when closed
	 * (when this returns false) and when they should do nothing.
	 */
	public static boolean runningInLauncher() {
		return in_launcher;
	}


	private Launcher() {
		URL duke_of = Launcher.class.getResource(LOGO_FILE);
		JLabel logo_label = null;
		if (duke_of != null) {
			logo_label = new JLabel(new ImageIcon(duke_of));
			logo_label.setBackground(BACKGROUND_COLOR);
			logo_label.setOpaque(true);
		}

		JPanel top_left = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints(0, 0, 1, 1, 100.0, 1,
			GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0),
			0, 0);
		if (logo_label != null) {
			top_left.add(logo_label, gbc);
			gbc.gridy++;

			addWebStartLinks(top_left, gbc);
		}

		// Left panel containing logo and jump labels
		JPanel left_panel = new JPanel(new BorderLayout(0, 0));
		left_panel.setOpaque(true);
		left_panel.setBackground(BACKGROUND_COLOR);
		left_panel.setBorder(new EmptyBorder(5, 0, 0, 0));
		left_panel.add(top_left, BorderLayout.PAGE_START);

		// Header
		JLabel header = new JLabel("Glazed Lists");
		header.setOpaque(true);
		header.setForeground(FOREGROUND_COLOR);
		header.setBackground(BACKGROUND_COLOR);
		header.setBorder(new EmptyBorder(5, 0, 5, 0));
		Font font = header.getFont();
		font = new Font(font.getName(), Font.BOLD, font.getSize() + 10);
		header.setFont(font);

		// Main panel containing header and content
		JPanel main_panel = new JPanel(new BorderLayout());
		main_panel.setOpaque(false);
		main_panel.add(header, BorderLayout.PAGE_START);
		JPanel content_panel = buildContentPanel();
		main_panel.add(content_panel, BorderLayout.CENTER);

		// Outer panel containing left and main panels
		JPanel outer_panel = new JPanel(new BorderLayout());
		outer_panel.add(left_panel, BorderLayout.LINE_START);
		outer_panel.add(main_panel, BorderLayout.CENTER);
		outer_panel.setBackground(Color.WHITE);

		JFrame frame = new JFrame("Glazed Lists");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		frame.setContentPane(outer_panel);

		frame.setSize(new Dimension(550, 350));
		centerWindow(frame);
		frame.setVisible(true);
	}


	private JPanel buildContentPanel() {
		JLabel welcome_label = new JLabel(WELCOME_TEXT);

		// Ok, this is overkill. But this is what we do!
		// Had to use an EventList in here somewhere...
		BasicEventList root_list = new BasicEventList(loadDemos());
		SortedList sorter = new SortedList(root_list);
		demo_list = new JList(new EventListModel(sorter));
		demo_list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		demo_list.getSelectionModel().addListSelectionListener(this);

		JScrollPane scroller = new JScrollPane(demo_list,
			JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
			JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		Dimension dim = demo_list.getPreferredScrollableViewportSize();
		dim.width += 10;
		scroller.setPreferredSize(dim);

		help_text_viewer = new JEditorPane();
		help_text_viewer.setEditable(false);
		help_text_viewer.setBorder(null);
		help_text_viewer.setContentType("text/html");
		help_text_viewer.setOpaque(true);
		help_text_viewer.setBackground(Color.WHITE);
		JScrollPane help_scroller = new JScrollPane(help_text_viewer,
			JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
			JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		help_scroller.setPreferredSize(new Dimension(150, dim.height));
		help_scroller.setBorder(null);
		help_scroller.getViewport().setOpaque(false);

		launch_button = new JButton("Launch...");
		launch_button.setOpaque(false);
		launch_button.addActionListener(this);
		launch_button.setEnabled(false);
		JPanel help_launch_panel = new JPanel(new BorderLayout());
		help_launch_panel.setOpaque(false);
		help_launch_panel.setBorder(new EmptyBorder(0, 7, 0, 0));
		help_launch_panel.add(help_scroller, BorderLayout.CENTER);
		JPanel filler = new JPanel(new FlowLayout(FlowLayout.CENTER));
		filler.setOpaque(false);
		filler.add(launch_button);
		help_launch_panel.add(filler, BorderLayout.PAGE_END);

		JPanel inner_panel = new JPanel(new BorderLayout());
		inner_panel.setOpaque(false);
		inner_panel.add(scroller, BorderLayout.LINE_START);
		inner_panel.add(help_launch_panel, BorderLayout.CENTER);

		JPanel panel = new JPanel(new BorderLayout(0, 10));
		panel.setOpaque(false);
		panel.setBorder(new EmptyBorder(5, 5, 5, 5));
		panel.add(welcome_label, BorderLayout.PAGE_START);
		panel.add(inner_panel, BorderLayout.CENTER);

		return panel;
	}


	/**
	 * If we're running within WebStart, we'll add links to the web site and documentation.
	 *
	 * @param box The box to add buttons to.
	 */
	private void addWebStartLinks(JPanel box, GridBagConstraints gbc) {
		// Note: We're going to do a bunch of relection here to avoid requiring the JNLP
		// JAR to build the launcher.
		Object basic_service;
		Method show_document_method;
		try {
			// See if we're running in webstart
			Class service_manager_class = Class.forName( "javax.jnlp.ServiceManager" );
			Method service_names_method =
				service_manager_class.getMethod( "getServiceNames", new Class[]{} );
			String[] service_names = ( String[] ) service_names_method.invoke(
				service_manager_class, new Object[] {});

			// If null or empty, not running in WebStart
			if ( service_names == null || service_names.length == 0 ) return;


			// Lookup the BasicService (needed to launch URLs)
			Class basic_service_class = Class.forName( "javax.jnlp.BasicService" );

			Method lookup_method =
				service_manager_class.getMethod("lookup",new Class[] {String.class});

			basic_service = lookup_method.invoke(service_manager_class,
				new Object[] {basic_service_class.getName()});

			// See if web browsers are supported
			Method web_browser_supported_method =
				basic_service_class.getMethod("isWebBrowserSupported", new Class[]{});
			Boolean bool = (Boolean) web_browser_supported_method.invoke(basic_service,
				new Object[] {});
			if ( bool == null || !bool.booleanValue()) return;

			show_document_method = basic_service_class.getMethod("showDocument",
				new Class[]{URL.class} );
		} catch(Exception ex) {
			return;
		}

		box.add(createHeaderLabel("Links"), gbc);
		gbc.gridy++;
		try {
			box.add(createLinkBarLabel("Main Website",
				new URL("http://publicobject.com/glazedlists/"), basic_service,
				show_document_method), gbc);
			gbc.gridy++;
		} catch(MalformedURLException ex) {
		}

		try {
			box.add(createLinkBarLabel("Java.net",
				new URL("http://glazedlists.dev.java.net/"), basic_service,
				show_document_method), gbc);
			gbc.gridy++;
		} catch(MalformedURLException ex) {
		}

		JPanel filler = new JPanel();
		filler.setBackground(BACKGROUND_COLOR);
		filler.setPreferredSize(new Dimension(1, 5));
		filler.setMinimumSize(new Dimension(1, 5));
		filler.setMaximumSize(new Dimension(10000, 5));
		box.add(filler, gbc);
		gbc.gridy++;

		box.add(createHeaderLabel("Documentation"), gbc);
		gbc.insets = new Insets(0, 0, 0, 0);
		gbc.gridy++;
		try {
			box.add(createLinkBarLabel("Tutorial",
				new URL("http://publicobject.com/glazedlists/tutorial/"),
				basic_service, show_document_method), gbc);
			gbc.gridy++;
		} catch(MalformedURLException ex) {
		}

		try {
			box.add(createLinkBarLabel("Javadoc API",
				new URL("http://publicobject.com/glazedlists/api/"),
				basic_service, show_document_method), gbc);
			gbc.gridy++;
		} catch(MalformedURLException ex) {
		}

		try {
			box.add(createLinkBarLabel("FAQ",
				new URL("http://publicobject.com/glazedlists/faq.html"), basic_service,
				show_document_method), gbc);
			gbc.gridy++;
		} catch(MalformedURLException ex) {
		}
	}

	public void valueChanged(ListSelectionEvent e) {
		Object selected_value = demo_list.getSelectedValue();

		launch_button.setEnabled(selected_value != null);

		if (selected_value == null)
			help_text_viewer.setText("");
		else {
			help_text_viewer.setText("<html><body bgcolor=\"#ffffff\"><font color=\"" +
				HELP_TEXT_COLOR + "\">" + ((Demo) selected_value).help_text +
				"</font></body></html>");
		}
	}


	public void actionPerformed(ActionEvent e) {
		Demo demo = (Demo) demo_list.getSelectedValue();
		if (demo == null) return;

		try {
			Class clazz = Class.forName(demo.class_name);
			Method method = clazz.getMethod("main", new Class[]{STRING_ARRAY_CLASS});

			method.invoke(clazz, new Object[]{new String[ 0 ]});
		} catch(Exception ex) {
			handleError(ex, "attempting to run the demo");
		}
	}


	/**
	 * Load the available demos from a properties file.
	 */
	private java.util.List loadDemos() {
		Properties props = new Properties();
		InputStream in = null;
		try {
			in = Launcher.class.getResourceAsStream("demos.properties");

			props.load(in);
		} catch(Exception ex) {
			handleError(ex, "loading data about the demos");
			System.exit(-1);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch(IOException ex) {
				}
			}
		}

		LinkedList demo_list = new LinkedList();

		Enumeration enum = props.propertyNames();
		while (enum.hasMoreElements()) {
			String name = (String) enum.nextElement();

			// Skip anything that's not a "main" element
			if (name.endsWith(".class") || name.endsWith(".desc")) continue;

			String demo_name = props.getProperty(name, null);
			String demo_class = props.getProperty(name + ".class", null);
			String demo_help = props.getProperty(name + ".desc", "No help text available");

			// Error checking
			if (isEmpty(demo_name)) {
				System.err.println("Empty name for property \"" + name + "\". Check format");
				continue;
			}
			if (isEmpty(demo_class)) {
				System.err.println("Empty class name for property \"" + name +
					"\". Check format");
				continue;
			}

			demo_list.add(new Demo(demo_name, demo_help, demo_class));
		}

		return demo_list;
	}


	/**
	 * See if a string is empty.
	 */
	private boolean isEmpty(String str) {
		if (str == null) return true;
		if (str.trim().length() == 0) return true;

		return false;
	}


	/**
	 * Displays an error dialog and exception information.
	 *
	 * @param ex       The exception that occurred.
	 * @param location A descriptive string of what ws happening when the error occurred.
	 *                 Will be displayed at the end of a sentance so it should be all
	 *                 lower-case. Do not include any puctuation.
	 */
	private void handleError(Exception ex, String location) {
		StringWriter writer = new StringWriter();
		ex.printStackTrace(new PrintWriter(writer));

		StringBuffer buf = new StringBuffer("Sorry, an error occurred ");
		buf.append(location).append(".\n");
		buf.append("Please report this error to \"issues@glazedlists.dev.java.net\"");
		buf.append("\n\n");
		buf.append("Exception:\n");
		buf.append(writer.getBuffer());

		JOptionPane.showMessageDialog(null, buf, "Error", JOptionPane.ERROR_MESSAGE);
	}


	/**
	 * Create a label that looks like a section header on the Glazed Lists website.
	 */
	private JLabel createHeaderLabel(String text) {
		JLabel label = new JLabel(text);
		label.setOpaque(true);
		label.setBackground(SECTION_HEADER_BACKGROUND);
		label.setForeground(SECTION_HEADER_FOREGROUND);
		label.setBorder(new EmptyBorder(3, 3, 3, 3));

		Font font = label.getFont();
		label.setFont(new Font(font.getName(), Font.PLAIN, font.getSize() + 5));

		return label;
	}

	/**
	 * Create a label that looks like a link bar button on the Glazed Lists website.
	 */
	private JLabel createLinkBarLabel(String text, final URL url,
		final Object basic_service, final Method show_document_method ) {

		final JLabel label = new JLabel(text);
		label.setOpaque(true);
		label.setBackground(LINK_BAR_BACKGROUND);
		label.setForeground(LINK_BAR_FOREGROUND);
		label.setBorder(new EmptyBorder(0, 3, 0, 0));

		Font font = label.getFont();
		label.setFont(new Font(font.getName(), Font.PLAIN, font.getSize() - 2));

		label.addMouseListener(new MouseAdapter() {
			public void mouseEntered(MouseEvent e) {
				label.setBackground(LINK_BAR_BACKGROUND_ROLLOVER);
			}

			public void mouseExited(MouseEvent e) {
				label.setBackground(LINK_BAR_BACKGROUND);
			}

			public void mouseClicked(MouseEvent e) {
				try {
					show_document_method.invoke( basic_service, new Object[] { url } );
				}
				catch( Exception ex ) {}
			}
		});

		return label;
	}


	private void centerWindow(Window window) {
		Point center = GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint();
		Dimension win_size = window.getSize();
		Point window_point = new Point(center.x - (win_size.width / 2),
			center.y - (win_size.height / 2));
		window.setLocation(window_point);
	}


	/**
	 * Container class with demo information
	 */
	private class Demo implements Comparable {
		String name;
		String help_text;
		String class_name;

		Demo(String name, String help_text, String class_name) {
			this.name = name;
			this.help_text = help_text;
			this.class_name = class_name;
		}


		public String toString() {
			return name;
		}


		public int compareTo(Object o) {
			return name.compareTo(((Demo) o).name);
		}
	}
}
