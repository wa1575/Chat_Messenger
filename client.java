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
	private String nickname; // 클라이언트의 닉네임
	private String serverIP; // 서버 아이피
	private String ID;	// 클라이언트의 아이디

	private JTextArea incoming; // 채팅창
	private JTextField outgoing; // 입력창
	
	private Socket sock; // 서버 소켓
	
	private String lastNick; // 마지막으로 채팅창에 글을 남긴 클라이언트의 닉네임
	private JTextArea connectList; // 접속자 리스트
	private BufferedReader reader; // 서버측에서 보내는 메시지에 대한 Reader
	private PrintWriter writer; // 서버측으로 보내는 메시지에 대한 Writer

	// 폰트 설정 : fontNameList에 현재 클라이언트 컴퓨터에 있는 모든 폰트 목록을 불러옴
	private String fontNameList[] = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
	private Font fontlist[];

	public static void main(String Args[])
	{
		new client().excuteClient();
	}
	
	// 접속할 서버의 아이피와 사용할 닉네임을 입력
	public void excuteClient()
	{
		JFrame frame = new JFrame("Chatting Program Client");
		JPanel inputNickPanel = new JPanel();
		JPanel buttonPanel = new JPanel();
		JTextField nickField = new JTextField(10);
		JButton button = new JButton("Connect");
		JButton closeButton = new JButton("Close");
		JLabel infoLabel = new JLabel("사용하실 닉네임을 입력하세요.");
		JLabel ipInfoLabel = new JLabel("서버 IP Address를 입력하세요.");
		JTextField ipField = new JTextField(10);

		// 채팅 필드에서 엔터를 치거나 버튼을 클릭할때 이벤트리스너 등록
		nickField.addActionListener(new NickNameActionListener(frame, nickField, ipField));
		button.addActionListener(new NickNameActionListener(frame, nickField, ipField));
		closeButton.addActionListener(new ProgramCloseButtonListener());

		// 버튼 패널에 접속버튼과 닫기 버튼을 추가
		buttonPanel.add( button );
		buttonPanel.add( closeButton );

		// 메인패널의 레이아웃을 박스레이아웃(수직)으로 설정
		inputNickPanel.setLayout(new BoxLayout(inputNickPanel, BoxLayout.Y_AXIS));
		inputNickPanel.add(ipInfoLabel); // 아이피 라벨
		inputNickPanel.add(ipField);		// 아이피 필드
		inputNickPanel.add(infoLabel);	// 닉네임 라벨
		inputNickPanel.add(nickField);	// 닉네임 필드
		inputNickPanel.add(buttonPanel);	// 버튼 패널
		
		// 프레임의 기본종료버튼을 프로그램 종료로 설정
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		// 메인페널을 프레임에 추가
		frame.getContentPane().add(BorderLayout.CENTER, inputNickPanel);
		frame.setSize(300,200); // 프레임크기 지정
		frame.setVisible(true); // 프레임 가시화
	}

	public void startChatting()
	{
		// 실제 채팅 시작
		JFrame frame = new JFrame("Chatting Program Client");
		JPanel SouthPanel = new JPanel();
		JPanel EastPanel = new JPanel();

		// 대화 참가자 리스트 관리 
		connectList = new JTextArea(20,7);
		connectList.setEditable(false);
		JLabel connectInfoLabel = new JLabel("- 접속자명단 - ");

		// 동쪽패널에 접속자 라벨과 접속자 명단을 추가
		EastPanel.setLayout(new BoxLayout(EastPanel, BoxLayout.Y_AXIS));
		EastPanel.add( connectInfoLabel );
		EastPanel.add( connectList );

		// 대화텍스트 설정
		incoming = new JTextArea(20,30);
		incoming.setLineWrap(true);
		incoming.setWrapStyleWord(true);
		incoming.setEditable(false); // 수정 불가
		
		// 스크롤 추가
		JScrollPane qScroller = new JScrollPane(incoming);
		qScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		qScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		
		// 입력필드 설정
		outgoing = new JTextField();
		outgoing.addActionListener(new SendActionListener()); // 입력필드에서 엔터 누를시 이벤트 리스너
		JButton sendButton = new JButton("Send");
		sendButton.addActionListener(new SendActionListener()); // 버튼 클릭시 이벤트 리스너
		
		JTextField nickNameField = new JTextField(nickname); // 닉네임 필드 추가
		nickNameField.setEditable(false); // 수정불가

		JButton closeButton = new JButton("Close");
		// 닫기버튼 클릭시 이벤트 리스너
		closeButton.addActionListener( new ProgramCloseButtonListener() );

		// 폰트 패널 설정
		JPanel fontPanel = new JPanel();
		fontlist = new Font[fontNameList.length]; // 폰트리스트를 폰트네임리스트의 길이만큼 할당
		// 폰트 저장 (기본적으로 크기 12로 지정)
		for(int i=0; i<fontNameList.length; ++i) fontlist[i] = new Font(fontNameList[i],  0, 12);
		
		// 폰트 크기 리스트
		String fontSizeList[] = {"12","14","16","18","20","22"};
		JComboBox fontSizeComboBox = new JComboBox(fontSizeList);
		// 폰트 크기 변경 시 이벤트 리스너
		fontSizeComboBox.addActionListener(new FontSizeChangeActionListener() );
		// 폰트 리스트
		JComboBox fontComboBox = new JComboBox(fontNameList);
		// 폰트 변경 시 이벤트 리스너
		fontComboBox.addActionListener( new FontChangeActionListener(fontSizeComboBox) );

		// 각각의 라벨 등록
		JLabel fontInfoLabel = new JLabel("<폰트 변경> - ");
		JLabel fontNameLabel = new JLabel("폰트 : ");
		JLabel fontSizeLabel = new JLabel("크기 : ");

		// 기본 폰트 설정 - 돋음
		fontComboBox.setSelectedItem("돋움");

		// 남쪽 패널 설정
		SouthPanel.setLayout(new BoxLayout(SouthPanel, BoxLayout.Y_AXIS));
		fontPanel.add(fontInfoLabel); // 폰트패널 라벨
		fontPanel.add(fontNameLabel); // 폰트 라벨
		fontPanel.add(fontComboBox); // 폰트 콤보박스
		fontPanel.add(fontSizeLabel); // 폰트 크기 라벨
		fontPanel.add(fontSizeComboBox); // 폰트 크기 콤보박스
		SouthPanel.add(fontPanel); // 폰트 패널
		SouthPanel.add(nickNameField); // 닉네임 필드
		SouthPanel.add(outgoing); // 입력 필드

		// 버튼 패널 생성
		JPanel SendCloseButtonPanel = new JPanel();
		SendCloseButtonPanel.add(sendButton);
		SendCloseButtonPanel.add(closeButton);
		SouthPanel.add(SendCloseButtonPanel); // 버튼 패널

		setUpNetworking(); // 네트워크 셋업

		// 서버측에서 보내는 메시지를 전달받기 위한 스레드 시작
		Thread readerThread = new Thread( new IncomingReader());
		readerThread.start();

		// 프레임의 중간에 대화창
		frame.getContentPane().add(BorderLayout.CENTER, qScroller);
		// 프레임의 남쪽에 남쪽패널 추가 ( 폰트패널, 닉네임, 입력필드, 버튼패널 )
		frame.getContentPane().add(BorderLayout.SOUTH, SouthPanel);
		// 프레임의 동쪽에 동쪽패널 추가 ( 접속자 리스트 )
		frame.getContentPane().add(BorderLayout.EAST, EastPanel);

		// 프레임의 기본종료버튼을 프로그램 종료로 설정
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(600,600); // 크기지정
		frame.setVisible(true); // 가시화

		outgoing.requestFocus(); // 입력 필드로 포커스를 줌
	}

	private void setUpNetworking()
	{
		try
		{
			// 입력된 서버 아이피로 연결, 포트번호 5000
			sock = new Socket(serverIP, 5000);
			// 해당 소켓의 InputStreamReader를 저장
			InputStreamReader streamReader = new InputStreamReader(sock.getInputStream());
		
			reader = new BufferedReader(streamReader); // Reader 지정
			writer = new PrintWriter(sock.getOutputStream()); // Writer 지정

			System.out.println("networking established");

			// 서버로 자신의 닉네임과 새로 로그인했다는 메시지 전달
			writer.println("<NICKNAME>" + nickname + "</NICKNAME><NEW_LOGIN>");
			writer.flush();
		}
		// ConnectException 발생 시 해당 IP의 서버가 열려있지 않은 것
		catch ( ConnectException e )
		{
			JOptionPane.showMessageDialog(null,serverIP+"와 연결할 수 없습니다!!");
			System.exit(1);
		}
		// 예외 발생 시 예외사항 출력
		catch ( Exception e )
		{
			e.printStackTrace();
		}
	}

	// 폰트 변경 이벤트 리스너
	public class FontChangeActionListener implements ActionListener
	{
		private JComboBox fontSizeComboBox;

		public FontChangeActionListener(JComboBox cbox)
		{
			fontSizeComboBox = cbox;
		}
		public void actionPerformed( ActionEvent ev )
		{
			// 이벤트를 콤보박스로 변경 ( 폰트 콤보박스 )
			JComboBox combobox = (JComboBox)ev.getSource();

			int size = 12;
			try
			{
				// 폰트 사이즈 산출 ( 폰트크기 콤보 박스의 현재 선택된 아이템 )
				size = Integer.parseInt((String)fontSizeComboBox.getSelectedItem());
			}
			catch ( Exception e)
			{
				e.printStackTrace();
			}

			// 대화창의 폰트 변경
			incoming.setFont(new Font(fontlist[combobox.getSelectedIndex()].getFontName(), 0, size));			
		}
	}

	// 폰트 크기 이벤트 리스너
	public class FontSizeChangeActionListener implements ActionListener
	{
		public void actionPerformed( ActionEvent ev )
		{
			// 이벤트를 콤보박스로 변경 ( 폰트 크기 콤보박스 )
			JComboBox combobox = (JComboBox)ev.getSource();

			// 현재 대화창의 폰트 산출
			Font curFont = incoming.getFont();
			int size = 12;
			try
			{
				// 변경될 폰트 크기 산출
				size = Integer.parseInt((String)combobox.getSelectedItem());
			}
			catch ( Exception e)
			{
				e.printStackTrace();
			}
			// 대화창 폰트 크기 변경
			incoming.setFont(new Font(curFont.getFontName(), 0, size));
		}
	}

	// 닫기버튼 이벤트 리스너
	public class ProgramCloseButtonListener implements ActionListener
	{
		public void actionPerformed( ActionEvent ev )
		{
			// 프로그램 종료
			System.exit(0);
		}
	}

	// 보내기 이벤트 리스너
	public class SendActionListener implements ActionListener
	{
		public void actionPerformed( ActionEvent ev )
		{
			try
			{
				// 자신의 닉네임과 입력한 텍스트를 서버로 보냄
				writer.println("<NICKNAME>" + nickname + "</NICKNAME>" + outgoing.getText());
				writer.flush();
			}
			catch ( Exception e )
			{
				e.printStackTrace();
			}

			outgoing.setText(""); // 입력 필드를 비워줌
			outgoing.requestFocus(); // 입력 필드로 포커스를 줌
		}
	}

	// 닉네임 이벤트 리스너
	public class NickNameActionListener implements ActionListener
	{
		private JFrame frame;
		private JTextField txt_field;
		private JTextField ipField;
		
		// 생성자
		public NickNameActionListener(JFrame f, JTextField txt, JTextField ip)
		{
			frame = f;
			txt_field = txt;
			ipField = ip;
		}
		public void actionPerformed( ActionEvent ev )
		{
			nickname =  new String(txt_field.getText()); // 닉네임 산출
			frame.setVisible(false); // 해당 프레임 투명화
			serverIP = ipField.getText(); // 서버 아이피 산출
			startChatting(); // 채팅 시작
		}
	}

	// 서버측에서 보내는 메시지를 전달받기 위한 Runnable
	public class IncomingReader implements Runnable
	{
		public void run()
		{
			String message;
			try
			{
				// 서버측에서 메시지를 받음 ( 메시지를 보내올때 까지 블락상태 )
				while( (message = reader.readLine()) != null )
				{
					System.out.println("read " + message);

					// 보내온 메시지에서 닉네임 산출
					int index1 = message.indexOf("<NICKNAME>");
					int index2 = message.indexOf("</NICKNAME>");
					
					if( index1 == -1 || index2 == -1 )
					{
						// 닉네임이 존재하지 않는다면 
						// - 서버에 첫 접속했을 경우 서버측에서 보내온 아이디를 확인
						// - <YOURID>라는 필드의 아이디를 자신의 아이디로 설정
						index1 = message.indexOf("<YOURID>");
						index2 = message.indexOf("</YOURID>");
						if( index1 == -1 || index2 == -1 )
						{
							continue;
						}
						ID = message.substring(index1+8, index2);
						// 서버로 나의 닉네임을 보내줌.
						writer.println("<NICKNAME>"+nickname+"</NICKNAME><MYID>"+ID+"</MYID>");
						writer.flush();
						continue;
					}
					// 닉네임이 존재 한다면 닉네임 산출
					String curNick = message.substring(index1+10, index2);
					// 닉네임부분을 제거한 메시지를 메시지로 저장
					message = message.substring(index2+11, message.length());

					// 메시지에 <NEW_LOGIN>이 존재할 경우 접속 알림 메시지 출력
					if( (message.indexOf("<NEW_LOGIN>")) != -1 )
					{
						message = "====== 알림 : ["+curNick+"]님이 대화에 참여하셨습니다. ======";
						lastNick = new String(" ");
					}
					// 메시지에 <LOGOUT>이 존재할 경우 접속 해제 메시지 출력
					else if( (message.indexOf("<LOGOUT>")) != -1 )
					{
						index1 = message.indexOf("<LOGOUT>");
						index2 = message.indexOf("</LOGOUT>");
						if( index1 == -1 || index2 == -1 )
						{
							continue;
						}
						String exitClientNickname = message.substring(index1+8,index2);
						message = "====== 알림 : [" + exitClientNickname + "]님이 대화방을 떠났습니다. ======";
					}
					// 메시지에 <CONNECT_LIST>가 존재할 경우 접속자 명단 갱신
					else if(  (message.indexOf("<CONNECT_LIST>")) != -1  )
					{
						// 메시지에서 <CONNECT_LIST>의 문자열 파싱
						message = message.substring(message.indexOf("<CONNECT_LIST>")+14,message.indexOf("</CONNECT_LIST>"));			
						String parsingList[] = message.split("\\|"); // 해당 문자열에서 "|"를 기준으로 스플릿

						connectList.setText("");
						// 접속자 리스트 갱신
						for(int i=0; i<parsingList.length; ++i)
						{
							connectList.setText(connectList.getText()+parsingList[i]+"\n");
						}
						continue;
					}
					// 일반 메시지
					else
					{
						// 마지막으로 말한 닉네임과 현재 닉네임이 동일할 경우 닉네임 출력안함
						if( lastNick.compareTo(curNick) == 0 )
						{
							message = " -> " + message;
						}
						// 마지막으로 말한 닉네임과 현재 닉네임이 동일하지 않을 경우
						// - 닉네임과 내용 출력 후 마지막으로 말한 닉네임을 갱신
						else
						{
							message = "["+curNick+"]님의 대화 : \n -> " + message;
							lastNick = curNick;
						}
					}
					// 대화창에 message 를 출력
					incoming.append(message + "\n");
					// 스크롤을 항상 밑으로 지정
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
