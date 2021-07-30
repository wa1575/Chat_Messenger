package messenger;

import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.Font;
import java.awt.GraphicsEnvironment;

public class client 
{
	private String nickname; // Ŭ���̾�Ʈ�� �г���
	private String serverIP; // ���� ������
	private String ID;	// Ŭ���̾�Ʈ�� ���̵�

	private JTextArea incoming; // ä��â
	private JTextField outgoing; // �Է�â
	
	private Socket sock; // ���� ����
	
	private String lastNick; // ���������� ä��â�� ���� ���� Ŭ���̾�Ʈ�� �г���
	private JTextArea connectList; // ������ ����Ʈ
	private BufferedReader reader; // ���������� ������ �޽����� ���� Reader
	private PrintWriter writer; // ���������� ������ �޽����� ���� Writer

	// ��Ʈ ���� : fontNameList�� ���� Ŭ���̾�Ʈ ��ǻ�Ϳ� �ִ� ��� ��Ʈ ����� �ҷ���
	private String fontNameList[] = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
	private Font fontlist[];

	public static void main(String Args[])
	{
		new client().excuteClient();
	}
	
	// ������ ������ �����ǿ� ����� �г����� �Է�
	public void excuteClient()
	{
		JFrame frame = new JFrame("Chatting Program Client");
		JPanel inputNickPanel = new JPanel();
		JPanel buttonPanel = new JPanel();
		JTextField nickField = new JTextField(10);
		JButton button = new JButton("Connect");
		JButton closeButton = new JButton("Close");
		JLabel infoLabel = new JLabel("����Ͻ� �г����� �Է��ϼ���.");
		JLabel ipInfoLabel = new JLabel("���� IP Address�� �Է��ϼ���.");
		JTextField ipField = new JTextField(10);

		// ä�� �ʵ忡�� ���͸� ġ�ų� ��ư�� Ŭ���Ҷ� �̺�Ʈ������ ���
		nickField.addActionListener(new NickNameActionListener(frame, nickField, ipField));
		button.addActionListener(new NickNameActionListener(frame, nickField, ipField));
		closeButton.addActionListener(new ProgramCloseButtonListener());

		// ��ư �гο� ���ӹ�ư�� �ݱ� ��ư�� �߰�
		buttonPanel.add( button );
		buttonPanel.add( closeButton );

		// �����г��� ���̾ƿ��� �ڽ����̾ƿ�(����)���� ����
		inputNickPanel.setLayout(new BoxLayout(inputNickPanel, BoxLayout.Y_AXIS));
		inputNickPanel.add(ipInfoLabel); // ������ ��
		inputNickPanel.add(ipField);		// ������ �ʵ�
		inputNickPanel.add(infoLabel);	// �г��� ��
		inputNickPanel.add(nickField);	// �г��� �ʵ�
		inputNickPanel.add(buttonPanel);	// ��ư �г�
		
		// �������� �⺻�����ư�� ���α׷� ����� ����
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		// ��������� �����ӿ� �߰�
		frame.getContentPane().add(BorderLayout.CENTER, inputNickPanel);
		frame.setSize(300,200); // ������ũ�� ����
		frame.setVisible(true); // ������ ����ȭ
	}

