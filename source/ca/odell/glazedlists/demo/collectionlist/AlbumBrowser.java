/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003-2005 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.demo.collectionlist;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.CollectionList;
import ca.odell.glazedlists.CollectionListModel;
import ca.odell.glazedlists.swing.EventListModel;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.AbstractList;
import java.util.LinkedList;
import java.util.List;


/**
 * A simple application that demonstrates the usage of {@link CollectionList}.
 *
 * @author <a href="mailto:rob@starlight-systems.com">Rob Eden</a>
 */
public class AlbumBrowser {
	public static void main(String[] args) throws Exception {
		File song_file = null;
		if (args.length > 0) {
			String arg0 = args[ 0 ];

			song_file = new File(arg0);
			if (!song_file.exists()) {
				System.out.println("Usage: CollectionListTest <song_list>");
				System.out.println("<song_list> must be a tab-delimited file with song " +
					"in the first column and album in the second. The first row " +
					"is considered to be a header and will be skipped.");
				System.err.println("  File not found: " + song_file);
				return;
			}
		}

		final BasicEventList parent_event_list = new BasicEventList();

		long start = System.currentTimeMillis();
		importSongList(parent_event_list, song_file);
		System.out.println("Time to import song list: " +
			(System.currentTimeMillis() - start));

		final JList record_list = new JList(new EventListModel(parent_event_list));
		record_list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane record_list_scroller = new JScrollPane(record_list);
		record_list_scroller.setBorder(new TitledBorder("Records"));

		start = System.currentTimeMillis();
		final CollectionList song_event_list = new CollectionList(parent_event_list,
			new RecordCollectionListModel());
		System.out.println("Time to create CollectionList: " +
			(System.currentTimeMillis() - start));

		// Uncomment this if you'd like to run a speed test
//		start = System.currentTimeMillis();
//		for( int test_num = 0; test_num < 5; test_num++ ) {
//			for( int i = 0; i < song_event_list.size(); i++ ) {
//				song_event_list.get( i );
//			}
//		}
//		long time = System.currentTimeMillis() - start;
//		System.out.println( "Time to get all nodes (" +
//			song_event_list.size() + ") in list 5 times: " + time );

		final JList song_list = new JList(new EventListModel(song_event_list));
		JScrollPane song_list_scroller = new JScrollPane(song_list);
		song_list_scroller.setBorder(new TitledBorder("Songs"));

		JPanel panel = new JPanel(new GridLayout(1, 2));
		panel.add(record_list_scroller);
		panel.add(song_list_scroller);

		JPanel panel2 = new JPanel(new BorderLayout());
		panel2.add(panel, BorderLayout.CENTER);

		JPanel button_panel = new JPanel(new FlowLayout(FlowLayout.LEADING));
		panel2.add(button_panel, BorderLayout.NORTH);

		JButton add = new JButton("Add Front");
		add.setActionCommand("FRONT");
		button_panel.add(add);
		JButton add_end = new JButton("Add End");
		add_end.setActionCommand("END");
		button_panel.add(add_end);

		ActionListener add_listener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Record rec = new Record("New Record " + System.currentTimeMillis(),
					new String[]{"One", "Two", "Three"});

				long start = System.currentTimeMillis();
				if (e.getActionCommand().equals("FRONT")) {
					parent_event_list.add(0, rec);
				} else {
					parent_event_list.add(rec);
				}
				System.out.println("Time to add album: " +
					(System.currentTimeMillis() - start));
			}
		};
		add.addActionListener(add_listener);
		add_end.addActionListener(add_listener);


		JButton remove_front = new JButton("Remove Front");
		remove_front.setActionCommand("FRONT");
		button_panel.add(remove_front);
		JButton remove_end = new JButton("Remove End");
		remove_end.setActionCommand("END");
		button_panel.add(remove_end);

		ActionListener remove_listener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (parent_event_list.size() <= 0) return;

				long start = System.currentTimeMillis();
				if (e.getActionCommand().equals("FRONT")) {
					parent_event_list.remove(0);
				} else {
					parent_event_list.remove(parent_event_list.size() - 1);
				}
				System.out.println("Time to remove album: " +
					(System.currentTimeMillis() - start));
			}
		};
		remove_front.addActionListener(remove_listener);
		remove_end.addActionListener(remove_listener);


		JButton add_song = new JButton("Add Song");
		add_song.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Record r = (Record) parent_event_list.get(0);
				r.addSong("Song " + System.currentTimeMillis());
				long start = System.currentTimeMillis();
				parent_event_list.set(0, r);
				System.out.println("Time to add song: " +
					(System.currentTimeMillis() - start));
			}
		});
		button_panel.add(add_song);

		JButton remove_song = new JButton("Remove Song");
		remove_song.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Record r = (Record) parent_event_list.get(0);
				r.removeSong();
				long start = System.currentTimeMillis();
				parent_event_list.set(0, r);
				System.out.println("Time to remove song: " +
					(System.currentTimeMillis() - start));
			}
		});
		button_panel.add(remove_song);

		// Select the songs when an album is selected
		record_list.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting()) return;

				int parent_index = record_list.getSelectedIndex();

				int start = song_event_list.childStartingIndex(parent_index);
				int end = song_event_list.childEndingIndex(parent_index);

