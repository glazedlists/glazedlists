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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.net.URL;
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
	 * Color of help text
	 */
	private static final String HELP_TEXT_COLOR = "#ff7700";

	/**
	 * Welcome text
	 */
	private static final String WELCOME_TEXT =
		"<html>Welcome to the Glazed Lists demo launcher!<br><br>" +
		"These demos will help show off some of Glazed Lists' features " +
		"and serve as a guide when creating your own applications.<br><br>" +
		"Choose a demo from the list below to get started:</html>";


	private static final Class STRING_ARRAY_CLASS = new String[ 0 ].getClass();


	private JList demo_list;
	private JButton launch_button;
	private JEditorPane help_text_viewer;


	public static void main(String[] args) {
		// Set this system property so that anything we launch knows not to System.exit().
		System.setProperty("in_launcher", "true");

		new Launcher();
	}


	private Launcher() {
		URL duke_of = Launcher.class.getResource(LOGO_FILE);
		JLabel logo_label = null;
		if (duke_of != null) {
			logo_label = new JLabel(new ImageIcon(duke_of));
		}

		// Left panel containing logo and filler
		JPanel left_panel = new JPanel(new BorderLayout(0, 0));
		left_panel.setOpaque(true);
		left_panel.setBackground(BACKGROUND_COLOR);
		left_panel.setBorder(new EmptyBorder(5, 5, 0, 0));
		if (logo_label != null) {
			left_panel.add(logo_label, BorderLayout.PAGE_START);
		}

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

		frame.setSize(new Dimension(550, 300));
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
		help_text_viewer.setOpaque(false);
		JScrollPane help_scroller = new JScrollPane(help_text_viewer,
			JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
			JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		help_scroller.setPreferredSize(new Dimension( 150, dim.height));
		help_scroller.setBorder( null );
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

	public void valueChanged(ListSelectionEvent e) {
		Object selected_value = demo_list.getSelectedValue();

		launch_button.setEnabled(selected_value != null);

		if (selected_value == null)
			help_text_viewer.setText("");
		else {
			help_text_viewer.setText("<html><font color=\"" + HELP_TEXT_COLOR + "\">" +
				((Demo) selected_value).help_text + "</font></html>");
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