	public void startChatting()
	{
		// ���� ä�� ����
		JFrame frame = new JFrame("Chatting Program Client");
		JPanel SouthPanel = new JPanel();
		JPanel EastPanel = new JPanel();

		// ��ȭ ������ ����Ʈ ���� 
		connectList = new JTextArea(20,7);
		connectList.setEditable(false);
		JLabel connectInfoLabel = new JLabel("- �����ڸ�� - ");

		// �����гο� ������ �󺧰� ������ ����� �߰�
		EastPanel.setLayout(new BoxLayout(EastPanel, BoxLayout.Y_AXIS));
		EastPanel.add( connectInfoLabel );
		EastPanel.add( connectList );

		// ��ȭ�ؽ�Ʈ ����
		incoming = new JTextArea(20,30);
		incoming.setLineWrap(true);
		incoming.setWrapStyleWord(true);
		incoming.setEditable(false); // ���� �Ұ�
		
		// ��ũ�� �߰�
		JScrollPane qScroller = new JScrollPane(incoming);
		qScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		qScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		
		// �Է��ʵ� ����
		outgoing = new JTextField();
		outgoing.addActionListener(new SendActionListener()); // �Է��ʵ忡�� ���� ������ �̺�Ʈ ������
		JButton sendButton = new JButton("Send");
		sendButton.addActionListener(new SendActionListener()); // ��ư Ŭ���� �̺�Ʈ ������
		
		JTextField nickNameField = new JTextField(nickname); // �г��� �ʵ� �߰�
		nickNameField.setEditable(false); // �����Ұ�

		JButton closeButton = new JButton("Close");
		// �ݱ��ư Ŭ���� �̺�Ʈ ������
		closeButton.addActionListener( new ProgramCloseButtonListener() );

		// ��Ʈ �г� ����
		JPanel fontPanel = new JPanel();
		fontlist = new Font[fontNameList.length]; // ��Ʈ����Ʈ�� ��Ʈ���Ӹ���Ʈ�� ���̸�ŭ �Ҵ�
		// ��Ʈ ���� (�⺻������ ũ�� 12�� ����)
		for(int i=0; i<fontNameList.length; ++i) fontlist[i] = new Font(fontNameList[i],  0, 12);
		
		// ��Ʈ ũ�� ����Ʈ
		String fontSizeList[] = {"12","14","16","18","20","22"};
		JComboBox fontSizeComboBox = new JComboBox(fontSizeList);
		// ��Ʈ ũ�� ���� �� �̺�Ʈ ������
		fontSizeComboBox.addActionListener(new FontSizeChangeActionListener() );
		// ��Ʈ ����Ʈ
		JComboBox fontComboBox = new JComboBox(fontNameList);
		// ��Ʈ ���� �� �̺�Ʈ ������
		fontComboBox.addActionListener( new FontChangeActionListener(fontSizeComboBox) );

		// ������ �� ���
		JLabel fontInfoLabel = new JLabel("<��Ʈ ����> - ");
		JLabel fontNameLabel = new JLabel("��Ʈ : ");
		JLabel fontSizeLabel = new JLabel("ũ�� : ");

		// �⺻ ��Ʈ ���� - ����
		fontComboBox.setSelectedItem("����");

		// ���� �г� ����
		SouthPanel.setLayout(new BoxLayout(SouthPanel, BoxLayout.Y_AXIS));
		fontPanel.add(fontInfoLabel); // ��Ʈ�г� ��
		fontPanel.add(fontNameLabel); // ��Ʈ ��
		fontPanel.add(fontComboBox); // ��Ʈ �޺��ڽ�
		fontPanel.add(fontSizeLabel); // ��Ʈ ũ�� ��
		fontPanel.add(fontSizeComboBox); // ��Ʈ ũ�� �޺��ڽ�
		SouthPanel.add(fontPanel); // ��Ʈ �г�
		SouthPanel.add(nickNameField); // �г��� �ʵ�
		SouthPanel.add(outgoing); // �Է� �ʵ�

		// ��ư �г� ����
		JPanel SendCloseButtonPanel = new JPanel();
		SendCloseButtonPanel.add(sendButton);
		SendCloseButtonPanel.add(closeButton);
		SouthPanel.add(SendCloseButtonPanel); // ��ư �г�

		setUpNetworking(); // ��Ʈ��ũ �¾�

		// ���������� ������ �޽����� ���޹ޱ� ���� ������ ����
		Thread readerThread = new Thread( new IncomingReader());
		readerThread.start();

		// �������� �߰��� ��ȭâ
		frame.getContentPane().add(BorderLayout.CENTER, qScroller);
		// �������� ���ʿ� �����г� �߰� ( ��Ʈ�г�, �г���, �Է��ʵ�, ��ư�г� )
		frame.getContentPane().add(BorderLayout.SOUTH, SouthPanel);
		// �������� ���ʿ� �����г� �߰� ( ������ ����Ʈ )
		frame.getContentPane().add(BorderLayout.EAST, EastPanel);

		// �������� �⺻�����ư�� ���α׷� ����� ����
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(600,600); // ũ������
		frame.setVisible(true); // ����ȭ

		outgoing.requestFocus(); // �Է� �ʵ�� ��Ŀ���� ��
	}