//				System.out.println( parent_index + ": Start: " + start + "  End: " + end );

				song_list.setSelectionInterval(start, end);
				song_list.ensureIndexIsVisible(end);
				song_list.ensureIndexIsVisible(start);
			}
		});

		JFrame frame = new JFrame("Test");

		// Make sure we're not running in the launcher
		if (System.getProperty("in_launcher") == null) {
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		} else {
			frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		}

		frame.setContentPane(panel2);
		frame.pack();
		frame.setVisible(true);
	}


	private static void importSongList(BasicEventList event_list, File song_file)
		throws IOException {

		Reader in = null;
		BufferedReader br = null;
		try {
			// Load a user-specified file
			if (song_file != null) {
				in = new FileReader(song_file);
			}
			// Load the default file
			else {
				try {
					InputStream is = AlbumBrowser.class.getResourceAsStream("Library.txt");
					if (is != null) {
						in = new InputStreamReader(is);
					} else {
						in = new FileReader("Library.txt");	// um... try the current dir?
					}
				} catch(IOException ex) {
					System.err.println("Unable to load the default library file. " +
						"If the build problem can't be found, try manually specifying " +
						"a file.");
					throw ex;
				}
			}

			br = new BufferedReader(in);

			String last_album = null;
			LinkedList song_list = new LinkedList();

			System.out.print("Loading albums: ");
			int record_count = 0;
			String line;
			boolean first_print = true;
			for (int i = 0; (line = br.readLine()) != null; i++) {
				if (i == 0) continue;				// skip the first line (header)
				String[] toks = line.split("\t");

				if (toks == null || toks.length < 2) continue;

				String song = toks[ 0 ];
				String album = toks[ 1 ];

				// If we're on a different artist, create the record and add it to the list
				if (last_album != null && !last_album.equals(album)) {
					Record record = new Record(last_album,
						(String[]) song_list.toArray(new String[ song_list.size() ]));

					event_list.add(record);
					record_count++;
					song_list.clear();
				}

				last_album = album;

				if (isEmpty(song) || isEmpty(album)) continue;

				song_list.add(song);

				if ((i % 100) == 0) {
					if (!first_print) System.out.print("...");
					first_print = false;
					System.out.print(i);
				}
			}
			System.out.println("done: " + record_count);
		} finally {
			if (br != null) br.close();
			if (in != null) in.close();
		}
	}

	private static boolean isEmpty(String str) {
		if (str == null) return true;

		str = str.trim();

		return str.length() == 0;
	}


	static class Record extends AbstractList {
		String name;
		String[] songs;

		Record(String name, String[] songs) {
			this.name = name;
			this.songs = songs;
		}


		public String toString() {
			return name;
		}


		public int getSongCount() {
			return songs.length;
		}

		public String getSong(int index) {
			return songs[ index ];
		}

		public void addSong(String song) {
			String[] tmp = new String[ songs.length + 1 ];
			System.arraycopy(songs, 0, tmp, 0, songs.length);
			tmp[ songs.length ] = song;
			songs = tmp;
		}

		public void removeSong() {
			if (songs.length <= 0) return;

			String tmp[] = new String[ songs.length - 1 ];
			System.arraycopy(songs, 0, tmp, 0, tmp.length);
			songs = tmp;
		}

		public Object get(int index) {
			return getSong(index);
		}

		public int size() {
			return getSongCount();
		}
	}


	static class RecordCollectionListModel implements CollectionListModel {
		public List getChildren(Object parent) {
			return ((Record) parent);
		}
	}
}