	private void setUpNetworking()
	{
		try
		{
			// �Էµ� ���� �����Ƿ� ����, ��Ʈ��ȣ 5000
			sock = new Socket(serverIP, 5000);
			// �ش� ������ InputStreamReader�� ����
			InputStreamReader streamReader = new InputStreamReader(sock.getInputStream());
		
			reader = new BufferedReader(streamReader); // Reader ����
			writer = new PrintWriter(sock.getOutputStream()); // Writer ����

			System.out.println("networking established");

			// ������ �ڽ��� �г��Ӱ� ���� �α����ߴٴ� �޽��� ����
			writer.println("<NICKNAME>" + nickname + "</NICKNAME><NEW_LOGIN>");
			writer.flush();
		}
		// ConnectException �߻� �� �ش� IP�� ������ �������� ���� ��
		catch ( ConnectException e )
		{
			JOptionPane.showMessageDialog(null,serverIP+"�� ������ �� �����ϴ�!!");
			System.exit(1);
		}
		// ���� �߻� �� ���ܻ��� ���
		catch ( Exception e )
		{
			e.printStackTrace();
		}
	}

	// ��Ʈ ���� �̺�Ʈ ������
	public class FontChangeActionListener implements ActionListener
	{
		private JComboBox fontSizeComboBox;

		public FontChangeActionListener(JComboBox cbox)
		{
			fontSizeComboBox = cbox;
		}
		public void actionPerformed( ActionEvent ev )
		{
			// �̺�Ʈ�� �޺��ڽ��� ���� ( ��Ʈ �޺��ڽ� )
			JComboBox combobox = (JComboBox)ev.getSource();

			int size = 12;
			try
			{
				// ��Ʈ ������ ���� ( ��Ʈũ�� �޺� �ڽ��� ���� ���õ� ������ )
				size = Integer.parseInt((String)fontSizeComboBox.getSelectedItem());
			}
			catch ( Exception e)
			{
				e.printStackTrace();
			}

			// ��ȭâ�� ��Ʈ ����
			incoming.setFont(new Font(fontlist[combobox.getSelectedIndex()].getFontName(), 0, size));			
		}
	}

	// ��Ʈ ũ�� �̺�Ʈ ������
	public class FontSizeChangeActionListener implements ActionListener
	{
		public void actionPerformed( ActionEvent ev )
		{
			// �̺�Ʈ�� �޺��ڽ��� ���� ( ��Ʈ ũ�� �޺��ڽ� )
			JComboBox combobox = (JComboBox)ev.getSource();

			// ���� ��ȭâ�� ��Ʈ ����
			Font curFont = incoming.getFont();
			int size = 12;
			try
			{
				// ����� ��Ʈ ũ�� ����
				size = Integer.parseInt((String)combobox.getSelectedItem());
			}
			catch ( Exception e)
			{
				e.printStackTrace();
			}
			// ��ȭâ ��Ʈ ũ�� ����
			incoming.setFont(new Font(curFont.getFontName(), 0, size));
		}
	}

	// �ݱ��ư �̺�Ʈ ������
	public class ProgramCloseButtonListener implements ActionListener
	{
		public void actionPerformed( ActionEvent ev )
		{
			// ���α׷� ����
			System.exit(0);
		}
	}

	// ������ �̺�Ʈ ������
	public class SendActionListener implements ActionListener
	{
		public void actionPerformed( ActionEvent ev )
		{
			try
			{
				// �ڽ��� �г��Ӱ� �Է��� �ؽ�Ʈ�� ������ ����
				writer.println("<NICKNAME>" + nickname + "</NICKNAME>" + outgoing.getText());
				writer.flush();
			}
			catch ( Exception e )
			{
				e.printStackTrace();
			}

			outgoing.setText(""); // �Է� �ʵ带 �����
			outgoing.requestFocus(); // �Է� �ʵ�� ��Ŀ���� ��
		}
	}

	// �г��� �̺�Ʈ ������
	public class NickNameActionListener implements ActionListener
	{
		private JFrame frame;
		private JTextField txt_field;
		private JTextField ipField;
		
		// ������
		public NickNameActionListener(JFrame f, JTextField txt, JTextField ip)
		{
			frame = f;
			txt_field = txt;
			ipField = ip;
		}
		public void actionPerformed( ActionEvent ev )
		{
			nickname =  new String(txt_field.getText()); // �г��� ����
			frame.setVisible(false); // �ش� ������ ����ȭ
			serverIP = ipField.getText(); // ���� ������ ����
			startChatting(); // ä�� ����
		}
	}

	// ���������� ������ �޽����� ���޹ޱ� ���� Runnable
	public class IncomingReader implements Runnable
	{
		public void run()
		{
			String message;
			try
			{
				// ���������� �޽����� ���� ( �޽����� �����ö� ���� ������� )
				while( (message = reader.readLine()) != null )
				{
					System.out.println("read " + message);

					// ������ �޽������� �г��� ����
					int index1 = message.indexOf("<NICKNAME>");
					int index2 = message.indexOf("</NICKNAME>");
					
					if( index1 == -1 || index2 == -1 )
					{
						// �г����� �������� �ʴ´ٸ� 
						// - ������ ù �������� ��� ���������� ������ ���̵� Ȯ��
						// - <YOURID>��� �ʵ��� ���̵� �ڽ��� ���̵�� ����
						index1 = message.indexOf("<YOURID>");
						index2 = message.indexOf("</YOURID>");
						if( index1 == -1 || index2 == -1 )
						{
							continue;
						}
						ID = message.substring(index1+8, index2);
						// ������ ���� �г����� ������.
						writer.println("<NICKNAME>"+nickname+"</NICKNAME><MYID>"+ID+"</MYID>");
						writer.flush();
						continue;
					}
					// �г����� ���� �Ѵٸ� �г��� ����
					String curNick = message.substring(index1+10, index2);
					// �г��Ӻκ��� ������ �޽����� �޽����� ����
					message = message.substring(index2+11, message.length());

					// �޽����� <NEW_LOGIN>�� ������ ��� ���� �˸� �޽��� ���
					if( (message.indexOf("<NEW_LOGIN>")) != -1 )
					{
						message = "====== �˸� : ["+curNick+"]���� ��ȭ�� �����ϼ̽��ϴ�. ======";
						lastNick = new String(" ");
					}
					// �޽����� <LOGOUT>�� ������ ��� ���� ���� �޽��� ���
					else if( (message.indexOf("<LOGOUT>")) != -1 )
					{
						index1 = message.indexOf("<LOGOUT>");
						index2 = message.indexOf("</LOGOUT>");
						if( index1 == -1 || index2 == -1 )
						{
							continue;
						}
						String exitClientNickname = message.substring(index1+8,index2);
						message = "====== �˸� : [" + exitClientNickname + "]���� ��ȭ���� �������ϴ�. ======";
					}
					// �޽����� <CONNECT_LIST>�� ������ ��� ������ ��� ����
					else if(  (message.indexOf("<CONNECT_LIST>")) != -1  )
					{
						// �޽������� <CONNECT_LIST>�� ���ڿ� �Ľ�
						message = message.substring(message.indexOf("<CONNECT_LIST>")+14,message.indexOf("</CONNECT_LIST>"));			
						String parsingList[] = message.split("\\|"); // �ش� ���ڿ����� "|"�� �������� ���ø�

						connectList.setText("");
						// ������ ����Ʈ ����
						for(int i=0; i<parsingList.length; ++i)
						{
							connectList.setText(connectList.getText()+parsingList[i]+"\n");
						}
						continue;
					}
					// �Ϲ� �޽���
					else
					{
						// ���������� ���� �г��Ӱ� ���� �г����� ������ ��� �г��� ��¾���
						if( lastNick.compareTo(curNick) == 0 )
						{
							message = " -> " + message;
						}
						// ���������� ���� �г��Ӱ� ���� �г����� �������� ���� ���
						// - �г��Ӱ� ���� ��� �� ���������� ���� �г����� ����
						else
						{
							message = "["+curNick+"]���� ��ȭ : \n -> " + message;
							lastNick = curNick;
						}
					}
					// ��ȭâ�� message �� ���
					incoming.append(message + "\n");
					// ��ũ���� �׻� ������ ����
					int offset = incoming.getLineEndOffset(incoming.getLineCount()-1);
					incoming.setCaretPosition(offset);
				}
			}
			catch ( Exception e )
			{
				e.printStackTrace();
			}
		}
	}

}
